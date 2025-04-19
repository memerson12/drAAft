package drAAft.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.TridentItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TridentItem.class)
public class TridentItemMixin {

    @Redirect(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V", // Full signature for clarity
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;isTouchingWaterOrRain()Z"
            )
    )
    private boolean redirectIsTouchingWaterOrRain_onStoppedUsing(PlayerEntity playerEntity) {
        return checkRiptideCondition(playerEntity);
    }

    @Redirect(
            method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;", // Signature for the 'use' method
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;isTouchingWaterOrRain()Z"
            )
    )
    private boolean redirectIsTouchingWaterOrRain_use(PlayerEntity playerEntity) {
        return checkRiptideCondition(playerEntity);
    }

    @Unique
    private boolean checkRiptideCondition(PlayerEntity playerEntity) {
        BlockPos pos = playerEntity.getBlockPos();
        Biome biome = playerEntity.world.getBiome(pos);
        return playerEntity.isTouchingWater() || biome.getPrecipitation() == Biome.Precipitation.RAIN && biome.getTemperature(pos) >= 0.15F;
    }

}
