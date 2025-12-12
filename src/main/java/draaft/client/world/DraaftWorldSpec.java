package draaft.client.world;

import com.google.gson.JsonElement;
import dev.menx.worldimporter.RegionId;

import java.nio.file.Path;

public record DraaftWorldSpec(
    String room,
    String worldId,
    JsonElement worldGenSettings,
    RegionId[] regions,
    Path datapack
) {
    public static DraaftWorldSpec fromJson(JsonElement json, RegionId[] regions, Path datapack) {
        var jsonObj = json.getAsJsonObject();

        return new DraaftWorldSpec(
            jsonObj.get("room").getAsString(),
            jsonObj.get("worldId").getAsString(),
            jsonObj.get("worldGenSettings"),
            regions,
            datapack
        );
    }
}
