package walksy.consumableoptimizer.config;

import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import walksy.consumableoptimizer.config.impl.CategoryBank;

public class Config {

    public static final ConfigClassHandler<Config> CONFIG = ConfigClassHandler.createBuilder(Config.class)
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("consumableoptimizer.json"))
                    .build())
            .build();

    @SerialEntry
    public boolean modEnabled = true;

    @SuppressWarnings("deprecation") //stop the compiler crying
    public static Screen createConfigScreen(Screen parent) {
        var screen = YetAnotherConfigLib.create(CONFIG, (defaults, config, builder) -> {
            builder.title(Text.literal("Consumable Optimizer Config"));
            builder.category(CategoryBank.general(config, defaults));
            return builder;
        });
        return screen.generateScreen(parent);
    }
}
