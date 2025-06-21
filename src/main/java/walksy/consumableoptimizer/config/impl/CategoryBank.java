package walksy.consumableoptimizer.config.impl;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import net.minecraft.text.Text;
import walksy.consumableoptimizer.config.Config;


public class CategoryBank {


    public static ConfigCategory general(Config config, Config defaults)
    {
        var modSettingsGroupBuilder = OptionGroup.createBuilder()
            .name(Text.literal("General"));


        modSettingsGroupBuilder.option(Option.<Boolean>createBuilder()
            .name(Text.literal("Mod Enabled"))
            .binding(
                defaults.modEnabled,
                () -> config.modEnabled,
                value -> config.modEnabled = value
            )
            .controller(BooleanControllerBuilder::create)
            .build()
        );


        return ConfigCategory.createBuilder()
            .name(Text.literal("Mod Enabled"))
            .group(modSettingsGroupBuilder.build())
            .build();
    }
}
