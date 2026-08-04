package walksy.consumableoptimizer.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import walksy.consumableoptimizer.config.Config;
import walksy.consumableoptimizer.handler.ConsumableHandler;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Redirect(method = "modifyInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    private boolean applyMovementSpeedFactors(final LocalPlayer this$0) {
        if (ConsumableHandler.isWaitingForServer() && Config.modEnabled) {
            this$0.setSprinting(false);
            return true;
        }
        return this$0.isUsingItem();
    }
}
