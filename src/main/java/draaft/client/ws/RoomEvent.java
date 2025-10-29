package draaft.client.ws;

import com.google.gson.JsonObject;

public interface RoomEvent {
    String type();

    enum RoomEventType {
        JOINED,
        LEFT,
        KICK,
        SPECTATOR,
        PLAYER,
        ROOM_STARTED,
        RAW
    }

    /**
     * Fallback raw event if a typed mapping is not available.
     */
    record Raw(String type, JsonObject payload) implements RoomEvent {
    }

    // Common events we expect from the server. These may be extended later.
    record PlayerJoined(String playerUuid) implements RoomEvent {
        @Override
        public String type() {
            return "joined";
        }
    }

    record PlayerLeft(String playerUuid) implements RoomEvent {
        @Override
        public String type() {
            return "left";
        }
    }

    record PlayerKick(String playerUuid) implements RoomEvent {
        @Override
        public String type() {
            return "kick";
        }
    }

    record PlayerBecomeSpectator(String playerUuid) implements RoomEvent {
        @Override
        public String type() {
            return "spectator";
        }
    }

    /*
     * Player becomes a regular player from being a spectator.
     */
    record PlayerBecomePlayer(String playerUuid) implements RoomEvent {
        @Override
        public String type() {
            return "player";
        }
    }

    record RoomStarted(String roomCode) implements RoomEvent {
        @Override
        public String type() {
            return "room_started";
        }
    }
}

