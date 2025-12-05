package draaft.persistent;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import draaft.mixin.server.MinecraftServerAccessor;
import draaft.world.WorldClientInfo;
import net.minecraft.resource.Resource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.EnumSet;

import static draaft.draaft.MOD_ID;

@SuppressWarnings({"FieldMayBeFinal", "MismatchedQueryAndUpdateOfCollection"}) // GSON
public class WorldManifest {
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    private static final Gson GSON = new Gson();

    private static final Identifier IDENTIFIER = new Identifier("draaftpack", "world-manifest.json");

    private EnumSet<Feature> features = EnumSet.noneOf(Feature.class);

    public Boolean on(Feature feature) {
        return this.features.contains(feature);
    }

    public WorldClientInfo toClientInfo() {
        return new WorldClientInfo(this.on(Feature.ENCHANTED_BUCKET));
    }

    public static WorldManifest get(MinecraftServer server) {
        //noinspection resource
        var resxManager = ((MinecraftServerAccessor) server)
            .draaft$serverResourceManager()
            .getResourceManager();

        if (!resxManager.containsResource(IDENTIFIER)) {
            return new WorldManifest();
        } else {
            Resource resource;
            try {
                resource = resxManager.getResource(IDENTIFIER);
            } catch (IOException e) {
                LOGGER.error("failed to read the world manifest", e);

                return new WorldManifest();
            }

            return GSON.fromJson(new InputStreamReader(resource.getInputStream()), WorldManifest.class);
        }
    }

    public enum Feature {
        @SerializedName("EnchantedBucket")
        ENCHANTED_BUCKET,

        @SerializedName("FasterBlockEntities")
        FASTER_BLOCK_ENTITIES,

        @SerializedName("AllEnchanted")
        LEVEL_ONE_ENCHANTS,

        @SerializedName("NoInventory")
        NO_INVENTORY,
    }
}
