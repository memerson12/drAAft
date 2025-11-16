package draaft.mixin.block.entity;

import draaft.persistent.WorldState;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootableContainerBlockEntity.class)
public abstract class LootableContainerBlockEntityMixin extends LockableContainerBlockEntity {
    @Shadow
    @Nullable
    protected Identifier lootTableId;

    protected LootableContainerBlockEntityMixin(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
    }

    @Inject(method = "checkLootInteraction", at = @At("HEAD"), cancellable = true)
    void checkLootInteraction(PlayerEntity player, CallbackInfo ci) {
        if (this.lootTableId != null && this.lootTableId.toString().equals("minecraft:chests/desert_pyramid") && this.world != null && this.world.getServer() != null) {
            LootTable lootTable = this.world.getServer().getLootManager().getTable(this.lootTableId);
            if (player instanceof ServerPlayerEntity) {
                Criteria.PLAYER_GENERATES_CONTAINER_LOOT.test((ServerPlayerEntity)player, this.lootTableId);
            }

            this.lootTableId = null;
            LootContext.Builder builder = new LootContext.Builder((ServerWorld)this.world)
                .parameter(LootContextParameters.POSITION, new BlockPos(this.pos))
                .random(WorldState.getServerState((ServerWorld) this.world).getOrCreateRng(WorldState.RngType.TEMPLE, (ServerWorld) this.world));
            if (player != null) {
                builder.luck(player.getLuck()).parameter(LootContextParameters.THIS_ENTITY, player);
            }

            lootTable.supplyInventory(this, builder.build(LootContextTypes.CHEST));
            ci.cancel();
        }
    }
}
