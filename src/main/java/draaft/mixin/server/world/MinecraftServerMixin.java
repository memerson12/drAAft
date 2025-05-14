package draaft.mixin.server.world;

import draaft.draaft;
import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.ForcedChunkState;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.Map;
import java.util.Set;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Unique
    private static final Logger logger = draaft.LOGGER;

    @Shadow
    public abstract ServerWorld getOverworld();

    @Shadow @Final private Map<RegistryKey<World>, ServerWorld> worlds;

    @Inject(method = "loadWorld", at = @At("TAIL"))
    private void onWorldLoad(CallbackInfo ci) {
        ServerWorld serverWorld = getOverworld();
        ServerChunkManager serverChunkManager = serverWorld.getChunkManager();
        int loadedChunks = serverChunkManager.getTotalChunksLoadedCount();
        int loadedChunks2 = serverChunkManager.getLoadedChunkCount();
        logger.info("{} chunks are currently loaded", loadedChunks);
        logger.info("{} total chunks loaded", loadedChunks2);

//        logger.info("Generating Extra Chunks");
//        MinecraftServer server = (MinecraftServer) (Object) this;
//        ServerWorld overworld = getOverworld();
//
//        if (overworld == null) return;
//
//        BlockPos spawnPos = overworld.getSpawnPos();
//        ChunkPos centerChunk = new ChunkPos(spawnPos);
//
//        int extraRadius = 5; // Loads 11x11 chunks (does not affect spawn chunk radius)
//
//        for (int dx = -extraRadius; dx <= extraRadius; dx++) {
//            for (int dz = -extraRadius; dz <= extraRadius; dz++) {
//                ChunkPos chunkPos = new ChunkPos(centerChunk.x + dx, centerChunk.z + dz);
//
//                logger.info("\tGenerating Extra Chunk at {}", chunkPos.toString());
//                // Force-load chunk synchronously
//                overworld.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, true);
//            }
//        }
    }

    // New ticket level for the start region. Original is 11.
    // A level L results in a (2L-1)x(2L-1) square of chunks.
    @Unique
    private static final int NEW_START_REGION_TICKET_LEVEL = 93; // Example: 16 for a 31x31 chunk area
//    private static final int NEW_START_REGION_TICKET_LEVEL = 11; // Example: 16 for a 31x31 chunk area

    // Calculate the corresponding number of chunks for the new ticket level.
    // This is (2 * NEW_START_REGION_TICKET_LEVEL - 1)^2.
    @Unique
    private static final int NEW_EXPECTED_CHUNK_COUNT = (2 * NEW_START_REGION_TICKET_LEVEL - 1) * (2 * NEW_START_REGION_TICKET_LEVEL - 1); // e.g., 31*31 = 961


//    @ModifyConstant(
//            method = "prepareStartRegion(Lnet/minecraft/server/WorldGenerationProgressListener;)V",
//            constant = @Constant(intValue = 11)
//    )
//    private int modifyStartTicketLevel(int originalLevel) {
//        return NEW_START_REGION_TICKET_LEVEL;
//    }

    @ModifyConstant(
            method = "loadWorld",
            constant = @Constant(intValue = 11)
    )
    private int modifyLoadWorld(int original) {
        return NEW_START_REGION_TICKET_LEVEL;
    }

    /**
     * Modifies the expected number of loaded chunks in the while loop condition.
     * This must match the new ticket level: (2 * NEW_TICKET_LEVEL - 1)^2.
     */
    @ModifyConstant(
            method = "prepareStartRegion(Lnet/minecraft/server/WorldGenerationProgressListener;)V",
            constant = @Constant(intValue = 441)
    )
    private int modifyExpectedLoadedChunks(int originalCount) {
//        logger.info("Going to load {} chunks", NEW_EXPECTED_CHUNK_COUNT);
        return NEW_EXPECTED_CHUNK_COUNT;
    }

    @Inject(method = "prepareStartRegion", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkManager;addTicket(Lnet/minecraft/server/world/ChunkTicketType;Lnet/minecraft/util/math/ChunkPos;ILjava/lang/Object;)V"))
    private void addNewTicker(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci) {
        ServerWorld serverWorld = this.getOverworld();
        BlockPos blockPos = serverWorld.getSpawnPos();
        ServerChunkManager serverChunkManager = serverWorld.getChunkManager();
        ChunkTicketManager ticketManager = serverChunkManager.threadedAnvilChunkStorage.getTicketManager();
        ChunkPos chunkPos = new ChunkPos(blockPos);
//        serverChunkManager.addTicket(ChunkTicketType.START, new ChunkPos(blockPos), 11, Unit.INSTANCE);
        ticketManager.addTicket(ChunkTicketType.field_14032, chunkPos, NEW_START_REGION_TICKET_LEVEL, chunkPos);

    }

    @Inject(method = "prepareStartRegion", at = @At(value = "TAIL"))
    private void test(CallbackInfo ci) {
        ServerWorld serverWorld = this.getOverworld();
        BlockPos blockPos = serverWorld.getSpawnPos();
        ServerChunkManager serverChunkManager = serverWorld.getChunkManager();
//        logger.info("Removing temp ticket");
//        serverChunkManager.removeTicket(ChunkTicketType.START, new ChunkPos(blockPos), NEW_START_REGION_TICKET_LEVEL, Unit.INSTANCE);
//        logger.info("Adding spawn chunk ticket");
//        serverChunkManager.addTicket(ChunkTicketType.START, new ChunkPos(blockPos), 11, Unit.INSTANCE);

        for (ServerWorld serverWorld2 : worlds.values()) {
            int count = 0;
            ForcedChunkState forcedChunkState = serverWorld2.getPersistentStateManager().get(ForcedChunkState::new, "chunks");
            if (forcedChunkState != null) {
                LongIterator longIterator = forcedChunkState.getChunks().iterator();

                while (longIterator.hasNext()) {
                    longIterator.nextLong();
                    count++;
                }
            }
            logger.info("{} chunks forced for world {}", count, serverWorld.getRegistryKey());
        }
    }

//    @Unique
//    private int debugCount = 0;
//
//    @Inject(method = "tick", at = @At(value = "TAIL"))
//    private void tick(CallbackInfo ci) {
//        ServerWorld serverWorld = getOverworld();
//        if (debugCount == 20) {
//            ServerChunkManager serverChunkManager = serverWorld.getChunkManager();
//            int loadedChunks = serverChunkManager.getTotalChunksLoadedCount();
//            int loadedChunks2 = serverChunkManager.getLoadedChunkCount();
////            serverChunkManager.threadedAnvilChunkStorage
//            logger.info("{} chunks are currently loaded", loadedChunks);
//            logger.info("{} total chunks loaded", loadedChunks2);
//            debugCount = 0;
//        }
//        debugCount++;
//    }

}
