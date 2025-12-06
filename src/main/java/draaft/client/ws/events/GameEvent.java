package draaft.client.ws.events;

import java.util.UUID;

public interface GameEvent {
    GameEventType type();

    enum GameEventType {
        ADVANCEMENTCOUNT,
    }

    record AdvancementCount(UUID playerUuid, int count) implements GameEvent {
        @Override
        public GameEventType type() {
            return GameEventType.ADVANCEMENTCOUNT;
        }
    }
}
