package draaft.mixin.world.poi;

import net.minecraft.block.BlockState;
import net.minecraft.world.poi.PointOfInterestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(PointOfInterestType.class)
public interface PointOfInterestTypeAccessor {
    @Invoker("register")
    static PointOfInterestType draaft$register(String id, Set<BlockState> workStationStates, int ticketCount, int searchDistance) {
        throw new AssertionError("unreachable");
    }
}
