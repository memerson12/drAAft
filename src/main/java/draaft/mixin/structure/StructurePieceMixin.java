package draaft.mixin.structure;

import draaft.world.PyramidChest;
import net.minecraft.block.BlockState;
import net.minecraft.loot.LootTables;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(StructurePiece.class)
public abstract class StructurePieceMixin {
    @Unique
    private static final String ADD_CHEST_TARGET = "addChest(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockBox;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Identifier;Lnet/minecraft/block/BlockState;)Z";

    @Unique
    private static final String SET_BLOCK_STATE_TARGET = "Lnet/minecraft/world/WorldAccess;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z";

    @Redirect(method = ADD_CHEST_TARGET, at = @At(value = "INVOKE", target = SET_BLOCK_STATE_TARGET))
    boolean modifyBlockState(
        WorldAccess instance, BlockPos blockPos, BlockState blockState, int i,
        WorldAccess world, BlockBox boundingBox, Random random, BlockPos pos, Identifier lootTableId, @Nullable BlockState block
    ) {
        if (LootTables.DESERT_PYRAMID_CHEST.equals(lootTableId)) {
            return instance.setBlockState(blockPos, blockState.with(PyramidChest.PYRAMID_CHEST, true), i);
        } else {
            return instance.setBlockState(blockPos, blockState, i);
        }
    }
}
