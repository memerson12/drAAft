package draaft.mixin.entity.mob;

import draaft.draaft;
import draaft.persistent.WorldState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(DrownedEntity.class)
public abstract class DrownedEntityMixin extends ZombieEntity implements RangedAttackMob {
    @Unique
    private static final Logger logger = draaft.LOGGER;

    public DrownedEntityMixin(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    public DrownedEntityMixin(World world) {
        super(world);
    }

    @ModifyConstant(method = "initEquipment", constant = @Constant(doubleValue = 0.9))
    private double injectedEquipment(double value) {
        return 0.7;
    }

    @ModifyConstant(method = "initialize", constant = @Constant(floatValue = 0.03F))
    private float injectedShell(float value) {
        return 0.09F;
    }

    @Redirect(method = "initEquipment", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextFloat()F"))
    private float injectedEquipmentFloat(Random instance) {
        instance.setSeed(getBlockSeed(instance, this.world.getServer().getSaveProperties().getGeneratorOptions().getSeed(), this.getBlockPos().getX(), this.getBlockPos().getZ()));
        return instance.nextFloat();
    }

    @Redirect(method = "initEquipment", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I"))
    private int injectedEquipmentInt(Random instance, int i) {
        return (int) getBlockSeed(instance, this.world.getServer().getSaveProperties().getGeneratorOptions().getSeed(), this.getBlockPos().getX(), this.getBlockPos().getZ());
    }

    @Redirect(method = "initialize", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextFloat()F"))
    private float injectedShell(Random instance) {
        instance.setSeed(getBlockSeed(instance, this.world.getServer().getSaveProperties().getGeneratorOptions().getSeed(), this.getBlockPos().getZ(), this.getBlockPos().getX()));
        return 1.0F - instance.nextFloat();
    }

    @Unique
    private static long getBlockSeed(Random random, long seed, int x, int z) {
        random.setSeed(seed);
        long l = random.nextLong() | 1L;
        long m = random.nextLong() | 1L;
        return (long) x * l + (long) z * m ^ seed;
    }

    @Override
    protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
        if (!(this.world instanceof ServerWorld)) {
            logger.warn("DrownedEntityMixin - Not ServerWorld");
            super.dropEquipment(source, lootingMultiplier, allowDrops);
            return;
        }
        super.dropEquipment(source, lootingMultiplier, false);
        ServerWorld world = (ServerWorld) this.getEntityWorld();
        WorldState state = WorldState.getServerState(world);
        Random draaftTridentRng = state.getOrCreateTridentRng(world);

        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemStack = this.getEquippedStack(equipmentSlot);

            if (itemStack.getItem().equals(Items.NAUTILUS_SHELL)) {
                continue;
            }
            float f = itemStack.getItem().equals(Items.TRIDENT) ? 0.125F : this.getDropChance(equipmentSlot);
            boolean bl = f > 1.0F;
            if (!itemStack.isEmpty()
                    && !EnchantmentHelper.hasVanishingCurse(itemStack)
                    && (allowDrops || bl)
                    && Math.max(draaftTridentRng.nextFloat() - (float)lootingMultiplier * 0.02F, 0.0F) < f) {
                if (!bl && itemStack.isDamageable()) {
                    itemStack.setDamage(Math.min(itemStack.getMaxDamage() - this.random.nextInt(1 + this.random.nextInt(Math.max(itemStack.getMaxDamage() - 3, 1))), itemStack.getMaxDamage() - 2));
                }
                this.dropStack(itemStack);
            }
        }
    }
}
