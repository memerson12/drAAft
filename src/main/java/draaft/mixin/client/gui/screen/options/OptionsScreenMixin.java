package draaft.mixin.client.gui.screen.options;

import draaft.client.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
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
public abstract class OptionsScreenMixin extends Screen {
    @Unique
    private final OptionsScreenAccessor self = (OptionsScreenAccessor) this;

    @Unique
    private Difficulty originalDifficulty;

    @Unique
    private boolean pseudoPeacefulSelected = false;

    private OptionsScreenMixin(Text title) {
        super(title);
    }

    @Redirect(method = "init", at = @At(value = "NEW", ordinal = 0, target = "Lnet/minecraft/client/gui/widget/ButtonWidget;"))
    ButtonWidget difficultyButton(int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress) {
        if (!ModConfig.getInstance().disablePeaceful) {
            return new ButtonWidget(x, y, width, height, message, onPress);
        }

        // this instance is reused when one of the option subscreens is closed
        if (!this.pseudoPeacefulSelected) {
            this.originalDifficulty = self.draaft$difficulty();
        } else {
            this.pseudoPeacefulSelected = false;
        }

        return new ButtonWidget(x, y, width, height, message, (btn) -> {
            if (this.pseudoPeacefulSelected) {
                this.switchTo(Difficulty.EASY);
                this.pseudoPeacefulSelected = false;

                self.draaft$difficultyButton().setMessage(self.draaft$getDifficultyButtonText(self.draaft$difficulty()));
            } else if (self.draaft$difficulty() == Difficulty.HARD) {
                this.switchTo(this.originalDifficulty);
                this.pseudoPeacefulSelected = true;

                self.draaft$difficultyButton().setMessage(
                    new TranslatableText("options.difficulty")
                        .append(": ")
                        .append(new TranslatableText("draaft.game.options.pseudoPeaceful"))
                );
            } else {
                this.switchTo(Difficulty.byOrdinal(self.draaft$difficulty().getId() + 1));

                self.draaft$difficultyButton().setMessage(self.draaft$getDifficultyButtonText(self.draaft$difficulty()));
            }
        });
    }

    @Unique
    private void switchTo(Difficulty difficulty) {
        self.draaft$setDifficulty(difficulty);

        Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler())
            .sendPacket(new UpdateDifficultyC2SPacket(difficulty));
    }
}
