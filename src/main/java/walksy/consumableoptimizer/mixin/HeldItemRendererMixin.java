package walksy.consumableoptimizer.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import walksy.consumableoptimizer.ConsumableOptimizer;
import walksy.consumableoptimizer.config.Config;
import walksy.consumableoptimizer.handler.ConsumableHandler;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Inject(
            method = "shouldSkipHandAnimationOnSwap",
            at = @At("HEAD"),
            cancellable = true
    )
    private void shouldSkipHandAnimationOnSwap(ItemStack from, ItemStack to, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (!Config.modEnabled || client.isInSingleplayer() || !ConsumableOptimizer.hasConsumable() || !ConsumableOptimizer.enabledServer) {
            return;
        }

        boolean fromConsumable = from.get(DataComponentTypes.CONSUMABLE) != null;
        boolean toConsumable = to.get(DataComponentTypes.CONSUMABLE) != null;
        if (fromConsumable && toConsumable && from.isOf(to.getItem()) && ConsumableHandler.shouldSkipHandAnimationOnSwap()) {
            cir.setReturnValue(true);
        }
    }
}