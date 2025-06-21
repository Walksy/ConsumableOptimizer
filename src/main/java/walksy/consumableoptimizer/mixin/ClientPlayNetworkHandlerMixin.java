package walksy.consumableoptimizer.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.consumableoptimizer.config.Config;
import walksy.consumableoptimizer.handler.ConsumableHandler;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onPlaySound", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/sound/SoundCategory;FFJ)V", shift = At.Shift.BEFORE), cancellable = true)
    public void onPlaySound(PlaySoundS2CPacket packet, CallbackInfo ci)
    {
        if (!Config.CONFIG.instance().modEnabled) return;
        ConsumableHandler.handleServerSounds(packet.getSound().value(), ci);
    }
}
