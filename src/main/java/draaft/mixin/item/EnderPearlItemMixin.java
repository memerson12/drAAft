package draaft.mixin.item;

import com.llamalad7.mixinextras.sugar.Local;
import draaft.client.DraaftState;
import draaft.persistent.WorldManifest;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnderPearlItem.class)
public class EnderPearlItemMixin {
    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ItemCooldownManager;set(Lnet/minecraft/item/Item;I)V"))
    void noCooldown(ItemCooldownManager instance, Item item, int duration, @Local(argsOnly = true) World world) {
        if (!world.isClient() && WorldManifest.get((ServerWorld) world).on(WorldManifest.Feature.DANGEROUS_PEARLS))
        {
            return;
        }
        instance.set(item, duration);
    }
}
