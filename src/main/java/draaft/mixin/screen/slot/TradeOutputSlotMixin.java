package draaft.mixin.screen.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.TradeOutputSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TradeOutputSlot.class)
public abstract class TradeOutputSlotMixin extends Slot {
    @Shadow private int amount;

    public TradeOutputSlotMixin(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public void onCrafted(ItemStack stack) {
        this.amount = 0;
    }
}
