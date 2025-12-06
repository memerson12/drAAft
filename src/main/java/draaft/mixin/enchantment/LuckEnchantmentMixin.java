package draaft.mixin.enchantment;

import draaft.draaft;
import draaft.world.EnchantUtils;
import net.minecraft.enchantment.LuckEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LuckEnchantment.class)
public abstract class LuckEnchantmentMixin {
    @Inject(method = "getMaxLevel", at = @At("HEAD"), cancellable = true)
    void getMaxLevel(CallbackInfoReturnable<Integer> cir) {
        if (EnchantUtils.levelOneEnchants()) {
            cir.setReturnValue(1);
        }
    }
}
