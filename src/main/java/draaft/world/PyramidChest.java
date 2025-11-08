package draaft.world;

import draaft.mixin.world.poi.PointOfInterestTypeAccessor;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterestStorage;

import java.util.stream.Collectors;

public class PyramidChest {
    private static final String POI_ID = "pyramid_chest";

    public static final BooleanProperty PYRAMID_CHEST = BooleanProperty.of(POI_ID);

    public static void registerPoi() {
        var states = Blocks.CHEST.getStateManager().getStates()
            .stream()
            .filter(state -> state.get(PYRAMID_CHEST))
            .collect(Collectors.toSet());

        PointOfInterestTypeAccessor.draaft$register(POI_ID, states, 0, 0);
    }

    public static boolean canSpawn(ServerWorld world, BlockPos blockPos) {
        // chests always generate at y=53
        if (blockPos.getY() < 50 || blockPos.getY() > 54) {
            return true;
        }

        return world
            .getPointOfInterestStorage()
            .getInSquare(poi -> poi.toString().equals(POI_ID), blockPos, 8, PointOfInterestStorage.OccupationStatus.ANY)
            .noneMatch(poi -> true);
    }
}
