package draaft.mixin.advancement;

import draaft.client.DraaftState;
import draaft.persistent.WorldManifest;
import draaft.world.MinecraftClientWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {
    @Shadow
    private ServerPlayerEntity owner;
    @Unique
    private boolean hasCompletedGreatView = false;

    @Inject(method = "grantCriterion", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/AdvancementRewards;apply(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
    private void injectAdvancementUpload(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        DraaftState.with((ds) -> ds.addAdvancement(advancement.getId().toString()));
    }

    @Inject(method = "grantCriterion", at = @At("HEAD"), cancellable = true)
    private void cancelGreatView(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        if (advancement.getId().toString().contains("end/levitate")) {
            if (!hasCompletedGreatView && FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
                && MinecraftClientWrapper.available()
                && MinecraftClientWrapper.getWorld() != null
                && WorldManifest.get(MinecraftClientWrapper.getWorld()).on(WorldManifest.Feature.POOR_VIEW)) {
                this.owner.removeStatusEffect(StatusEffects.LEVITATION);
                cir.setReturnValue(false);
                hasCompletedGreatView = true;
            }
        }
    }
}
