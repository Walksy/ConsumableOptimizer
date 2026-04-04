package walksy.consumableoptimizer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import walksy.consumableoptimizer.network.DisableModPayload;
import walksy.consumableoptimizer.network.HandshakePayload;

public class ConsumableOptimizer implements ModInitializer {

    public static boolean enabledServer = true;

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.clientboundPlay().register(DisableModPayload.ID, DisableModPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(HandshakePayload.ID, HandshakePayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(DisableModPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                enabledServer = false;
                Component message = Component.empty()
                    .append(Component.literal("[Consumable Optimizer] ").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .append(Component.literal("This server has disabled Consumable Optimizer.").withStyle(ChatFormatting.GRAY));

                context.player().sendSystemMessage(message);
            });
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            sender.sendPacket(new HandshakePayload());
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            enabledServer = true;
        });
    }

    public static boolean hasConsumable() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null &&
            (player.getMainHandItem().get(DataComponents.CONSUMABLE) != null
                || player.getOffhandItem().get(DataComponents.CONSUMABLE) != null);
    }
}
