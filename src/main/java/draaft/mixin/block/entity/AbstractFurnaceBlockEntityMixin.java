package draaft.mixin.block.entity;

import draaft.draaft;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin extends LockableContainerBlockEntity {

    protected AbstractFurnaceBlockEntityMixin(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
    }

    @Inject(method = "getCookTime", at = @At("HEAD"), cancellable = true)
    void getCookTime(CallbackInfoReturnable<Integer> cir) {
        if (draaft.FASTER_BLOCK_ENTITIES) cir.setReturnValue(1);
    }
}
