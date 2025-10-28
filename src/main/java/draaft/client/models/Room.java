package draaft.client.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public record Room(
        @SerializedName("code") String roomCode,
        List<DraaftPlayer> members,
        @SerializedName("admin") DraaftPlayer roomAdmin,
        @SerializedName("config") RoomConfig roomConfig) {
}
