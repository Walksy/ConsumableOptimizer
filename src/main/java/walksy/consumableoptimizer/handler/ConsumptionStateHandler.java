package walksy.consumableoptimizer.handler;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;

import java.util.Objects;

public class ConsumptionStateHandler {
    private boolean suppressServerBurp;
    private boolean suppressEquipmentAnimation;
    private SoundEvent soundToSuppress;
    private int trackerState;
    private long trackerTriggerTime;

    public void suppressBurp() {
        this.suppressServerBurp = true;
    }

    public void suppressEquipmentAnimation() {
        this.suppressEquipmentAnimation = true;
    }

    public boolean shouldSuppressBurp() {
        return this.suppressServerBurp;
    }

    public boolean shouldSuppressEquipmentAnimation() {
        return this.suppressEquipmentAnimation;
    }

    public void resetEquipmentAnimation() {
        this.suppressEquipmentAnimation = false;
    }

    public void resetBurpSuppression() {
        this.suppressServerBurp = false;
    }

    public void setSoundSuppression(SoundEvent sound) {
        this.soundToSuppress = sound;
    }

    public boolean isSoundSuppressed(SoundEvent sound) {
        return soundToSuppress != null && Objects.equals(soundToSuppress, sound);
    }

    public void clearSoundSuppression() {
        this.soundToSuppress = null;
    }

    public void track() {
        this.trackerState = 1;
        this.trackerTriggerTime = System.currentTimeMillis();
    }

    public boolean updateTrack(DataTracker.SerializedEntry<?> entry, ClientPlayerEntity player) {
        if (player.isUsingItem()) {
            ItemStack stack = player.getActiveItem();
            ConsumableComponent component = stack.get(DataComponentTypes.CONSUMABLE);
            if (component != null && component.canConsume(player, stack)) {
                return true;
            }
        }

        int value = extractTrackerValue(entry);
        boolean beginConsuming = value == 0 || value == 2;
        boolean stopConsuming = value == 1 || value == 3;

        if (stopConsuming) {
            this.suppressEquipmentAnimation = false;
        }

        if (trackerState == 0) {
            return false;
        }

        if (System.currentTimeMillis() - trackerTriggerTime > 1500) {
            trackerState = 0;
            return false;
        }

        if (trackerState == 1) {
            if (beginConsuming) {
                trackerState = 2;
                return true;
            }

            trackerState = 0;
            return false;
        }

        if (trackerState == 2) {
            if (stopConsuming) {
                trackerState = 0;
                return true;
            }

            trackerState = 0;
            return false;
        }

        return false;
    }


    private int extractTrackerValue(DataTracker.SerializedEntry<?> entry) {
        if (entry.value() instanceof Number n) {
            return n.intValue();
        }
        if (entry.value() instanceof Boolean b) {
            return b ? 1 : 0;
        }
        return -1;
    }
}
