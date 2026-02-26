package draaft.mixin.client.network;

import draaft.compat.ModCompat;
import draaft.compat.speedrunigt.SpeedrunIGTCompat;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ClientAdvancementManager.class)
public class ClientAdvancementManagerMixin {
    @Shadow
    @Final
    private AdvancementManager manager;

    @Shadow
    @Final
    private Map<Advancement, AdvancementProgress> advancementProgresses;

    @Inject(method = "onAdvancements", at = @At("RETURN"))
    void onAdvancements(AdvancementUpdateS2CPacket packet, CallbackInfo ci) {
        if (ModCompat.hasSpeedrunIGT()) {
            SpeedrunIGTCompat.onAdvancements(this.manager, this.advancementProgresses);
        }
    }
}
