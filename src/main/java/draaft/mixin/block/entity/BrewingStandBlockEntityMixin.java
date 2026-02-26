package draaft.mixin.block.entity;

import draaft.draaft;
import draaft.persistent.WorldManifest;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin extends LockableContainerBlockEntity {
    protected BrewingStandBlockEntityMixin(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 400))
    private int injected(int constant) {
        var serverWorld = (ServerWorld) world;

        assert serverWorld != null;

        return WorldManifest.get(serverWorld).on(WorldManifest.Feature.FASTER_BLOCK_ENTITIES)
            ? 1
            : constant;
    }
}
