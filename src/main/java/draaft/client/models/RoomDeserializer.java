package draaft.client.models;

import com.google.gson.*;

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

        // Convert members array of UUID strings to List<DraaftPlayer>
        List<DraaftPlayer> members = new ArrayList<>();
        JsonArray membersArray = jsonObject.getAsJsonArray("members");
        for (JsonElement memberElement : membersArray) {
            String uuid = memberElement.getAsString();

            // Handle placeholder UUIDs
            if ("<PROFILE ID>".equals(uuid)) {
                System.out.println("Warning: API returned placeholder UUID '<PROFILE ID>'. Using test UUID instead.");
                // Use a test UUID for development
                uuid = "00000000-0000-0000-0000-000000000001";
            }

            members.add(new DraaftPlayer(uuid));
        }

        // Convert admin UUID string to DraaftPlayer
        String adminUuid = jsonObject.get("admin").getAsString();

        // Handle placeholder UUIDs
        if ("<PROFILE ID>".equals(adminUuid)) {
            System.out.println("Warning: API returned placeholder admin UUID '<PROFILE ID>'. Using test UUID instead.");
            // Use a test UUID for development
            adminUuid = "00000000-0000-0000-0000-000000000001";
        }

        DraaftPlayer roomAdmin = new DraaftPlayer(adminUuid);

        // Deserialize config object
        JsonObject configObject = jsonObject.getAsJsonObject("config");
        RoomConfig roomConfig = new RoomConfig(
                configObject.get("enforce_timer").getAsBoolean(),
                configObject.get("pick_time").getAsInt(),
                configObject.get("spectators_get_world").getAsBoolean());

        return new Room(roomCode, members, roomAdmin, roomConfig);
    }
}
