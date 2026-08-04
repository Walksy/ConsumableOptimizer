package walksy.consumableoptimizer.handler;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;

import java.util.Objects;

public class ConsumptionStateHandler {

    private static final long SERVER_WAIT_TIMEOUT_MS = 500L;
    private static final long TRACKER_TIMEOUT_MS = 1500L;

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
        if (this.waitingForServer && System.currentTimeMillis() - this.serverWaitStartTime > SERVER_WAIT_TIMEOUT_MS) {
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

    public void resetBurpSuppression() {
        this.suppressServerBurp = false;
    }

    public void setSoundSuppression(final SoundEvent sound) {
        this.soundToSuppress = sound;
    }

    public boolean isSoundSuppressed(final SoundEvent sound) {
        return this.soundToSuppress != null && Objects.equals(this.soundToSuppress, sound);
    }

    public void clearSoundSuppression() {
        this.soundToSuppress = null;
    }

    public boolean updateTrack(final SynchedEntityData.DataValue<?> entry, final LocalPlayer player) {
        final int value = this.extractTrackerValue(entry);
        final boolean beginConsuming = value == 0 || value == 2;
        final boolean stopConsuming = value == 1 || value == 3;
        if (stopConsuming) {
            this.stopServerWait();
        }
        if (player.isUsingItem()) {
            final ItemStack stack = player.getActiveItem();
            final Consumable component = stack.get(DataComponents.CONSUMABLE);
            if (component != null && component.canConsume(player, stack)) {
                return true;
            }
        }
        if (stopConsuming) {
            this.suppressEquipmentAnimation = false;
        }
        if (this.trackerState == 0) {
            return false;
        }
        if (System.currentTimeMillis() - this.trackerTriggerTime > TRACKER_TIMEOUT_MS) {
            this.trackerState = 0;
            return false;
        }
        if (this.trackerState == 1) {
            this.trackerState = beginConsuming ? 2 : 0;
            return beginConsuming;
        }
        if (this.trackerState == 2) {
            this.trackerState = 0;
            return stopConsuming;
        }
        return false;
    }

    private int extractTrackerValue(final SynchedEntityData.DataValue<?> entry) {
        final Object value = entry.value();
        if (value instanceof Number n) {
            return n.intValue();
        }
        if (value instanceof Boolean b) {
            return b ? 1 : 0;
        }
        return -1;
    }
}
