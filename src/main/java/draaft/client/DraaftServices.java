package draaft.client;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

public record DraaftServices(
    URI apiBase,
    URI webBase,
    // note: the origin MUST NOT end with a trailing slash
    String webOrigin
) {
    public final static int API_VERSION = 1;

    public final static DraaftServices DEFAULT = new DraaftServices(
        URI.create("https://api.disrespec.tech/"),
        URI.create("https://disrespec.tech/draaft/"),
        "https://disrespec.tech"
    );

    public static @Nullable DraaftServices fromJvmArgs() {
        if (System.getProperty("DRAAFT_DEV") != null || FabricLoader.getInstance().isDevelopmentEnvironment()) {
            return new DraaftServices(
                URI.create(System.getProperty("DRAAFT_API_BASE", "http://localhost:8000/")),
                URI.create(System.getProperty("DRAAFT_WEB_BASE", "http://localhost:8080/draaft/")),
                System.getProperty("DRAAFT_WEB_ORIGIN", "http://localhost:8080")
            );
        } else {
            return null;
        }
    }
}
