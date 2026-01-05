package draaft.mixin.block;

import draaft.persistent.WorldManifest;
import draaft.persistent.WorldState;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

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
        final float DEBRIS_CHANCE = WorldManifest.get((ServerWorld) world).on(WorldManifest.Feature.DEBRIS_RATES) ? 0.2F : 0.005F;

        if (!(world.getDimension().isUltrawarm() && (pos.getY() >= 5 && pos.getY() <= 25))) {
            return;
        }
        if (this.is(Blocks.NETHERRACK) || this.is(Blocks.NETHER_GOLD_ORE) || this.is(Blocks.NETHER_QUARTZ_ORE) || this.is(Blocks.MAGMA_BLOCK) || this.is(Blocks.GRAVEL) || this.is(Blocks.SOUL_SAND) || this.is(Blocks.SOUL_SOIL) || this.is(Blocks.BLACKSTONE)) {
            WorldState worldState = WorldState.getServerState((ServerWorld) world);
            WorldState.RandomState draaftMinedState = worldState.getOrCreateRng(WorldState.RngType.NETHERITE_MINED, (ServerWorld) world);
            int mined = draaftMinedState.incrementUses();

            if (draaftMinedState.getRandom().nextFloat() < DEBRIS_CHANCE || (mined % 200) == 0) {
                placeDebris(pos, player, world);
                draaftMinedState.resetUses();
            }
        }
    }

    @Inject(method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private static void getDroppedStacks(BlockState state, ServerWorld world, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack stack, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (state.getBlock().is(Blocks.DEAD_BUSH)) {
            LootContext.Builder builder = new LootContext.Builder(world)
                .random(WorldState.getServerState(world).getOrCreateRng(WorldState.RngType.DEAD_BUSH, world).getRandom())
                .parameter(LootContextParameters.POSITION, pos)
                .parameter(LootContextParameters.TOOL, stack)
                .optionalParameter(LootContextParameters.THIS_ENTITY, entity)
                .optionalParameter(LootContextParameters.BLOCK_ENTITY, blockEntity);
            cir.setReturnValue(state.getDroppedStacks(builder));
        } else if (state.getBlock().is(Blocks.LAPIS_ORE)) {
            LootContext.Builder builder = new LootContext.Builder(world)
                .random(WorldState.getServerState(world).getOrCreateRng(WorldState.RngType.LAPIS, world).getRandom())
                .parameter(LootContextParameters.POSITION, pos)
                .parameter(LootContextParameters.TOOL, stack)
                .optionalParameter(LootContextParameters.THIS_ENTITY, entity)
                .optionalParameter(LootContextParameters.BLOCK_ENTITY, blockEntity);
            cir.setReturnValue(state.getDroppedStacks(builder));
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
