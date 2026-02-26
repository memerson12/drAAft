package draaft.client.models;

import java.util.ArrayList;
import java.util.stream.Collectors;

public record Room(String code, ArrayList<DraaftPlayer> members, DraaftPlayer admin, RoomConfig config) {
    public ArrayList<DraaftPlayer> getNonSpectatorPlayers() {
        return members.stream()
            .filter(draaftPlayer -> !draaftPlayer.isSpectator())
            .collect(Collectors.toCollection(ArrayList::new));
    }
}
