package draaft.client.models;

import com.google.gson.*;
import draaft.client.Utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RoomDeserializer implements JsonDeserializer<Room> {

    @Override
    public Room deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();

        // Extract room code
        String roomCode = jsonObject.get("code").getAsString();

        // Convert admin UUID string to DraaftPlayer
        String adminUuid = Utils.formatUuid(jsonObject.get("admin").getAsString());
        DraaftPlayer roomAdmin = new DraaftPlayer(adminUuid);

        // Convert members array of UUID strings to List<DraaftPlayer>
        List<DraaftPlayer> members = new ArrayList<>();
        JsonArray membersArray = jsonObject.getAsJsonArray("members");
        for (JsonElement memberElement : membersArray) {
            String uuid = Utils.formatUuid(memberElement.getAsString());
            if (uuid.equals(adminUuid)) {
                members.add(roomAdmin);
            } else {
                members.add(new DraaftPlayer(uuid));
            }
        }

        // Deserialize config object
        JsonObject configObject = jsonObject.getAsJsonObject("config");
        RoomConfig roomConfig = new RoomConfig(
                configObject.get("enforce_timer").getAsBoolean(),
                configObject.get("pick_time").getAsInt(),
                configObject.get("spectators_get_world").getAsBoolean());

        return new Room(roomCode, members, roomAdmin, roomConfig);
    }
}
