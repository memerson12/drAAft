package draaft.client.models;

public record RoomConfig(
        boolean enforce_timer,
        int pick_time,
        boolean spectators_get_world) {
}
