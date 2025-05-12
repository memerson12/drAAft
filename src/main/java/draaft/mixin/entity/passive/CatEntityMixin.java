package draaft.mixin.entity.passive;

import draaft.persistent.WorldState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(CatEntity.class)
public abstract class CatEntityMixin extends TameableEntity {
    protected CatEntityMixin(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "initialize", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I"))
    private int redirectCatType(Random instance, int i) {
        instance.setSeed(getBlockSeed(instance, this.world.getServer().getSaveProperties().getGeneratorOptions().getSeed(), this.getBlockPos().getX(), this.getBlockPos().getZ()));
        return instance.nextInt(i);
    }

    @Redirect(method = "interactMob", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I"))
    private int redirectCatTame(Random instance, int i) {
        ServerWorld world = (ServerWorld) this.getEntityWorld();
        WorldState state = WorldState.getServerState(world);
        Random draaftCatRng = state.getOrCreateCatRng(world);

        return draaftCatRng.nextInt(i);
    }

    @Unique
    private static long getBlockSeed(Random random, long seed, int x, int z) {
        random.setSeed(seed);
        long l = random.nextLong() | 1L;
        long m = random.nextLong() | 1L;
        return (long) x * l + (long) z * m ^ seed;
    }
}
