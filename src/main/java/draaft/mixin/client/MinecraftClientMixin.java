package draaft.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @WrapMethod(method = "shouldMonitorTickDuration")
    boolean disablePieChartOnNoF3(Operation<Boolean> original) {
        return !MinecraftClient.getInstance().hasReducedDebugInfo() && original.call();
    }
}
