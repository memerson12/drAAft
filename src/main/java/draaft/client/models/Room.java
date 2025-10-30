package draaft.client.models;

import java.util.List;

public record Room(String code, List<DraaftPlayer> members, DraaftPlayer admin, RoomConfig config) {
}
