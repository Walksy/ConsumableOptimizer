package walksy.consumableoptimizer.config;

import main.walksy.lib.api.WalksyLibConfig;
import main.walksy.lib.core.config.impl.LocalConfig;
import main.walksy.lib.core.config.local.Category;
import main.walksy.lib.core.config.local.Option;
import main.walksy.lib.core.config.local.options.BooleanOption;
import main.walksy.lib.core.config.local.options.groups.OptionGroup;
import main.walksy.lib.core.utils.PathUtils;

public class Config implements WalksyLibConfig {

    public static boolean modEnabled = true;

    private final Option<Boolean> modEnabledOption = BooleanOption.createBuilder("Mod Enabled", () -> modEnabled, modEnabled, newValue -> modEnabled = newValue)
        .build();

    private final Category generalCategory = Category.createBuilder("General")
        .group(OptionGroup.createBuilder("General Options")
            .addOption(modEnabledOption)
            .build())
        .build();

    @Override
    public LocalConfig define() {
        return LocalConfig.createBuilder("Consumable Optimizer")
            .path(PathUtils.ofConfigDir("consumableoptimizer"))
            .category(generalCategory)
            .build();
    }
}
