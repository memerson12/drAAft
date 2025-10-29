package draaft.mixin.client.gui.screen.options;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.options.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.packet.c2s.play.UpdateDifficultyC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin {
    @Unique
    private boolean pseudoPeacefulSelected = false;

    @Redirect(method = "init", at = @At(value = "NEW", ordinal = 0, target = "Lnet/minecraft/client/gui/widget/ButtonWidget;"))
    ButtonWidget difficultyButton(int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress) {
        var self = (OptionsScreenAccessor)this;

        return new ButtonWidget(x, y, width, height, message, (btn) -> {
            if (self.draaft$difficulty() == Difficulty.HARD) {
                pseudoPeacefulSelected = !pseudoPeacefulSelected;

                if (!pseudoPeacefulSelected) {
                    // PEACEFUL + 1 = EASY
                    self.draaft$setDifficulty(Difficulty.PEACEFUL);
                }
            }

            if (!pseudoPeacefulSelected) {
                self.draaft$setDifficulty(Difficulty.byOrdinal(self.draaft$difficulty().getId() + 1));

                Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler())
                    .sendPacket(new UpdateDifficultyC2SPacket(self.draaft$difficulty()));

                self.draaft$difficultyButton().setMessage(self.draaft$getDifficultyButtonText(self.draaft$difficulty()));
            } else {
                self.draaft$difficultyButton().setMessage(
                    new TranslatableText("options.difficulty")
                        .append(": ")
                        .append(new TranslatableText("draaft.game.options.pseudoPeaceful"))
                );
            }
        });
    }
}
