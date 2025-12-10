package draaft.mixin.entity.player;

import draaft.persistent.WorldManifest;
import draaft.world.MinecraftClientWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Shadow
    @Final
    public DefaultedList<ItemStack> main;

    @Shadow
    @Final
    public DefaultedList<ItemStack> armor;

    @Shadow
    @Final
    public DefaultedList<ItemStack> offHand;

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 36))
    int setInventorySize(int constant) {
        return noInventory() ? 9 : 36;
    }

    @ModifyVariable(method = "setStack", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    int setSlotIndex(int slot) {
        if (!noInventory() || slot < 9) {
            return slot;
        } else if (slot > 35 && slot < 41) {
            return slot - 27;
        }

        return -1;
    }

    @Inject(method = "setStack", at = @At("HEAD"), cancellable = true)
    void setStack(int slot, ItemStack stack, CallbackInfo ci) {
        if (noInventory() && slot < 0) {
            ci.cancel();
        }
    }

    @Inject(method = "size", at = @At("HEAD"), cancellable = true)
    void size(CallbackInfoReturnable<Integer> cir) {
        if (noInventory()) {
            cir.setReturnValue(14);
        } else {
            cir.setReturnValue(this.main.size() + this.armor.size() + this.offHand.size());
        }
    }

    @ModifyVariable(method = "removeStack(I)Lnet/minecraft/item/ItemStack;", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    int setSlotIndex2(int slot) {
        if (!noInventory() || slot < 9) {
            return slot;
        } else if (slot > 35 && slot < 41) {
            return slot - 27;
        }

        return -1;
    }

    @Inject(method = "removeStack(I)Lnet/minecraft/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    void removeStack(int slot, CallbackInfoReturnable<ItemStack> cir) {
        if (noInventory() && slot < 0) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @ModifyVariable(method = "removeStack(II)Lnet/minecraft/item/ItemStack;", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    int setSlotIndex3(int slot) {
        if (!noInventory() || slot < 9) {
            return slot;
        } else if (slot > 35 && slot < 41) {
            return slot - 27;
        }

        return -1;
    }

    @Inject(method = "removeStack(II)Lnet/minecraft/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    void removeStack2(int slot, int amount, CallbackInfoReturnable<ItemStack> cir) {
        if (noInventory() && slot < 0) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @ModifyVariable(method = "getStack", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    int setSlotIndex4(int slot) {
        if (!noInventory() || slot < 9) {
            return slot;
        } else if (slot > 35 && slot < 41) {
            return slot - 27;
        }

        return -1;
    }

    @Inject(method = "getStack", at = @At("HEAD"), cancellable = true)
    void getStack(int slot, CallbackInfoReturnable<ItemStack> cir) {
        if (noInventory() && slot < 0) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Redirect(method = "clone", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;setStack(ILnet/minecraft/item/ItemStack;)V"))
    void setCloneIndex(PlayerInventory instance, int slot, ItemStack stack) {
        if (noInventory() && slot > 8 && slot < 14) {
            slot = slot + 27;
        }
        instance.setStack(slot, stack);
    }

    @Redirect(method = "clone", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;getStack(I)Lnet/minecraft/item/ItemStack;"))
    ItemStack setCloneIndex2(PlayerInventory instance, int slot) {
        if (noInventory() && slot > 8 && slot < 14) {
            slot = slot + 27;
        }
        return instance.getStack(slot);
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
