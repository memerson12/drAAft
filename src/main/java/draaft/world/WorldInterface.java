package draaft.world;

import org.jetbrains.annotations.Nullable;

public interface WorldInterface {
    @Nullable WorldClientInfo draaft$clientInfo();

    void draaft$setClientInfo(WorldClientInfo clientInfo);
}
