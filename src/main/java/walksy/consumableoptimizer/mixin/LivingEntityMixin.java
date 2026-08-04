package walksy.consumableoptimizer.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.consumableoptimizer.ConsumableOptimizer;
import walksy.consumableoptimizer.config.Config;
import walksy.consumableoptimizer.handler.ConsumableHandler;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "updateUsingItem", at = @At("HEAD"), cancellable = true)
    private void tickItemStackUsage(ItemStack useItem, CallbackInfo ci) {
        if (!Config.modEnabled || Minecraft.getInstance().hasSingleplayerServer() || !ConsumableOptimizer.enabledServer) {
            return;
        }
        if (LivingEntity.class.cast(this) != Minecraft.getInstance().player) {
            return;
        }
        ConsumableHandler.handleItemStackUsage(useItem, ci);
    }
}
