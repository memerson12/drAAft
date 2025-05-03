package draaft.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(DrownedEntity.class)
public abstract class DrownedEntityMixin extends ZombieEntity implements RangedAttackMob {
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
}
