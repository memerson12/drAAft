package draaft.mixin.block;

import draaft.persistent.WorldState;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockMixin extends AbstractBlock {
    @Shadow
    public abstract boolean is(Block block);

    public BlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "onBreak", at = @At("TAIL"))
    void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo ci) {
        if (world.isClient()) {
            return;
        }
        if (!(world.getDimension().isUltrawarm() && (pos.getY() >= 5 && pos.getY() <= 25))) {
            return;
        }
        if (this.is(Blocks.NETHERRACK) || this.is(Blocks.NETHER_GOLD_ORE) || this.is(Blocks.NETHER_QUARTZ_ORE) || this.is(Blocks.MAGMA_BLOCK) || this.is(Blocks.GRAVEL) || this.is(Blocks.SOUL_SAND) || this.is(Blocks.SOUL_SOIL) || this.is(Blocks.BLACKSTONE)) {
            WorldState worldState = WorldState.getServerState((ServerWorld) world);
            WorldState.RandomState draaftMinedState = worldState.getOrCreateRng(WorldState.RngType.MINED, (ServerWorld) world);
            int mined = draaftMinedState.incrementUses();

            if (draaftMinedState.getRandom().nextFloat() < 0.01F || (mined % 100) == 0) {
                placeDebris(pos, player, world);
                draaftMinedState.resetUses();
            }
        }
    }

    @Unique
    private void placeDebris(BlockPos pos, PlayerEntity player, World world) {
        Direction direction = Direction.getEntityFacingOrder(player)[0];
        BlockPos newPos = pos.offset(direction, 2);
        if (!world.getBlockState(newPos).isAir() && world.getBlockState(newPos).getFluidState().isEmpty()) {
            world.setBlockState(newPos, Blocks.ANCIENT_DEBRIS.getDefaultState());
        }
    }
}
