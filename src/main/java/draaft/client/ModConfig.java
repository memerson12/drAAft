package draaft.client;

import me.contaria.speedrunapi.config.api.SpeedrunConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import static draaft.draaft.MOD_ID;

@Environment(EnvType.CLIENT) // SpeedrunAPI fails to load on servers
public class ModConfig implements SpeedrunConfig {
    public boolean disablePeaceful = true;

    private static ModConfig instance;

    public static ModConfig getInstance() {
        return instance;
    }

    @Override
    public String modID() {
        return MOD_ID;
    }

    {
        ModConfig.instance = this;
    }
}
