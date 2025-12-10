package draaft.mixin.screen.slot;

import draaft.persistent.WorldManifest;
import draaft.world.MinecraftClientWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {
    @Shadow
    @Final
    public Inventory inventory;

    @Shadow
    @Final
    private int index;

    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    void canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (noInventory() && inventory instanceof PlayerInventory && !(index < 9 || (index > 35 && index < 41))) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getStack", at = @At("HEAD"), cancellable = true)
    void getStack(CallbackInfoReturnable<ItemStack> cir) {
        if (noInventory() && inventory instanceof PlayerInventory && !(index < 9 || (index > 35 && index < 41))) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "doDrawHoveringEffect", at = @At("HEAD"), cancellable = true)
    void doDrawHoveringEffect(CallbackInfoReturnable<Boolean> cir) {
        if (noInventory() && inventory instanceof PlayerInventory && !(index < 9 || (index > 35 && index < 41))) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private static boolean noInventory() {
        if (
            FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
                && MinecraftClientWrapper.available()
                && MinecraftClientWrapper.getWorld() != null
        ) {
            var world = MinecraftClientWrapper.getWorld();

            return WorldManifest.get(world).on(WorldManifest.Feature.NO_INVENTORY);
        } else {
            return false;
        }
    }
}
