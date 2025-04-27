package draaft.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.decorator.ChanceDecoratorConfig;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.decorator.LavaLakeDecorator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;
import java.util.stream.Stream;

@Mixin(LavaLakeDecorator.class)
public abstract class LavaLakeDecoratorMixin extends Decorator<ChanceDecoratorConfig> {
    public LavaLakeDecoratorMixin(Codec<ChanceDecoratorConfig> configCodec) {
        super(configCodec);
    }

    /**
     * @author pacmanmvc
     * @reason make lava pools generate more
     */
    @Overwrite
    public Stream<BlockPos> getPositions(WorldAccess worldAccess, ChunkGenerator chunkGenerator, Random random, ChanceDecoratorConfig chanceDecoratorConfig, BlockPos blockPos) {
        if (random.nextInt(chanceDecoratorConfig.chance / 20) == 0) {
            int i = random.nextInt(16) + blockPos.getX();
            int j = random.nextInt(16) + blockPos.getZ();
            int k = random.nextInt(random.nextInt(chunkGenerator.getMaxY() - 8) + 8);
            if (k < worldAccess.getSeaLevel() || random.nextInt(chanceDecoratorConfig.chance / 20) == 0) {
                return Stream.of(new BlockPos(i, k, j));
            }
        }

        return Stream.empty();
    }
}
