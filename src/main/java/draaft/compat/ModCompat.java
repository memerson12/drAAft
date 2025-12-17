package draaft.compat;

import net.fabricmc.loader.api.FabricLoader;

public abstract class ModCompat {
    public static boolean hasSpeedrunIGT() {
        return FabricLoader.getInstance().isModLoaded("speedrunigt");
    }
}
