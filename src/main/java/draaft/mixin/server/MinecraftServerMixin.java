package draaft.mixin.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Random;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Redirect(method = "setupSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/source/BiomeSource;locateBiome(IIIILjava/util/List;Ljava/util/Random;)Lnet/minecraft/util/math/BlockPos;"))
    @Nullable
    private static BlockPos getSpawnPos(BiomeSource instance, int startX, int startY, int startZ, int radius, List<Biome> biomes, Random random) {
        BlockPos blockPos = null;
        int matches = 0;
        for (int offsetZ = -32; offsetZ <= 64; offsetZ++) {
            for (int offsetX = -32; offsetX <= 64; offsetX++) {
                if (biomes.contains(instance.getBiomeForNoiseGen(offsetX, startY >> 2, offsetZ))) {
                    int distanceFactor = ((offsetX >> 2) * (offsetX >> 2)) + ((offsetZ >> 2) * (offsetZ >> 2));
                    if (blockPos == null || ((random.nextInt(distanceFactor + 1) == 0) && (random.nextInt(matches + 1) == 0))) {
                        blockPos = new BlockPos(offsetX << 2, startY, offsetZ << 2);
                        matches++;
                    }
                }
            }
        }
        return blockPos;
    }
}
