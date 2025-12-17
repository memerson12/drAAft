package draaft.mixin.enchantment;

import draaft.world.EnchantUtils;
import net.minecraft.enchantment.SoulSpeedEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoulSpeedEnchantment.class)
public abstract class SoulSpeedEnchantmentMixin {
    @Inject(method = "getMaxLevel", at = @At("HEAD"), cancellable = true)
    void getMaxLevel(CallbackInfoReturnable<Integer> cir) {
        if (EnchantUtils.levelOneEnchants()) {
            cir.setReturnValue(2);
        }
    }
}
