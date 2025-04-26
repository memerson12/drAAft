package draaft.mixin;

import net.minecraft.world.gen.decorator.EndGatewayDecorator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EndGatewayDecorator.class)
public abstract class EndGatewayDecoratorMixin {
    @ModifyConstant(method = "getPositions(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Ljava/util/Random;Lnet/minecraft/world/gen/decorator/NopeDecoratorConfig;Lnet/minecraft/util/math/BlockPos;)Ljava/util/stream/Stream;", constant = @Constant(intValue = 700))
    private int injected(int constant) {
        return 100;
    }
}
