package draaft.client;

import draaft.draaft;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

public record DraaftServices(
    URI apiBase,
    URI webBase,
    // note: the origin MUST NOT end with a trailing slash
    String webOrigin,
    @Nullable String githubRepo,
    boolean supportsDevAuth
) {
    public final static int API_VERSION = 1;

    public final static DraaftServices DEFAULT = new DraaftServices(
        URI.create("https://api.disrespec.tech/"),
        URI.create("https://disrespec.tech/draaft/"),
        "https://disrespec.tech",
        "memerson12/drAAft",
        false
    );

    public static @Nullable DraaftServices fromJvmArgs() {
        if (draaft.IS_DEBUG) {
            return new DraaftServices(
                URI.create(System.getProperty("DRAAFT_API_BASE", "http://localhost:8000/")),
                URI.create(System.getProperty("DRAAFT_WEB_BASE", "http://localhost:8080/draaft/")),
                System.getProperty("DRAAFT_WEB_ORIGIN", "http://localhost:8080"),
                System.getProperty("DRAAFT_GH_REPO"),
                true
            );
        } else {
            return null;
        }
    }
}
