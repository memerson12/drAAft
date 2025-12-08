package draaft.world;

import draaft.persistent.WorldManifest;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

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

    // Wrapper to avoid class loading issues on dedicated servers
    private static class MinecraftClientWrapper {
        static boolean available() {
            var client = MinecraftClient.getInstance();

            return client != null && client.getServer() != null;
        }

        static @Nullable ServerWorld getWorld() {
            assert MinecraftClient.getInstance().getServer() != null;

            return MinecraftClient.getInstance().getServer().getOverworld();
        }
    }
}
