package draaft.mixin.screen.slot;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
        if (inventory instanceof PlayerInventory && !(index < 9 || (index > 35 && index < 41))) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getStack", at = @At("HEAD"), cancellable = true)
    void getStack(CallbackInfoReturnable<ItemStack> cir) {
        if (inventory instanceof PlayerInventory && !(index < 9 || (index > 35 && index < 41))) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(method = "doDrawHoveringEffect", at = @At("HEAD"), cancellable = true)
    void doDrawHoveringEffect(CallbackInfoReturnable<Boolean> cir) {
        if (inventory instanceof PlayerInventory && !(index < 9 || (index > 35 && index < 41))) {
            cir.setReturnValue(false);
        }
    }
}
