package draaft.client.models;

public record RoomConfig(
    boolean enforceTimer,
    int pickTime,
    boolean spectatorsGetWorld) {
}
