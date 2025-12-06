package draaft.world;

import draaft.persistent.WorldManifest;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public abstract class EnchantUtils {
    public static boolean levelOneEnchants() {
        if (
            FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
                && MinecraftClientWrapper.available()
                && MinecraftClientWrapper.getServer() != null
        ) {
            var server = MinecraftClientWrapper.getServer();

            return WorldManifest.get(server).on(WorldManifest.Feature.LEVEL_ONE_ENCHANTS);
        } else {
            return false;
        }
    }

    private static class MinecraftClientWrapper {
        static boolean available() {
            return MinecraftClient.getInstance() != null;
        }

        static @Nullable MinecraftServer getServer() {
            return MinecraftClient.getInstance().getServer();
        }
    }
}
