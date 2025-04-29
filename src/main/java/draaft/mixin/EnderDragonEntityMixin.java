package draaft.mixin; // Use your project's package structure

import draaft.api.EnderDragonEntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.MobEntity; // EnderDragonEntity ultimately extends MobEntity
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

/**
 * Mixin to add a persistent, world-seed-based Random instance to the EnderDragonEntity.
 */
@Mixin(EnderDragonEntity.class)
public abstract class EnderDragonEntityMixin extends MobEntity implements EnderDragonEntityAccessor { // Extend MobEntity or appropriate base

    @Unique
    public Random draaft_persistentPhaseRng;

    protected EnderDragonEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void draaft$setRandom(Random random) {
        this.draaft_persistentPhaseRng = random;
    }

    @Override
    public Random draaft$getRandom() {
        return this.draaft_persistentPhaseRng;
    }

    /**
     * Injects into the EnderDragonEntity's constructor right after the superclass constructor call.
     * Initializes the dragon-specific Random instance using the world seed.
     * This ensures the Random object is created once per dragon instance.
     *
     * @param world The world the dragon is spawned in.
     * @param ci    CallbackInfo for the injection.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void draaft_onInit(EntityType<EnderDragonEntity> entityType, World world, CallbackInfo ci) {
        MinecraftServer server = world.getServer();
        if (server == null) return;
        long seed = server.getSaveProperties().getGeneratorOptions().getSeed();
        this.draaft_persistentPhaseRng = new Random(seed);

        // Optional: Add unique factors like dragon UUID if needed
        // this.draaft_persistentPhaseRng = new Random(seed ^ this.getUuid().getLeastSignificantBits());
    }
}
