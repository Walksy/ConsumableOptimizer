package walksy.consumableoptimizer.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.consumableoptimizer.ConsumableOptimizer;
import walksy.consumableoptimizer.config.Config;
import walksy.consumableoptimizer.handler.ConsumableHandler;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "tickItemStackUsage", at = @At("HEAD"), cancellable = true)
    private void tickItemStackUsage(ItemStack stack, CallbackInfo ci) {
        if (!Config.modEnabled || MinecraftClient.getInstance().isInSingleplayer() || !ConsumableOptimizer.enabledServer) return;
        if (LivingEntity.class.cast(this) != MinecraftClient.getInstance().player) return;
        ConsumableHandler.handleItemStackUsage(stack, ci);
    }
}
