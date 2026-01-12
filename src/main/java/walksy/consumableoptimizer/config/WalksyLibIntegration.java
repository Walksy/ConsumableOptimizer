package walksy.consumableoptimizer.config;

import main.walksy.lib.api.WalksyLibApi;
import main.walksy.lib.api.WalksyLibConfig;
import main.walksy.lib.core.config.impl.LocalConfig;

public class WalksyLibIntegration implements WalksyLibApi {

    @Override
    public LocalConfig getConfig() {
        WalksyLibConfig config = new Config();
        return config.getOrCreateConfig();
    }
}
