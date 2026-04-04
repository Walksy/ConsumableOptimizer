package walksy.consumableoptimizer.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import walksy.consumableoptimizer.ConsumableOptimizer;
import walksy.consumableoptimizer.config.Config;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {


    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    public abstract boolean hasSingleplayerServer();

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;useItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    public void doItemUse(CallbackInfo ci, InteractionHand[] var1, int var2, int var3, InteractionHand hand, ItemStack heldItem) {
        if (!Config.modEnabled || this.hasSingleplayerServer() || !ConsumableOptimizer.enabledServer) return;
        ItemStack stack = this.player.getItemInHand(hand);
        Consumable component = stack.get(DataComponents.CONSUMABLE);
        if (component != null && !component.canConsume(player, stack)) {
            ci.cancel();
        }
    }
}