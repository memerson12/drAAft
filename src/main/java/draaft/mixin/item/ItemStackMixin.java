package draaft.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import draaft.world.EnchantUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Lazy;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Unique private final static Lazy<List<Enchantment>> pickaxeshovelEnchants = new Lazy<>(() -> List.of(Enchantments.EFFICIENCY, Enchantments.FORTUNE, Enchantments.MENDING, Enchantments.UNBREAKING));
    @Unique private final static Lazy<List<Enchantment>> axehoeEnchants = new Lazy<>(() -> List.of(Enchantments.EFFICIENCY, Enchantments.MENDING, Enchantments.SILK_TOUCH, Enchantments.UNBREAKING));
    @Unique private final static Lazy<List<Enchantment>> swordEnchants = new Lazy<>(() -> List.of(Enchantments.FIRE_ASPECT, Enchantments.KNOCKBACK, Enchantments.LOOTING, Enchantments.MENDING, Enchantments.SHARPNESS, Enchantments.SWEEPING, Enchantments.UNBREAKING));
    @Unique private final static Lazy<List<Enchantment>> helmetEnchants = new Lazy<>(() -> List.of(Enchantments.AQUA_AFFINITY, Enchantments.MENDING, Enchantments.PROTECTION, Enchantments.RESPIRATION, Enchantments.UNBREAKING));
    @Unique private final static Lazy<List<Enchantment>> chestplateleggingsEnchants = new Lazy<>(() -> List.of(Enchantments.MENDING, Enchantments.PROTECTION, Enchantments.UNBREAKING));
    @Unique private final static Lazy<List<Enchantment>> bootsEnchants = new Lazy<>(() -> List.of(Enchantments.DEPTH_STRIDER, Enchantments.FEATHER_FALLING, Enchantments.MENDING, Enchantments.PROTECTION, Enchantments.SOUL_SPEED, Enchantments.UNBREAKING));
    @Unique private final static Lazy<List<Enchantment>> compassEnchants = new Lazy<>(() -> List.of(Enchantments.VANISHING_CURSE));
    @Unique private final static Lazy<List<Enchantment>> fnsoasEnchants = new Lazy<>(() -> List.of(Enchantments.MENDING, Enchantments.UNBREAKING));
    @Unique private final static Lazy<List<Enchantment>> rodEnchants = new Lazy<>(() -> List.of(Enchantments.LUCK_OF_THE_SEA, Enchantments.LURE, Enchantments.MENDING, Enchantments.UNBREAKING));
    @Unique private final static Lazy<List<Enchantment>> bowEnchants = new Lazy<>(() -> List.of(Enchantments.FLAME, Enchantments.INFINITY, Enchantments.POWER, Enchantments.PUNCH, Enchantments.UNBREAKING));
    @Unique private final static Lazy<List<Enchantment>> tridentEnchants = new Lazy<>(() -> List.of(Enchantments.CHANNELING, Enchantments.IMPALING, Enchantments.LOYALTY, Enchantments.PUNCH, Enchantments.UNBREAKING));
    @Unique private final static Lazy<List<Enchantment>> crossbowEnchants = new Lazy<>(() -> List.of(Enchantments.PIERCING, Enchantments.MENDING, Enchantments.QUICK_CHARGE, Enchantments.UNBREAKING));

    @Unique
    private static final Lazy<Map<String, List<Enchantment>>> enchantmentMap = new Lazy<>(() -> Map.ofEntries(
        Map.entry("pickaxe", pickaxeshovelEnchants.get()),
        Map.entry("shovel", pickaxeshovelEnchants.get()),
        Map.entry("_axe", axehoeEnchants.get()),
        Map.entry("hoe", axehoeEnchants.get()),
        Map.entry("shears", axehoeEnchants.get()),
        Map.entry("sword", swordEnchants.get()),
        Map.entry("helmet", helmetEnchants.get()),
        Map.entry("chestplate", chestplateleggingsEnchants.get()),
        Map.entry("leggings", chestplateleggingsEnchants.get()),
        Map.entry("boots", bootsEnchants.get()),
        Map.entry("compass", compassEnchants.get()),
        Map.entry("flint_and_steel", fnsoasEnchants.get()),
        Map.entry("_on_a_stick", fnsoasEnchants.get()),
        Map.entry("shield", fnsoasEnchants.get()),
        Map.entry("elytra", fnsoasEnchants.get()),
        Map.entry("fishing_rod", rodEnchants.get()),
        Map.entry("bow", bowEnchants.get()),
        Map.entry("trident", tridentEnchants.get()),
        Map.entry("crossbow", crossbowEnchants.get())
    ));

    @Shadow
    public abstract void putSubTag(String key, Tag tag);

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    public abstract Item getItem();

    @Shadow
    private CompoundTag tag;

    @Unique
    private static boolean isDraftedItemTag(CompoundTag tag) {
        if (tag.contains("_d2i")) {
            return true;
        }
        if (tag.contains("tag")) {
            Tag innerTag = tag.get("tag");
            if (CompoundTag.class.isAssignableFrom(innerTag.getClass())) {
                return ((CompoundTag) innerTag).contains("_d2i");
            }
        }
        return false;
    }

    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;I)V", at = @At("TAIL"))
    void ItemStack(ItemConvertible item, int count, CallbackInfo ci) {
        if (!EnchantUtils.levelOneEnchants()) {
            return;
        }

        if (item != null && !item.asItem().getTranslationKey().contains("bowl")) {
            for (String id : enchantmentMap.get().keySet()) {
                if (item.asItem().getTranslationKey().contains(id)) {
                    ListTag listTag = new ListTag();
                    for (Enchantment enchantment : enchantmentMap.get().get(id)) {
                        CompoundTag compoundTag = new CompoundTag();
                        compoundTag.putString("id", String.valueOf(Registry.ENCHANTMENT.getId(enchantment)));
                        compoundTag.putShort("lvl", (short) enchantment.getMaxLevel());
                        listTag.add(compoundTag);
                    }
                    this.putSubTag("Enchantments", listTag);
                }
            }
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    void ItemStack(CompoundTag tag, CallbackInfo ci) {
        if (!EnchantUtils.levelOneEnchants() || isDraftedItemTag(tag)) {
            return;
        }

        Item item = this.getItem();

        if (item != null && !item.asItem().getTranslationKey().contains("bowl")) {
            for (String id : enchantmentMap.get().keySet()) {
                if (item.asItem().getTranslationKey().contains(id)) {
                    ListTag listTag = new ListTag();
                    for (Enchantment enchantment : enchantmentMap.get().get(id)) {
                        CompoundTag compoundTag = new CompoundTag();
                        compoundTag.putString("id", String.valueOf(Registry.ENCHANTMENT.getId(enchantment)));
                        compoundTag.putShort("lvl", (short) enchantment.getMaxLevel());
                        listTag.add(compoundTag);
                    }
                    this.putSubTag("Enchantments", listTag);
                }
            }
        }
    }

    @WrapOperation(method = "copy", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;copy()Lnet/minecraft/nbt/CompoundTag;"))
    CompoundTag copyInject(CompoundTag instance, Operation<CompoundTag> original, @Local(ordinal = 1) ItemStack itemStack) {
        CompoundTag copyresult = original.call(instance);

        if (!EnchantUtils.levelOneEnchants() || !itemStack.hasTag() || isDraftedItemTag(copyresult)) {
            return copyresult;
        }

        CompoundTag newTag = itemStack.getTag();
        if (newTag == null) {
            return copyresult;
        }

        Tag newEnchants = newTag.get("Enchantments");

        if (newEnchants == null) {
            return copyresult;
        }

        // copy the tag
        copyresult.put("Enchantments", newEnchants);

        return copyresult;
    }

    /*
    @Inject(method = "onCraft", at = @At("HEAD"))
    void onCraftInject(World world, PlayerEntity player, int amount, CallbackInfo ci) {
        if (!EnchantUtils.levelOneEnchants()) {
            return;
        }

        final Item item = this.getItem();

        if (item != null && !item.asItem().getTranslationKey().contains("bowl")) {
            for (String id : enchantmentMap.get().keySet()) {
                if (item.asItem().getTranslationKey().contains(id)) {
                    ListTag listTag = new ListTag();
                    for (Enchantment enchantment : enchantmentMap.get().get(id)) {
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
    */
}
