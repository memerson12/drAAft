package draaft.compat;

import net.fabricmc.loader.api.FabricLoader;

public abstract class ModCompat {
    public static boolean hasSpeedrunIGT() {
        return FabricLoader.getInstance().isModLoaded("speedrunigt");
    }

    public static boolean hasFastReset() {
        return FabricLoader.getInstance().isModLoaded("fast_reset");
    }

    public static boolean hasAtum() {
        return FabricLoader.getInstance().isModLoaded("atum");
    }

    public static boolean hasStandardSettings() {
        return FabricLoader.getInstance().isModLoaded("standardsettings");
    }
}
