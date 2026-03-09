package walksy.consumableoptimizer.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.consumableoptimizer.ConsumableOptimizer;
import walksy.consumableoptimizer.config.Config;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {


    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;interactItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"), cancellable = true)
    public void doItemUse(CallbackInfo ci, @Local Hand hand) {
        if (!Config.modEnabled || MinecraftClient.getInstance().isInSingleplayer() || !ConsumableOptimizer.enabledServer) return;
        ItemStack stack = this.player.getStackInHand(hand);
        ConsumableComponent component = stack.get(DataComponentTypes.CONSUMABLE);
        if (component != null && !component.canConsume(player, stack)) {
            ci.cancel();
        }
    }
}