package draaft.compat.fastreset;

import fast_reset.client.interfaces.FRMinecraftServer;
import net.minecraft.server.MinecraftServer;

public abstract class FastResetCompat {
    public static void preventSaving(MinecraftServer server) {
        ((FRMinecraftServer) server).fastReset$fastReset();
    }
}
