package draaft.client.world;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.JsonOps;
import dev.menx.worldimporter.*;
import dev.menx.worldimporter.net.WorldServerConnection;
import draaft.client.ServerClient;
import draaft.client.gui.DraaftToast;
import draaft.compat.ModCompat;
import draaft.compat.speedrunigt.SpeedrunIGTCompat;
import draaft.draaft;
import draaft.mixin.client.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SaveLevelScreen;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.FileNameUtil;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryTracker;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Function;

public abstract class Worlds {
    @SuppressWarnings("LoggerInitializedWithForeignClass")
    private final static Logger MC_LOGGER = LogManager.getLogger(MinecraftClient.class);
    private final static Logger LOGGER = LogManager.getLogger(draaft.MOD_ID);

    public static void create(DraaftWorldSpec worldSpec, String roomCode, List<String> playerNames) {
        var client = MinecraftClient.getInstance();

        var savesDir = client.getLevelStorage().getSavesDirectory();

        var wiManifest = new Manifest(
            "drAAft",
            new WorldSpec(
                worldSpec.worldId(),
                ServerClient.GSON.fromJson(worldSpec.worldGenSettings(), JsonElement.class),
                worldSpec.regions()
            )
        );

        var draaftMetadata = new DraaftLevelMetadata(roomCode, worldSpec.worldId(), playerNames);

        try {
            var wsc = WorldServerConnection.fromManifest(wiManifest, new DraaftRegionServer());

            var regionServer = new DraaftRegionServer();

            var regionOrder = new RegionOrder(worldSpec.regions());

            var currentWorld = client.world;
            if (currentWorld != null) {
                currentWorld.disconnect();
            }

            if (client.isInSingleplayer()) {
                client.disconnect(new SaveLevelScreen(new TranslatableText("menu.savingLevel")));
            } else {
                client.disconnect();
            }

            var genOpts = wsc.generatorOptions(new RegionManager(wsc, regionOrder, regionServer));

            var worldName = String.format("%s - %s", wsc.title().asString(), wsc.abbrevWorldId());

            var levelInfo = new LevelInfo(
                worldName,
                GameMode.SURVIVAL,
                false,
                Difficulty.EASY,
                false,
                new GameRules(),
                new DataPackSettings(List.of("vanilla", "draaftpack"), List.of())
            );

            var saveDirName = FileNameUtil.getNextUniqueName(savesDir, levelInfo.getLevelName(), "");

            var datapacksDir = savesDir.resolve(saveDirName).resolve("datapacks");

            Files.createDirectories(datapacksDir);

            Files.copy(worldSpec.datapack(), datapacksDir.resolve("draaftpack.zip"));

            createDraaftWorld(saveDirName, levelInfo, genOpts, draaftMetadata);
        } catch (WorldImporterException | IOException e) {
            // TODO(me-nx): unregister the client / signal failure to the draaft server?
            LOGGER.error("failed to create a draaft world", e);
            DraaftToast.showError(new TranslatableText("draaft.preparing.worldCreationError"), Text.of(e.toString()));
        }
    }

    private static void createDraaftWorld(
        String saveDir,
        LevelInfo levelInfo,
        GeneratorOptions generatorOptions,
        DraaftLevelMetadata draaftLevelMetadata
    ) {
        var registryTracker = RegistryTracker.create();

        if (ModCompat.hasSpeedrunIGT()) {
            SpeedrunIGTCompat.initializeTimer(
                saveDir,
                levelInfo.getGameMode(),
                levelInfo.areCommandsAllowed(),
                levelInfo.getDifficulty()
            );
        }

        startIntegratedServer(
            saveDir,
            registryTracker,
            session -> levelInfo.getDatapackSettings(),
            (session, modifiable, resourceManager, dataPackSettings) -> {
                var registryOps = RegistryOps.of(JsonOps.INSTANCE, resourceManager, registryTracker);

                var dimOptsResult = registryOps.loadToRegistry(
                    generatorOptions.getDimensionMap(), Registry.DIMENSION_OPTIONS, DimensionOptions.CODEC
                );

                var dimOpts = dimOptsResult.resultOrPartial(MC_LOGGER::error).orElse(generatorOptions.getDimensionMap());

                var levelProps = new LevelProperties(levelInfo, generatorOptions.withDimensions(dimOpts), dimOptsResult.lifecycle());

                ((LevelPropertiesExt) levelProps).draaft$setMetadata(draaftLevelMetadata);

                return levelProps;
            },
            false,
            MinecraftClient.WorldLoadAction.CREATE
        );
    }

    private static void startIntegratedServer(
        String saveDir,
        RegistryTracker.Modifiable registryTracker,
        Function<LevelStorage.Session, DataPackSettings> datapackSettingsConsumer,
        Function4<LevelStorage.Session, RegistryTracker.Modifiable, ResourceManager, DataPackSettings, SaveProperties> savePropertiesConsumer,
        boolean safeMode,
        MinecraftClient.WorldLoadAction worldLoadAction
    ) {
        ((MinecraftClientAccessor) MinecraftClient.getInstance()).draaft$startIntegratedServer(
            saveDir,
            registryTracker,
            datapackSettingsConsumer,
            savePropertiesConsumer,
            safeMode,
            worldLoadAction
        );
    }
}
