package walksy.consumableoptimizer.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.consumableoptimizer.ConsumableOptimizer;
import walksy.consumableoptimizer.config.Config;
import walksy.consumableoptimizer.handler.ConsumableHandler;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method={"handleSoundEvent"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/ClientLevel;playSeededSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V", shift=At.Shift.BEFORE)}, cancellable=true)
    public void onPlaySound(ClientboundSoundPacket packet, CallbackInfo ci) {
        if (!Config.modEnabled || Minecraft.getInstance().hasSingleplayerServer() || !ConsumableOptimizer.enabledServer) return;
        ConsumableHandler.handleServerSounds(packet.getSound().value(), ci);
    }

    @Inject(method={"handleSetEntityData"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/ClientLevel;getEntity(I)Lnet/minecraft/world/entity/Entity;", shift=At.Shift.BEFORE)}, cancellable=true)
    public void onEntityTrackerUpdate(ClientboundSetEntityDataPacket packet, CallbackInfo ci) {
        if (!Config.modEnabled || Minecraft.getInstance().hasSingleplayerServer() || !ConsumableOptimizer.enabledServer) return;
        ConsumableHandler.handleEntityTrackerUpdate(packet, ci);
    }
}