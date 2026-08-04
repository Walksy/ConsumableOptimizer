package walksy.consumableoptimizer.config;

import main.walksy.lib.api.WalksyLibApi;
import main.walksy.lib.api.WalksyLibConfig;
import main.walksy.lib.core.config.impl.ModConfig;

public class WalksyLibIntegration implements WalksyLibApi {

    @Override
    public ModConfig getConfig() {
        WalksyLibConfig config = new Config();
        return config.getOrCreateConfig();
    }
}
