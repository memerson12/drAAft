package draaft.client.models;

import java.util.ArrayList;

public record Room(String code, ArrayList<DraaftPlayer> members, DraaftPlayer admin, RoomConfig config) {
}
