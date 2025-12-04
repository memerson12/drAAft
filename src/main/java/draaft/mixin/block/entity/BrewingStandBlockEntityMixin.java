package draaft.mixin.block.entity;

import draaft.draaft;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
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
        if (draaft.FASTER_BLOCK_ENTITIES) return 1;
        return constant;
    }
}
