package walksy.consumableoptimizer.handler;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;

import java.util.Objects;

public class ConsumptionStateHandler {
    private boolean suppressServerBurp;
    private boolean suppressEquipmentAnimation;
    private SoundEvent soundToSuppress;
    private int trackerState;
    private long trackerTriggerTime;
    private volatile boolean waitingForServer;
    private volatile long serverWaitStartTime;

    public void startServerWait() {
        this.waitingForServer = true;
        this.serverWaitStartTime = System.currentTimeMillis();
    }

    public boolean isWaitingForServer() {
        if (this.waitingForServer && (System.currentTimeMillis() - this.serverWaitStartTime > 500)) {
            this.waitingForServer = false;
        }
        return this.waitingForServer;
    }

    public void stopServerWait() {
        this.waitingForServer = false;
    }

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
        int value = extractTrackerValue(entry);
        boolean beginConsuming = value == 0 || value == 2;
        boolean stopConsuming = value == 1 || value == 3;

        if (stopConsuming) {
            this.stopServerWait();
        }

        if (player.isUsingItem()) {
            ItemStack stack = player.getActiveItem();
            FoodComponent component = stack.get(DataComponentTypes.FOOD);
            if (component != null && player.canConsume(component.canAlwaysEat())) {
                return true;
            }
        }

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
