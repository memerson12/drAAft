package draaft.mixin;

import draaft.api.EnderDragonEntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(EnderDragonEntity.class)
public abstract class EnderDragonEntityMixin extends MobEntity implements EnderDragonEntityAccessor {
    protected EnderDragonEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Unique
    public Random draaft_persistentPhaseRng;

    @Override
    public void draaft$setRandom(Random random) {
        this.draaft_persistentPhaseRng = random;
    }

    @Override
    public Random draaft$getRandom() {
        return this.draaft_persistentPhaseRng;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void draaft_onInit(EntityType<EnderDragonEntity> entityType, World world, CallbackInfo ci) {
        MinecraftServer server = world.getServer();
        if (server == null) return;
        long seed = server.getSaveProperties().getGeneratorOptions().getSeed();
        this.draaft_persistentPhaseRng = new Random(seed);
    }
}
