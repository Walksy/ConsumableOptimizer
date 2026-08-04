package walksy.consumableoptimizer.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import walksy.consumableoptimizer.ConsumableOptimizer;
import walksy.consumableoptimizer.config.Config;
import walksy.consumableoptimizer.handler.ConsumableHandler;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Inject(
            method = "shouldInstantlyReplaceVisibleItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void shouldSkipHandAnimationOnSwap(ItemStack from, ItemStack to, CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = Minecraft.getInstance();
        if (!Config.modEnabled || client.hasSingleplayerServer() || !ConsumableOptimizer.hasConsumable() || !ConsumableOptimizer.enabledServer) {
            return;
        }
        boolean fromConsumable = from.get(DataComponents.CONSUMABLE) != null;
        boolean toConsumable = to.get(DataComponents.CONSUMABLE) != null;
        if (fromConsumable && toConsumable && from.is(to.getItem()) && ConsumableHandler.shouldSkipHandAnimationOnSwap()) {
            cir.setReturnValue(true);
        }
    }
}