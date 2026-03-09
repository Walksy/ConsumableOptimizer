package walksy.consumableoptimizer.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.PlaySoundConsumeEffect;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.consumableoptimizer.ConsumableOptimizer;
import walksy.consumableoptimizer.data.EntityDataTrackerValues;

import java.util.List;

public final class ConsumableHandler {

    public static final ConsumptionStateHandler STATE = new ConsumptionStateHandler();
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    public static void handleItemStackUsage(ItemStack stack, CallbackInfo ci) {
        ClientPlayerEntity player = CLIENT.player;
        ConsumableComponent component = stack.get(DataComponentTypes.CONSUMABLE);
        if (player == null || component == null) return;
        if (!component.canConsume(player, stack) || player.getItemCooldownManager().isCoolingDown(stack)) {
            CLIENT.interactionManager.stopUsingItem(player);
        }
        if (!(player.getItemUseTimeLeft() > 0)) {
            processAudio(player, stack, component);
            consume(player);

            ci.cancel();
        }
    }

    public static void handlePacket(Packet<?> packet, CallbackInfo ci) {
        if (CLIENT.world == null || CLIENT.player == null) return;

        if (packet instanceof EntityStatusS2CPacket status) {
            if (status.getEntity(CLIENT.world) == CLIENT.player && status.getStatus() == EntityStatuses.CONSUME_ITEM) {
                if (STATE.isWaitingForServer()) {
                    STATE.stopServerWait();
                    ci.cancel();
                }
            }
        }
    }

    public static void handleEntityTrackerUpdate(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci) {
        if (CLIENT.world == null || CLIENT.player == null || !ConsumableOptimizer.hasConsumable()) return;

        Entity entity = CLIENT.world.getEntityById(packet.id());
        if (entity != CLIENT.player) return;

        List<DataTracker.SerializedEntry<?>> values = packet.trackedValues();
        if (values == null) return;

        int id = EntityDataTrackerValues.CONSUMPTION.getId();
        values.stream().filter(e -> e != null && e.id() == id).findFirst().ifPresent(entry -> {
            if (STATE.updateTrack(entry, CLIENT.player) && CLIENT.currentScreen == null) {
                applyFilteredTracker(CLIENT.player, values, id);
                ci.cancel();
            }
        });
    }

    public static void handleServerSounds(SoundEvent sound, CallbackInfo ci) {
        if (STATE.shouldSuppressBurp() && sound == SoundEvents.ENTITY_PLAYER_BURP) {
            STATE.resetBurpSuppression();
            ci.cancel();
        } else if (STATE.isSoundSuppressed(sound)) {
            STATE.clearSoundSuppression();
            ci.cancel();
        }
    }

    private static void processAudio(ClientPlayerEntity player, ItemStack stack, ConsumableComponent comp) {
        STATE.setSoundSuppression(comp.sound().value());
        playSound(player, comp.sound().value(), true);

        if (comp.useAction() == UseAction.EAT || stack.isOf(Items.HONEY_BOTTLE)) {
            playSound(player, SoundEvents.ENTITY_PLAYER_BURP, false, 0.5f, MathHelper.nextBetween(player.getRandom(), 0.9f, 1.0f));
            STATE.suppressBurp();
        }

        comp.onConsumeEffects().stream()
                .filter(e -> e instanceof PlaySoundConsumeEffect)
                .map(e -> ((PlaySoundConsumeEffect) e).sound().value())
                .forEach(s -> playSound(player, s, false));
    }

    public static boolean shouldSkipHandAnimationOnSwap() {
        return STATE.shouldSuppressEquipmentAnimation();
    }

    private static void consume(ClientPlayerEntity player) {
        if (player.isUsingItem()) {
            Hand hand = player.getActiveHand();
            if (!player.getActiveItem().equals(player.getStackInHand(hand))) {
                CLIENT.interactionManager.stopUsingItem(player);
            } else {
                if (!player.getActiveItem().isEmpty() && player.isUsingItem()) {
                    finishUsing(player);
                    player.clearActiveItem();
                    STATE.startServerWait();
                }
            }
        }
    }

    public static void finishUsing(LivingEntity user) {
        ItemStack stack = user.getActiveItem();
        ConsumableComponent component = stack.get(DataComponentTypes.CONSUMABLE);
        if (component != null) {
            finish(component, user, stack);
        }
    }

    private static void finish(ConsumableComponent component, LivingEntity user, ItemStack stack) {
        component.spawnParticlesAndPlaySound(user.getRandom(), user, stack, 16);
        STATE.suppressEquipmentAnimation();
    }

    private static void playSound(ClientPlayerEntity player, SoundEvent sound, boolean randomPitch) {
        float pitch = randomPitch ? player.getRandom().nextTriangular(1.0f, 0.4f) : 1.0f;
        playSound(player, sound, randomPitch, 1.0f, pitch);
    }

    private static void playSound(ClientPlayerEntity player, SoundEvent sound, boolean randomPitch, float volume, float pitch) {
        CLIENT.world.playSound(player, player.getX(), player.getY(), player.getZ(), sound, SoundCategory.NEUTRAL, volume, pitch);
    }

    private static void applyFilteredTracker(ClientPlayerEntity player, List<DataTracker.SerializedEntry<?>> values, int excludeId) {
        player.getDataTracker().writeUpdatedEntries(values.stream()
                .filter(e -> e != null && e.id() != excludeId)
                .toList());
    }
}