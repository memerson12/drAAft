package draaft.mixin.entity;

import draaft.persistent.WorldState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EyeOfEnderEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(EyeOfEnderEntity.class)
public abstract class EyeOfEnderEntityMixin extends Entity {
    public EyeOfEnderEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(method = "moveTowards", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I"))
    private int dropsItem(Random instance, int i) {
        ServerWorld world = (ServerWorld) this.getEntityWorld();
        WorldState state = WorldState.getServerState(world);
        WorldState.RandomState draaftEyeState = state.getOrCreateRng(WorldState.RngType.EYE, world);
        Random draaftEyeRng = draaftEyeState.getRandom();
        draaftEyeState.incrementUses();

        return draaftEyeState.getUses() == 2 ? 1 : draaftEyeRng.nextInt(i);
    }
}
