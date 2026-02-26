package draaft.mixin.client;

import draaft.draaft;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Unique
    private static final String DEBUG_ENABLED_TARGET = "Lnet/minecraft/client/options/GameOptions;debugEnabled:Z";

    @Inject(method = "onKey", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = DEBUG_ENABLED_TARGET))
    void onOpenF3(CallbackInfo ci) {
        if (MinecraftClient.getInstance().options.debugEnabled) {
            draaft.currentF3Taunt++;
        }
    }
}
