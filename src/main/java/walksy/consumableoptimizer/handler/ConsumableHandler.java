package walksy.consumableoptimizer.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.ConsumeEffect;
import net.minecraft.item.consume.PlaySoundConsumeEffect;
import net.minecraft.item.consume.TeleportRandomlyConsumeEffect;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.consumableoptimizer.ConsumableOptimizer;
import walksy.consumableoptimizer.DataTrackerValues;

import java.util.List;

public class ConsumableHandler {

    private static boolean cancelServerBurp = false;
    @Nullable
    private static SoundEvent soundToCancel = null;

    public static void handleItemStackUsage(ItemStack stack, CallbackInfo ci) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || player.getItemUseTimeLeft() > 0) return;

        ConsumableComponent component = stack.get(DataComponentTypes.CONSUMABLE);
        if (component == null) return;

        SoundEvent altSound = extractEffects(component, player, stack);
        playConsumptionSounds(player, component);

        if (requiresBurp(stack, component)) {
            playBurpSound(player);
            cancelServerBurp = true;
        }

        if (altSound != null) {
            player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), altSound,
                    player.getSoundCategory(), 1.0f, 1.0f);
        }

        consumeItem(player);
        ci.cancel();
    }

    public static void handleServerResponse(Packet<?> packet, CallbackInfo ci) {

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (packet instanceof EntityTrackerUpdateS2CPacket trackerPacket && player != null) {
            Entity entity = client.world.getEntityById(trackerPacket.id());
            if (entity == player && ConsumableOptimizer.hasConsumable()) {
                List<DataTracker.SerializedEntry<?>> filtered = trackerPacket.trackedValues().stream()
                        .filter(entry -> entry.id() == DataTrackerValues.HEALTH.getId()
                                || entry.id() == DataTrackerValues.ABSORPTION.getId())
                        .toList();

                entity.getDataTracker().writeUpdatedEntries(filtered);
                ci.cancel();
            }
        }

        if (packet instanceof EntityStatusS2CPacket statusPacket
                && statusPacket.getStatus() == EntityStatuses.CONSUME_ITEM
                && player != null
                && ConsumableOptimizer.hasConsumable()
                && statusPacket.getEntity(client.world) == player) {
            ci.cancel();
        }
    }

    public static void handleServerSounds(SoundEvent sound, CallbackInfo ci) {
        if (sound == SoundEvents.ENTITY_PLAYER_BURP && cancelServerBurp) {
            cancelServerBurp = false;
            ci.cancel();
        }

        if (soundToCancel != null && sound == soundToCancel) {
            soundToCancel = null;
            ci.cancel();
        }
    }

    private static SoundEvent extractEffects(ConsumableComponent comp, ClientPlayerEntity player, ItemStack stack) {
        SoundEvent result = null;

        for (ConsumeEffect fx : comp.onConsumeEffects()) {
            if (fx instanceof PlaySoundConsumeEffect se) {
                result = se.sound().value();
            }
        }

        return result;
    }

    private static void playConsumptionSounds(ClientPlayerEntity player, ConsumableComponent component) {
        SoundEvent sound = component.sound().value();
        soundToCancel = sound;

        player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), sound,
                SoundCategory.NEUTRAL, 1.0f, player.getRandom().nextTriangular(1.0f, 0.4f));
    }

    private static boolean requiresBurp(ItemStack stack, ConsumableComponent comp) {
        return comp.useAction() == UseAction.EAT || stack.isOf(Items.HONEY_BOTTLE);
    }

    private static void playBurpSound(ClientPlayerEntity player) {
        player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS,
                0.5f, MathHelper.nextBetween(player.getRandom(), 0.9f, 1.0f));
    }

    private static void consumeItem(ClientPlayerEntity player) {
        if (!player.isUsingItem()) return;

        Hand hand = player.getActiveHand();
        ItemStack activeStack = player.getActiveItem();
        ItemStack handStack = player.getStackInHand(hand);

        if (!ItemStack.areEqual(activeStack, handStack)) {
            player.stopUsingItem();
            return;
        }

        if (!activeStack.isEmpty()) {
            ItemStack result = activeStack.finishUsing(player.getWorld(), player);
            if (!ItemStack.areEqual(result, activeStack)) {
                player.setStackInHand(hand, result);
            }
            player.clearActiveItem();
        }
    }
}
