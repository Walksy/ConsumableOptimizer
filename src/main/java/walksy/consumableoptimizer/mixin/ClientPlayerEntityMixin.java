package walksy.consumableoptimizer.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import walksy.consumableoptimizer.handler.ConsumableHandler;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Redirect(method = "applyMovementSpeedFactors", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean applyMovementSpeedFactors(ClientPlayerEntity instance) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (ConsumableHandler.STATE.isWaitingForServer()) {
            player.setSprinting(false);
            return true;
        }
        return player.isUsingItem();
    }
}
