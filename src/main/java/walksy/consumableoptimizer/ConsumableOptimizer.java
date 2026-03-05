package walksy.consumableoptimizer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import walksy.consumableoptimizer.network.DisableModPayload;
import walksy.consumableoptimizer.network.HandshakePayload;

public class ConsumableOptimizer implements ModInitializer {

    public static boolean enabledServer = true;

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(DisableModPayload.ID, DisableModPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(HandshakePayload.ID, HandshakePayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(DisableModPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                enabledServer = false;
                Text message = Text.empty()
                    .append(Text.literal("[Consumable Optimizer] ").formatted(Formatting.RED, Formatting.BOLD))
                    .append(Text.literal("This server has disabled Consumable Optimizer.").formatted(Formatting.GRAY));

                context.player().sendMessage(message, false);
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
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        return player != null &&
            (player.getMainHandStack().get(DataComponentTypes.CONSUMABLE) != null
                || player.getOffHandStack().get(DataComponentTypes.CONSUMABLE) != null);
    }
}
