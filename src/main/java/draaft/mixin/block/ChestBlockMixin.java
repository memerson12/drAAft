package draaft.mixin.block;

import draaft.world.PyramidChest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.state.StateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestBlock.class)
public class ChestBlockMixin {
    @Inject(method = "appendProperties", at = @At("TAIL"))
    void appendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(PyramidChest.PYRAMID_CHEST);
    }

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/ChestBlock;setDefaultState(Lnet/minecraft/block/BlockState;)V"))
    BlockState modifyDefaultState(BlockState bs) {
        return bs.with(PyramidChest.PYRAMID_CHEST, false);
    }
}
