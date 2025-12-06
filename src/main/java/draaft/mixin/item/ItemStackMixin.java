package draaft.mixin.item;

import draaft.world.EnchantUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract void putSubTag(String key, Tag tag);

    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;I)V", at = @At("TAIL"))
    void ItemStack(ItemConvertible item, int count, CallbackInfo ci) {
        if (!EnchantUtils.levelOneEnchants()) {
            return;
        }

        List<Enchantment> pickaxeshovelEnchants = List.of(Enchantments.EFFICIENCY, Enchantments.FORTUNE, Enchantments.MENDING, Enchantments.UNBREAKING);
        List<Enchantment> axehoeEnchants = List.of(Enchantments.EFFICIENCY, Enchantments.MENDING, Enchantments.SILK_TOUCH, Enchantments.UNBREAKING);
        List<Enchantment> swordEnchants = List.of(Enchantments.FIRE_ASPECT, Enchantments.KNOCKBACK, Enchantments.LOOTING, Enchantments.MENDING, Enchantments.SMITE, Enchantments.SWEEPING, Enchantments.UNBREAKING);
        List<Enchantment> helmetEnchants = List.of(Enchantments.AQUA_AFFINITY, Enchantments.MENDING, Enchantments.PROTECTION, Enchantments.RESPIRATION, Enchantments.UNBREAKING);
        List<Enchantment> chestplateleggingsEnchants = List.of(Enchantments.MENDING, Enchantments.PROTECTION, Enchantments.UNBREAKING);
        List<Enchantment> bootsEnchants = List.of(Enchantments.DEPTH_STRIDER, Enchantments.FEATHER_FALLING, Enchantments.MENDING, Enchantments.PROTECTION, Enchantments.SOUL_SPEED, Enchantments.UNBREAKING);
        List<Enchantment> compassEnchants = List.of(Enchantments.VANISHING_CURSE);
        List<Enchantment> fnsoasEnchants = List.of(Enchantments.MENDING, Enchantments.UNBREAKING);
        List<Enchantment> rodEnchants = List.of(Enchantments.LUCK_OF_THE_SEA, Enchantments.LURE, Enchantments.MENDING, Enchantments.UNBREAKING);
        List<Enchantment> bowEnchants = List.of(Enchantments.FLAME, Enchantments.INFINITY, Enchantments.POWER, Enchantments.PUNCH, Enchantments.UNBREAKING);
        List<Enchantment> tridentEnchants = List.of(Enchantments.CHANNELING, Enchantments.IMPALING, Enchantments.LOYALTY, Enchantments.PUNCH, Enchantments.UNBREAKING);
        List<Enchantment> crossbowEnchants = List.of(Enchantments.PIERCING, Enchantments.MENDING, Enchantments.QUICK_CHARGE, Enchantments.UNBREAKING);
        Map<String, List<Enchantment>> enchantmentMap = Map.ofEntries(Map.entry("pickaxe", pickaxeshovelEnchants), Map.entry("shovel", pickaxeshovelEnchants), Map.entry("_axe", axehoeEnchants), Map.entry("hoe", axehoeEnchants), Map.entry("shears", axehoeEnchants), Map.entry("sword", swordEnchants), Map.entry("helmet", helmetEnchants), Map.entry("chestplate", chestplateleggingsEnchants), Map.entry("leggings", chestplateleggingsEnchants), Map.entry("boots", bootsEnchants), Map.entry("compass", compassEnchants), Map.entry("flint_and_steel", fnsoasEnchants), Map.entry("_on_a_stick", fnsoasEnchants), Map.entry("shield", fnsoasEnchants), Map.entry("elytra", fnsoasEnchants), Map.entry("fishing_rod", rodEnchants), Map.entry("bow", bowEnchants), Map.entry("trident", tridentEnchants), Map.entry("crossbow", crossbowEnchants));

        if (item != null && !item.asItem().getTranslationKey().contains("bowl")) {
            for (String id : enchantmentMap.keySet()) {
                if (item.asItem().getTranslationKey().contains(id)) {
                    ListTag listTag = new ListTag();
                    for (Enchantment enchantment : enchantmentMap.get(id)) {
                        CompoundTag compoundTag = new CompoundTag();
                        compoundTag.putString("id", String.valueOf(Registry.ENCHANTMENT.getId(enchantment)));
                        compoundTag.putShort("lvl", (short) 1);
                        listTag.add(compoundTag);
                    }
                    this.putSubTag("Enchantments", listTag);
                }
            }
        }
    }
}
