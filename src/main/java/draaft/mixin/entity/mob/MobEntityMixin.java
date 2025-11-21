package draaft.mixin.entity.mob;

import draaft.persistent.WorldState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {
    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "dropLoot", at = @At("HEAD"))
    void dropLoot(DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        if (this.getType().equals(EntityType.WITHER_SKELETON) && causedByPlayer) {
            if (source.getAttacker() == null) {
                LOGGER.warn("Something went horribly wrong - causedByPlayer was true, getAttacker() was null");
                return;
            }
            int looting;
            if (source.getAttacker() instanceof PlayerEntity) {
                looting = EnchantmentHelper.getLooting((LivingEntity) source.getAttacker());
            } else {
                LOGGER.warn("Something went horribly wrong - causedByPlayer was true, getAttacker() was not a PlayerEntity");
                looting = 0;
            }
            ServerWorld world = (ServerWorld) this.getEntityWorld();
            WorldState state = WorldState.getServerState(world);
            WorldState.RandomState draaftSkullState = state.getOrCreateRng(WorldState.RngType.SKULL, world);
            int killed = draaftSkullState.incrementUses();
            int timer = 20;
            while (looting > 0) {
                timer -= (6 / looting);
                looting--;
            }
            if ((killed % timer) == 0) {
                dropPitySkull();
            }
        }
    }

    @Unique
    private void dropPitySkull() {
        WorldState.getServerState((ServerWorld) this.getEntityWorld()).getOrCreateRng(WorldState.RngType.SKULL, (ServerWorld) this.getEntityWorld()).resetUses();
        this.dropStack(new ItemStack(Items.WITHER_SKELETON_SKULL).setCustomName(Text.of("Pity Skull")));
    }
}
