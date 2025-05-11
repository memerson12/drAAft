package draaft.mixin.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Enchantments.class)
public abstract class EnchantmentsMixin {
    @ModifyArg(
            method = "<clinit>",
            slice = @Slice(
                    from = @At(value = "NEW", target = "net/minecraft/enchantment/LoyaltyEnchantment"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/LoyaltyEnchantment;<init>(Lnet/minecraft/enchantment/Enchantment$Rarity;[Lnet/minecraft/entity/EquipmentSlot;)V")
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/enchantment/LoyaltyEnchantment;<init>(Lnet/minecraft/enchantment/Enchantment$Rarity;[Lnet/minecraft/entity/EquipmentSlot;)V"
            )
    )
    private static Enchantment.Rarity loyalty(Enchantment.Rarity weight) {
        return Enchantment.Rarity.RARE;
    }

    @ModifyArg(
            method = "<clinit>",
            slice = @Slice(
                    from = @At(value = "NEW", target = "net/minecraft/enchantment/ChannelingEnchantment"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/ChannelingEnchantment;<init>(Lnet/minecraft/enchantment/Enchantment$Rarity;[Lnet/minecraft/entity/EquipmentSlot;)V")
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/enchantment/ChannelingEnchantment;<init>(Lnet/minecraft/enchantment/Enchantment$Rarity;[Lnet/minecraft/entity/EquipmentSlot;)V"
            )
    )
    private static Enchantment.Rarity channeling(Enchantment.Rarity weight) {
        return Enchantment.Rarity.RARE;
    }
}
