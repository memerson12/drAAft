package draaft.client.ws.events;

import draaft.client.models.RoomConfig;

public interface RoomStateEvents {
    RoomEventType type();

    enum RoomEventType {
        CLOSED,
        CONFIG,
        COMMENCED,
    }

    record closed() implements RoomStateEvents {
        @Override
        public RoomEventType type() {
            return RoomEventType.CLOSED;
        }
    }

    record configUpdate(RoomConfig config) implements RoomStateEvents {
        @Override
        public RoomEventType type() {
            return RoomEventType.CONFIG;
        }
    }

    record commenced() implements RoomStateEvents {
        @Override
        public RoomEventType type() {
            return RoomEventType.COMMENCED;
        }
    }
}

