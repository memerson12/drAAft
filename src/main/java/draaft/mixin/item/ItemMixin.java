package draaft.mixin.item;

import draaft.draaft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin implements ItemConvertible {
    @Inject(method = "hasGlint", at = @At("HEAD"), cancellable = true)
    void hasGlint(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (draaft.ENCHANTED_BUCKET) {
            if (stack.getItem().equals(Items.BUCKET) || stack.getItem().equals(Items.LAVA_BUCKET) || stack.getItem().equals(Items.WATER_BUCKET) || stack.getItem().equals(Items.TROPICAL_FISH_BUCKET) || stack.getItem().equals(Items.PUFFERFISH_BUCKET) || stack.getItem().equals(Items.COD_BUCKET) || stack.getItem().equals(Items.SALMON_BUCKET) || stack.getItem().equals(Items.MILK_BUCKET)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "getRarity", at = @At("HEAD"), cancellable = true)
    void getRarity(ItemStack stack, CallbackInfoReturnable<Rarity> cir) {
        if (draaft.ENCHANTED_BUCKET) {
            if (stack.getItem().equals(Items.BUCKET) || stack.getItem().equals(Items.LAVA_BUCKET) || stack.getItem().equals(Items.WATER_BUCKET) || stack.getItem().equals(Items.TROPICAL_FISH_BUCKET) || stack.getItem().equals(Items.PUFFERFISH_BUCKET) || stack.getItem().equals(Items.COD_BUCKET) || stack.getItem().equals(Items.SALMON_BUCKET) || stack.getItem().equals(Items.MILK_BUCKET)) {
                cir.setReturnValue(Rarity.EPIC);
            }
        }
    }
}
