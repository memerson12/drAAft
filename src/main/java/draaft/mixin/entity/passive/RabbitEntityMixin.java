package draaft.mixin.entity.passive;

import draaft.persistent.WorldState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RabbitEntity.class)
public abstract class RabbitEntityMixin extends AnimalEntity {
    protected RabbitEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected LootContext.Builder getLootContextBuilder(boolean causedByPlayer, DamageSource source) {
        LootContext.Builder builder = new LootContext.Builder((ServerWorld) this.world)
                .random(WorldState.getServerState((ServerWorld) this.world).getOrCreateRng(WorldState.RngType.RABBIT, (ServerWorld) this.world))
                .parameter(LootContextParameters.THIS_ENTITY, this)
                .parameter(LootContextParameters.POSITION, this.getBlockPos())
                .parameter(LootContextParameters.DAMAGE_SOURCE, source)
                .optionalParameter(LootContextParameters.KILLER_ENTITY, source.getAttacker())
                .optionalParameter(LootContextParameters.DIRECT_KILLER_ENTITY, source.getSource());
        if (causedByPlayer && this.attackingPlayer != null) {
            builder = builder.parameter(LootContextParameters.LAST_DAMAGE_PLAYER, this.attackingPlayer).luck(this.attackingPlayer.getLuck());
        }

        return builder;
    }
}
