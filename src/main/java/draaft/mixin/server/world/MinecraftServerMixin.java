package draaft.mixin.server.world;

import draaft.draaft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.MetricsData;
import net.minecraft.util.Unit;
import net.minecraft.util.UserCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.SaveProperties;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Unique
    private static final Logger logger = draaft.LOGGER;

    @Shadow
    public abstract ServerWorld getOverworld();

    @Unique
    private final Set<ChunkPos> temporaryStartTicketPositions = new HashSet<>();
    @Unique
    private int calculatedOptimalTicketLevel;

    @Unique
    private static final int MAX_TICKET_LEVEL = 33; // Maximum allowed ticket level for sub tickets (minecraft hardcodes a max of 33)

    // The level defining the overall desired coverage area
    // (2*level-1)x(2*level-1) chunk area (i.e. a 185x185 chunk area for level 93)
    @Unique
//    private static final int DESIRED_COVERAGE_LEVEL = 93;
    private static final int DESIRED_COVERAGE_LEVEL = 35;

    // The number of chunks that should be loaded to cover the desired area (2*DESIRED_COVERAGE_LEVEL-1)^2
    @Unique
    private static final int DESIRED_CHUNK_COUNT = (2 * DESIRED_COVERAGE_LEVEL - 1) * (2 * DESIRED_COVERAGE_LEVEL - 1);

    @Unique boolean isNewWorld = true;
    @Unique boolean hasCheckedIfNewWorld = false;

    /**
     * Creates multiple smaller chunk tickets to cover a large area instead of using one oversized ticket.
     * <p>
     * Minecraft has a hard limit of 33 for ticket levels, but we want to cover a much larger area.
     * This method calculates the optimal ticket level that will evenly divide our desired coverage area,
     * then places multiple tickets in a grid pattern to exactly tile the entire area without gaps or overlaps.
     * <p>
     * Each ticket of level L creates a (2L-1)×(2L-1) square of loaded chunks centered around the ticket position.
     * By carefully spacing these tickets, we can create a seamless grid covering the full desired area.
     *
     * @param world    The server world where tickets will be added
     * @param spawnPos The world spawn position to center the tickets around
     */
    @Unique
    private void addMultipleStartTickets(ServerWorld world, BlockPos spawnPos) {
        ServerChunkManager chunkManager = world.getChunkManager();
        ChunkPos spawnChunkPos = new ChunkPos(spawnPos);

        // Calculate the side length of the desired coverage area in chunks
        // For a level L ticket, the coverage area is a square with side length (2L-1)
        int desiredCoverageSide = 2 * DESIRED_COVERAGE_LEVEL - 1;

        int optimalTicketLevel = 0;
        int optimalTicketSide = 0;

        // Find the largest ticket level <= MAX_TICKET_LEVEL that evenly divides the desired coverage area
        // This ensures we can tile the area perfectly without gaps or overlaps
        for (int level = MAX_TICKET_LEVEL; level >= 1; level--) {
            int currentSide = 2 * level - 1; // Side length of a ticket with this level
            // Check if this ticket size divides the desired area evenly
            if (currentSide > 0 && currentSide < desiredCoverageSide && desiredCoverageSide % currentSide == 0) {
                optimalTicketLevel = level;
                optimalTicketSide = currentSide;
                break;
            }
        }

        // Fallback to level 1 if no optimal level was found
        // This can happen if the desired coverage side length is prime or has large prime factors
        if (optimalTicketLevel == 0) {
            logger.warn("Could not find a ticket level <= {} that exactly divides a {}x{} area's side length ({}) and is < level {}. Falling back to level 1.",
                    MAX_TICKET_LEVEL, DESIRED_COVERAGE_LEVEL, DESIRED_COVERAGE_LEVEL, desiredCoverageSide, DESIRED_COVERAGE_LEVEL);
            if (desiredCoverageSide > 0) {
                optimalTicketLevel = 1;
                optimalTicketSide = 1;
            } else {
                logger.error("Desired coverage side length is not positive ({})!", desiredCoverageSide);
                return;
            }
        }

        // Calculate how many tickets we need in each direction to cover the desired area
        int numTicketsPerSide = desiredCoverageSide / optimalTicketSide;
        int totalTickets = numTicketsPerSide * numTicketsPerSide;

        logger.info("Creating {} tickets at optimal level {} (side {}) to exactly tile desired area side {} (level {})",
                totalTickets, optimalTicketLevel, optimalTicketSide, desiredCoverageSide, DESIRED_COVERAGE_LEVEL);

        // Spacing between ticket centers equals the side length of each ticket's coverage
        // This ensures tickets perfectly align edge-to-edge without gaps or overlaps
        int spacing = optimalTicketSide;

        // Calculate the total span covered by the centers of the tickets along one axis.
        // This is the number of intervals (numTicketsPerSide - 1) times the spacing.
        int totalSpanOfCenters = (numTicketsPerSide - 1) * spacing;

        // Calculate offsets to center the entire grid of tickets around the spawn point
        // We need to shift the first ticket position by half of the total span in both directions
        int firstTicketOffsetX = totalSpanOfCenters / 2;
        int firstTicketOffsetZ = totalSpanOfCenters / 2;

        // Clear any previous ticket positions and store the calculated optimal level
        temporaryStartTicketPositions.clear();
        calculatedOptimalTicketLevel = optimalTicketLevel;

        // Create the grid of tickets
        for (int x = 0; x < numTicketsPerSide; x++) {
            for (int z = 0; z < numTicketsPerSide; z++) {
                // Calculate the center for this ticket.
                // Start from the spawn chunk, subtract the offset for the first ticket's position,
                // and then add the offset for the current ticket's position (x, z) within the grid.
                int ticketX = spawnChunkPos.x - firstTicketOffsetX + (x * spacing);
                int ticketZ = spawnChunkPos.z - firstTicketOffsetZ + (z * spacing);
                ChunkPos ticketPos = new ChunkPos(ticketX, ticketZ);

                // Add the ticket and remember its position for later cleanup
                chunkManager.addTicket(ChunkTicketType.START, ticketPos, optimalTicketLevel, Unit.INSTANCE);
                temporaryStartTicketPositions.add(ticketPos);

                // Optional logging for each ticket added (can be noisy)
                // logger.info("Added start ticket at {} with level {}", ticketPos, optimalTicketLevel);
            }
        }
    }

    @Unique
    private boolean isNewWorld() {
        if(hasCheckedIfNewWorld) {
            return isNewWorld;
        }
        ServerWorld serverWorld = this.getOverworld();
        ServerChunkManager chunkManager = serverWorld.getChunkManager();
        int loadedCount = chunkManager.getLoadedChunkCount(); // is 0 for existing worlds
        this.hasCheckedIfNewWorld = true;
        this.isNewWorld = loadedCount != 0;
        return this.isNewWorld;
    }

    // Replace the default single START ticket addition with our multi-ticket approach
    @Inject(
            method = "prepareStartRegion",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerChunkManager;addTicket(Lnet/minecraft/server/world/ChunkTicketType;Lnet/minecraft/util/math/ChunkPos;ILjava/lang/Object;)V"
//                    shift = At.Shift.BEFORE
            )
    )
    private void overrideTicketAddition(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci) {
        if (!isNewWorld()) {
            return;
        }
        ServerWorld serverWorld = this.getOverworld();
        BlockPos spawnPos = serverWorld.getSpawnPos();

        addMultipleStartTickets(serverWorld, spawnPos);
    }

    // Modify the amount of chunks the loading screen expects
    @ModifyConstant(
            method = "loadWorld",
            constant = @Constant(intValue = 11)
    )
    private int modifyLoadWorldLevel(int original) {
        return DESIRED_COVERAGE_LEVEL;
    }

    // Adjust how many chunks we wait to load
    @ModifyConstant(
            method = "prepareStartRegion(Lnet/minecraft/server/WorldGenerationProgressListener;)V",
            constant = @Constant(intValue = 441)
    )
    private int modifyExpectedLoadedChunks(int originalCount) {
        if (!isNewWorld()) {
            return originalCount;
        }
        return DESIRED_CHUNK_COUNT;
    }

    // Adjust how many chunks we wait to load
    @ModifyConstant(
            method = "prepareStartRegion",
            constant = @Constant(intValue = 11)
    )
    private int modifyInitialTicket(int originalCount) {
        if (!isNewWorld()) {
            return originalCount;
        }
        return 0;
    }

    // Remove the temporary tickets we added earlier and save the forced chunks
    @Inject(method = "prepareStartRegion", at = @At(value = "TAIL"))
    private void cleanupAndFinalizeTickets(CallbackInfo ci) {
        if (!isNewWorld()) {
            return;
        }
        ServerWorld serverWorld = this.getOverworld();
        BlockPos blockPos = serverWorld.getSpawnPos();
        ServerChunkManager serverChunkManager = serverWorld.getChunkManager();

        logger.info("Removing {} temporary START tickets", temporaryStartTicketPositions.size());

        // Use stored ticket positions to remove tickets at the determined optimal level.
        // The stored positions already include the centering offset calculated during addition.
        for (ChunkPos ticketPos : temporaryStartTicketPositions) {
//            logger.info("\tRemoving temporary start ticket at {}", ticketPos);
            serverChunkManager.removeTicket(ChunkTicketType.START, ticketPos, calculatedOptimalTicketLevel, Unit.INSTANCE);
        }
        temporaryStartTicketPositions.clear();


        logger.info("Adding final spawn chunk ticket at level 11");
        serverChunkManager.addTicket(ChunkTicketType.START, new ChunkPos(blockPos), 11, Unit.INSTANCE);

        logger.info("Saving forced chunks... (may take a while)");
        logger.info("This prevents quitting the game taking a really long time (and I assume it would apply to 5 minute lag as well");
        serverChunkManager.save(true);
        logger.info("done saving forced chunks");
    }
}
