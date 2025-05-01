package draaft.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {
    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/Arrays;fill([FF)V"), index = 1)
    private float adjustDropChance(float val) {
        return 0.125F;
    }

    @ModifyConstant(method = "dropEquipment", constant = @Constant(floatValue = 0.01F))
    private float adjustLootingMultiplier(float constant) {
        return 0.02F;
    }

    @Redirect(method = "dropEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setDamage(I)V"))
    private void minTridentDamage(ItemStack itemStack, int damage) {
        if (itemStack.getItem().equals(Items.TRIDENT)) {
            itemStack.setDamage(Math.min(itemStack.getDamage() - 2, damage));
        } else {
            itemStack.setDamage(damage);
        }
    }
}
