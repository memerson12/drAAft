package draaft.mixin.entity;

import draaft.persistent.WorldState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(TntEntity.class)
public abstract class TntEntityMixin extends Entity {
    public TntEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "explode", at = @At("HEAD"))
    void explode(CallbackInfo ci) {
        if (this.world.getDimension().isUltrawarm() && (this.getBlockPos().getY() >= 10 && this.getBlockPos().getY() <= 20)) {
            ServerWorld world = (ServerWorld) this.getEntityWorld();
            WorldState state = WorldState.getServerState(world);
            WorldState.RandomState draaftTntState = state.getOrCreateRng(WorldState.RngType.TNT, world);
            int tnt = draaftTntState.incrementUses();
            float chance = Math.abs(this.getBlockPos().getY() - 15) < 3 ? 0.12F : 0.1F; // y13-17 12% otherwise 10%, to incentivize mining at correct y-height

            if (draaftTntState.getRandom().nextFloat() < chance || (tnt % 5) == 0) {
                placeDebrisBlob(draaftTntState.getRandom(), this.getBlockPos());
                draaftTntState.resetUses();
            }
        }
    }

    @Unique
    private void placeDebrisBlob(Random random, BlockPos pos) {
        int count = random.nextInt(4);
        if (count > 0) {
            int x = 0, y = 0, z = 0;

            while ((x == 0 && z == 0) && (y == 0 || y == 1)) {
                x = pos.getX() + random.nextInt(6) - random.nextInt(6);
                y = pos.getY() + random.nextInt(6) - random.nextInt(6);
                z = pos.getZ() + random.nextInt(6) - random.nextInt(6);
            }
            BlockPos start = new BlockPos(x, y, z);
            if (placeDebrisBlock(start)) {
                count--;

                while (count > 0) {
                    Direction direction = Direction.random(random);
                    start = start.offset(direction);
                    if (placeDebrisBlock(start)) {
                        count--;
                    }
                }
            }
        }
    }

    @Unique
    private boolean placeDebrisBlock(BlockPos pos) {
        if (this.world.getBlockState(pos).equals(Blocks.ANCIENT_DEBRIS.getDefaultState())) {
            return false;
        }
        if (!this.world.getBlockState(pos).isAir() && this.world.getBlockState(pos).getFluidState().isEmpty()) {
            this.world.setBlockState(pos, Blocks.ANCIENT_DEBRIS.getDefaultState());
            return true;
        }
        return false;
    }
}
