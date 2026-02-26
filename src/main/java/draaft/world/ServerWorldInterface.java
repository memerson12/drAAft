package draaft.world;

import draaft.persistent.WorldManifest;
import org.jetbrains.annotations.Nullable;

public interface ServerWorldInterface {
    @Nullable WorldManifest draaft$getWorldManifest();

    void draaft$setWorldManifest(WorldManifest worldManifest);
}
