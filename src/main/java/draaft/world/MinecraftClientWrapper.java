package draaft.world;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

// Wrapper to avoid class loading issues on dedicated servers
public class MinecraftClientWrapper {
    public static boolean available() {
        var client = MinecraftClient.getInstance();

        return client != null && client.getServer() != null;
    }

    public static @Nullable ServerWorld getWorld() {
        assert MinecraftClient.getInstance().getServer() != null;

        return MinecraftClient.getInstance().getServer().getOverworld();
    }
}
