package draaft.world;

import draaft.persistent.WorldManifest;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public abstract class EnchantUtils {
    public static boolean levelOneEnchants() {
        if (
            FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
                && MinecraftClientWrapper.available()
                && MinecraftClientWrapper.getWorld() != null
        ) {
            var world = MinecraftClientWrapper.getWorld();

            return WorldManifest.get(world).on(WorldManifest.Feature.LEVEL_ONE_ENCHANTS);
        } else {
            return false;
        }
    }

}
