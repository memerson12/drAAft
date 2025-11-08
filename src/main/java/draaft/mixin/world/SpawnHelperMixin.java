package draaft.mixin.world;

import draaft.draaft;
import draaft.world.PyramidChest;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(SpawnHelper.class)
public abstract class SpawnHelperMixin {
    @Unique
    private static final String SPAWN_ENTITIES_IN_CHUNK_TARGET = "Lnet/minecraft/world/SpawnHelper;spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V";

    @ModifyArg(method = "spawn", at = @At(value = "INVOKE", target = SPAWN_ENTITIES_IN_CHUNK_TARGET), index = 3)
    private static SpawnHelper.Checker addSpawnCondition(SpawnHelper.Checker checker) {
        return (type, pos, chunk) -> {
            if (chunk instanceof WorldChunk worldChunk && worldChunk.getWorld() instanceof ServerWorld world) {
                return checker.test(type, pos, chunk) && PyramidChest.canSpawn(world, pos);
            } else {
                draaft.LOGGER.debug("Unexpected types while spawning");
                return checker.test(type, pos, chunk);
            }
        };
    }
}
