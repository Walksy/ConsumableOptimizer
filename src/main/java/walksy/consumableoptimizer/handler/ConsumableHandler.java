package walksy.consumableoptimizer.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.PlaySoundConsumeEffect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.consumableoptimizer.ConsumableOptimizer;
import walksy.consumableoptimizer.data.EntityDataTrackerValues;

import java.util.List;

public final class ConsumableHandler {

    public static final ConsumptionStateHandler STATE = new ConsumptionStateHandler();
    private static final Minecraft client = Minecraft.getInstance();

    public static void handleItemStackUsage(ItemStack stack, CallbackInfo ci) {
        LocalPlayer player = client.player;
        Consumable component = stack.get(DataComponents.CONSUMABLE);
        if (player == null || component == null) return;
        if (!component.canConsume(player, stack) || player.getCooldowns().isOnCooldown(stack)) {
            client.gameMode.releaseUsingItem(player);
        }
        if (!(player.getUseItemRemainingTicks() > 0)) {
            processAudio(player, stack, component);
            consume(player);

            ci.cancel();
        }
    }

    public static void handlePacket(Packet<?> packet, CallbackInfo ci) {
        if (client.level == null || client.player == null) return;

        if (packet instanceof ClientboundEntityEventPacket status) {
            if (status.getEntity(client.level) == client.player && status.getEventId() == EntityEvent.USE_ITEM_COMPLETE) {
                if (STATE.isWaitingForServer()) {
                    STATE.stopServerWait();
                    ci.cancel();
                }
            }
        }
    }

    public static void handleEntityTrackerUpdate(ClientboundSetEntityDataPacket packet, CallbackInfo ci) {
        if (client.level == null || client.player == null || !ConsumableOptimizer.hasConsumable()) return;

        Entity entity = client.level.getEntity(packet.id());
        if (entity != client.player) return;

        List<SynchedEntityData.DataValue<?>> values = packet.packedItems();
        if (values == null) return;

        int id = EntityDataTrackerValues.CONSUMPTION.getId();
        values.stream().filter(e -> e != null && e.id() == id).findFirst().ifPresent(entry -> {
            if (STATE.updateTrack(entry, client.player) && client.screen == null) {
                applyFilteredTracker(client.player, values, id);
                ci.cancel();
            }
        });
    }

    public static void handleServerSounds(SoundEvent sound, CallbackInfo ci) {
        if (STATE.shouldSuppressBurp() && sound == SoundEvents.PLAYER_BURP) {
            STATE.resetBurpSuppression();
            ci.cancel();
        } else if (STATE.isSoundSuppressed(sound)) {
            STATE.clearSoundSuppression();
            ci.cancel();
        }
    }

    private static void processAudio(LocalPlayer player, ItemStack stack, Consumable comp) {
        STATE.setSoundSuppression(comp.sound().value());
        playSound(player, comp.sound().value(), true);

        if (comp.animation() == ItemUseAnimation.EAT || stack.is(Items.HONEY_BOTTLE)) {
            playSound(player, SoundEvents.PLAYER_BURP, false, 0.5f, Mth.nextFloat(player.getRandom(), 0.9f, 1.0f));
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

    private static void consume(LocalPlayer player) {
        if (player.isUsingItem()) {
            InteractionHand hand = player.getUsedItemHand();
            if (!player.getActiveItem().equals(player.getItemInHand(hand))) {
                client.gameMode.releaseUsingItem(player);
            } else {
                if (!player.getActiveItem().isEmpty() && player.isUsingItem()) {
                    finishUsing(player);
                    player.stopUsingItem();
                    STATE.startServerWait();
                }
            }
        }
    }

    public static void finishUsing(LivingEntity user) {
        ItemStack stack = user.getActiveItem();
        Consumable component = stack.get(DataComponents.CONSUMABLE);
        if (component != null) {
            finish(component, user, stack);
        }
    }

    private static void finish(Consumable component, LivingEntity user, ItemStack stack) {
        component.emitParticlesAndSounds(user.getRandom(), user, stack, 16);
        STATE.suppressEquipmentAnimation();
    }

    private static void playSound(LocalPlayer player, SoundEvent sound, boolean randomPitch) {
        float pitch = randomPitch ? player.getRandom().triangle(1.0f, 0.4f) : 1.0f;
        playSound(player, sound, randomPitch, 1.0f, pitch);
    }

    private static void playSound(LocalPlayer player, SoundEvent sound, boolean randomPitch, float volume, float pitch) {
        client.level.playSound(player, player.getX(), player.getY(), player.getZ(), sound, SoundSource.NEUTRAL, volume, pitch);
    }

    private static void applyFilteredTracker(LocalPlayer player, List<SynchedEntityData.DataValue<?>> values, int excludeId) {
        player.getEntityData().assignValues(values.stream()
                .filter(e -> e != null && e.id() != excludeId)
                .toList());
    }
}