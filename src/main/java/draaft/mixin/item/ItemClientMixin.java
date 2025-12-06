package draaft.mixin.item;

import draaft.world.WorldClientInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemClientMixin implements ItemConvertible {
    @Unique
    private static boolean isBucket(Item item) {
        return item.equals(Items.BUCKET)
            || item.equals(Items.LAVA_BUCKET)
            || item.equals(Items.WATER_BUCKET)
            || item.equals(Items.TROPICAL_FISH_BUCKET)
            || item.equals(Items.PUFFERFISH_BUCKET)
            || item.equals(Items.COD_BUCKET)
            || item.equals(Items.SALMON_BUCKET)
            || item.equals(Items.MILK_BUCKET);
    }

    @Inject(method = "hasGlint", at = @At("HEAD"), cancellable = true)
    void hasGlint(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        var world = MinecraftClient.getInstance().world;

        if (world != null && WorldClientInfo.get(world).enchantedBucket()) {
            if (isBucket(stack.getItem())) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "getRarity", at = @At("HEAD"), cancellable = true)
    void getRarity(ItemStack stack, CallbackInfoReturnable<Rarity> cir) {
        var world = MinecraftClient.getInstance().world;

        if (world != null && WorldClientInfo.get(world).enchantedBucket()) {
            if (isBucket(stack.getItem())) {
                cir.setReturnValue(Rarity.EPIC);
            }
        }
    }
}
