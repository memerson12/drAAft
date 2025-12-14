package draaft.client.world;

import org.jetbrains.annotations.Nullable;

public interface LevelPropertiesExt {
    @Nullable DraaftLevelMetadata draaft$metadata();

    void draaft$setMetadata(DraaftLevelMetadata metadata);
}
