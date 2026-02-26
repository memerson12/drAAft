package draaft.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Lifecycle;
import draaft.client.world.LevelPropertiesExt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.SaveProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @WrapMethod(method = "shouldMonitorTickDuration")
    boolean disablePieChartOnNoF3(Operation<Boolean> original) {
        return !MinecraftClient.getInstance().hasReducedDebugInfo() && original.call();
    }

    @Unique
    private static final String START_INTEGRATED_SERVER = "startIntegratedServer(Ljava/lang/String;Lnet/minecraft/util/registry/RegistryTracker$Modifiable;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function4;ZLnet/minecraft/client/MinecraftClient$WorldLoadAction;)V";

    @WrapOperation(
        method = START_INTEGRATED_SERVER,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/SaveProperties;getLifecycle()Lcom/mojang/serialization/Lifecycle;"
        )
    )
    Lifecycle markDraaftAsStable(SaveProperties instance, Operation<Lifecycle> original) {
        if (instance instanceof LevelPropertiesExt levelProperties && levelProperties.draaft$metadata() != null) {
            return Lifecycle.stable();
        } else {
            return original.call(instance);
        }
    }
}
