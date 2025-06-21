package walksy.consumableoptimizer;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import walksy.consumableoptimizer.config.Config;

public class ConsumableOptimizer implements ModInitializer {


    @Override
    public void onInitialize()
    {
        Config.CONFIG.load();
    }

    public static boolean hasConsumable() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        return player != null &&
            (player.getMainHandStack().get(DataComponentTypes.CONSUMABLE) != null
                || player.getOffHandStack().get(DataComponentTypes.CONSUMABLE) != null);
    }
}
