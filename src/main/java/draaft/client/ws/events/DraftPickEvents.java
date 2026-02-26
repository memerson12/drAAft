package draaft.client.ws.events;

import java.util.UUID;

public interface DraftPickEvents {
    DraaftPickEventType type();

    enum DraaftPickEventType {
        PICK,
        // We might have more events later e.g. undo pick, gambits, etc.
    }

    record Pick(UUID playerUuid, String pickKey, int pickNumber) implements DraftPickEvents {
        @Override
        public DraaftPickEventType type() {
            return DraaftPickEventType.PICK;
        }
    }
}
