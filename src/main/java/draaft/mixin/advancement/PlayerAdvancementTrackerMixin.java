package draaft.mixin.advancement;

import draaft.client.DraaftState;
import draaft.persistent.WorldManifest;
import draaft.player.PlayerData;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {
    @Shadow
    private ServerPlayerEntity owner;

    @Inject(method = "grantCriterion", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/AdvancementRewards;apply(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
    private void injectAdvancementUpload(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        DraaftState.with((ds) -> ds.addAdvancement(advancement.getId().toString()));
    }

    @Inject(method = "grantCriterion", at = @At("HEAD"), cancellable = true)
    private void cancelGreatView(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        var player = this.owner.getDataTracker().get(PlayerData.TRACKED);

        boolean poorView = WorldManifest.get(this.owner.getServerWorld())
            .on(WorldManifest.Feature.POOR_VIEW);

        if (advancement.getId().toString().contains("end/levitate")) {
            if (!player.hasCompletedGreatView() && poorView) {
                this.owner.removeStatusEffect(StatusEffects.LEVITATION);
                this.owner.getDataTracker().set(PlayerData.TRACKED, player.withHasCompletedGreatView(true));
                cir.setReturnValue(false);
            }
        }
    }
}
