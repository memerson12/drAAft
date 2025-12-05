package draaft.mixin.client.network;

import com.llamalad7.mixinextras.sugar.Local;
import draaft.world.WorldInterface;
import draaft.world.WorldClientInfo;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow
    private ClientWorld world;

    @Unique
    private static final String LOGGER_WARN_TARGET = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V";

    @Redirect(method = "onCustomPayload", at = @At(value = "INVOKE", target = LOGGER_WARN_TARGET, remap = false))
    void handleDraaftPackets(Logger instance, String fmt, Object args, @Local(argsOnly = true) CustomPayloadS2CPacket packet) {
        if (packet.getChannel().equals(WorldClientInfo.CHANNEL)) {
            ((WorldInterface) world).draaft$setClientInfo(WorldClientInfo.fromBuffer(packet.getData()));
        } else {
            instance.warn(fmt, args);
        }
    }
}
