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
                LOGGER.info("Something went horribly wrong - causedByPlayer was true, getAttacker() was null");
                return;
            }
            int i;
            if (source.getAttacker() instanceof PlayerEntity) {
                i = EnchantmentHelper.getLooting((LivingEntity) source.getAttacker());
            } else {
                LOGGER.info("Something went horribly wrong - causedByPlayer was true, getAttacker() was not a PlayerEntity");
                i = 0;
            }
            ServerWorld world = (ServerWorld) this.getEntityWorld();
            WorldState state = WorldState.getServerState(world);
            int killed = state.incrementRng(WorldState.RngType.SKULL, world);
            switch (i) {
                case 0: {
                    if ((killed > 0) && ((killed % 20) == 0)) {
                        state.resetRng(WorldState.RngType.SKULL);
                        this.dropStack(new ItemStack(Items.WITHER_SKELETON_SKULL).setCustomName(Text.of("Pity Skull")));
                    }
                    break;
                }
                case 1: {
                    if ((killed > 0) && ((killed % 14) == 0)) {
                        state.resetRng(WorldState.RngType.SKULL);
                        this.dropStack(new ItemStack(Items.WITHER_SKELETON_SKULL).setCustomName(Text.of("Pity Skull")));
                    }
                    break;
                }
                case 2: {
                    if ((killed > 0) && ((killed % 11) == 0)) {
                        state.resetRng(WorldState.RngType.SKULL);
                        this.dropStack(new ItemStack(Items.WITHER_SKELETON_SKULL).setCustomName(Text.of("Pity Skull")));
                    }
                    break;
                }
                case 3: {
                    if ((killed > 0) && ((killed % 9) == 0)) {
                        state.resetRng(WorldState.RngType.SKULL);
                        this.dropStack(new ItemStack(Items.WITHER_SKELETON_SKULL).setCustomName(Text.of("Pity Skull")));
                    }
                    break;
                }
            }
        }
    }
}
