package draaft.client.ws.events;

import java.util.UUID;

public interface RoomMemberEvents {
    RoomEventType type();

    enum RoomEventType {
        JOINED,
        LEFT,
        KICK,
        SPECTATOR,
        PLAYER,
        ROOM_STARTED,
    }

    // Common events we expect from the server. These may be extended later.
    record PlayerJoined(UUID playerUuid) implements RoomMemberEvents {
        @Override
        public RoomEventType type() {
            return RoomEventType.JOINED;
        }
    }

    record PlayerLeft(UUID playerUuid) implements RoomMemberEvents {
        @Override
        public RoomEventType type() {
            return RoomEventType.LEFT;
        }
    }

    record PlayerKick(UUID playerUuid) implements RoomMemberEvents {
        @Override
        public RoomEventType type() {
            return RoomEventType.KICK;
        }
    }

    record PlayerBecomeSpectator(UUID playerUuid) implements RoomMemberEvents {
        @Override
        public RoomEventType type() {
            return RoomEventType.SPECTATOR;
        }
    }

    /*
     * Player becomes a regular player from being a spectator.
     */
    record PlayerBecomePlayer(UUID playerUuid) implements RoomMemberEvents {
        @Override
        public RoomEventType type() {
            return RoomEventType.PLAYER;
        }
    }
}

