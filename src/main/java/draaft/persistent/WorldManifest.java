package draaft.persistent;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import draaft.mixin.server.MinecraftServerAccessor;
import draaft.world.ServerWorldInterface;
import draaft.world.WorldClientInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.Resource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.List;

import static draaft.draaft.MOD_ID;

@SuppressWarnings({"FieldMayBeFinal", "MismatchedQueryAndUpdateOfCollection"}) // GSON
public class WorldManifest {
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    private static final Gson GSON = new Gson();

    private static final Identifier IDENTIFIER = new Identifier("draaftpack", "world-manifest.json");

    private EnumSet<Feature> features = EnumSet.noneOf(Feature.class);

    private final Annotations annotations = new Annotations();

    public Boolean on(Feature feature) {
        return this.features.contains(feature);
    }

    public WorldClientInfo toClientInfo() {
        return new WorldClientInfo(
            this.on(Feature.ENCHANTED_BUCKET),
            this.on(Feature.SHOW_COORDS),
            this.annotations
        );
    }

    public static WorldManifest get(ServerWorld serverWorld) {
        var world = serverWorld.getServer().getOverworld();

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            return new WorldManifest();
        }

        var swi = (ServerWorldInterface) world;

        if (swi.draaft$getWorldManifest() == null) {
            swi.draaft$setWorldManifest(loadFromDataPack(world.getServer()));
        }

        return swi.draaft$getWorldManifest();
    }

    private static WorldManifest loadFromDataPack(MinecraftServer server) {
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

        @SerializedName("ShowCoords")
        SHOW_COORDS,

        @SerializedName("DebrisRates")
        DEBRIS_RATES,

        @SerializedName("DangerousPearls")
        DANGEROUS_PEARLS,

        @SerializedName("NoInventory")
        NO_INVENTORY,
    }

    public static class Annotations {
        @SerializedName("mushroom_island")
        @Nullable public String mushroomIsland;
        @SerializedName("jungle")
        @Nullable public String jungle;
        @SerializedName("mega_taiga")
        @Nullable public String megaTaiga;
        @SerializedName("snowy")
        @Nullable public String snowy;
        @SerializedName("badlands")
        @Nullable public String badlands;
        @SerializedName("bastion")
        @Nullable public String bastion;
        @SerializedName("fortress")
        @Nullable public String fortress;
        @SerializedName("strongholds")
        @Nullable public List<String> strongholds;
    }
}
