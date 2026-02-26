package draaft.mixin.entity.boss;

import draaft.persistent.WorldManifest;
import draaft.world.MinecraftClientWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherEntity.class)
public abstract class WitherEntityMixin extends HostileEntity {
    protected WitherEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "dropEquipment", at = @At("TAIL"))
    void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops, CallbackInfo ci) {
        if (
            FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
                && MinecraftClientWrapper.available()
                && MinecraftClientWrapper.getWorld() != null
        ) {
            var world = MinecraftClientWrapper.getWorld();

            if (WorldManifest.get(world).on(WorldManifest.Feature.DOUBLE_DROPS)) {
                ItemEntity itemEntity = this.dropItem(Items.NETHER_STAR);
                if (itemEntity != null) {
                    itemEntity.setCovetedItem();
                }
            }
        }
    }
}
