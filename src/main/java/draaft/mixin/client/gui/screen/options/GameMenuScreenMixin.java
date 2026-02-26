package draaft.mixin.client.gui.screen.options;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import draaft.client.DraaftState;
import draaft.compat.ModCompat;
import draaft.compat.fastreset.FastResetCompat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.Rect2i;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    @Unique
    private ButtonWidget reset;

    @Unique
    private long resetHoverStartTimeMS;

    @Unique
    private final TranslatableText resetText = new TranslatableText("draaft.game.menu.reset");

    protected GameMenuScreenMixin() {
        super(null);
    }

    @WrapOperation(method = "initWidgets", at = @At(value = "NEW", target = "net/minecraft/client/gui/widget/ButtonWidget"))
    ButtonWidget shiftButtons(int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress, Operation<ButtonWidget> original) {
        return original.call(
            x,
            false && DraaftState.inActiveDraaftWorld() ? y - 12 : y,
            width,
            height,
            message,
            onPress
        );
    }

    @Inject(method = "initWidgets", at = @At("RETURN"))
    void addDraaftButtons(CallbackInfo ci) {
        if (true || !DraaftState.inActiveDraaftWorld()) {
            return;
        }

        this.reset = this.addButton(new ButtonWidget(
            this.width / 2 - 102,
            this.height / 4 + 120,
            204,
            20,
            Text.of(""),
            btn -> DraaftState.with(state -> {
                var client = MinecraftClient.getInstance();

                if (ModCompat.hasFastReset() && client.getServer() != null) {
                    FastResetCompat.preventSaving(client.getServer());
                }

                state.createWorld();
            })
        ));

        this.resetHoverStartTimeMS = System.nanoTime() / 1_000_000;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/GameMenuScreen;renderBackground(Lnet/minecraft/client/util/math/MatrixStack;)V"))
    void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (true || !DraaftState.inActiveDraaftWorld()) {
            return;
        }

        if (!hoveringOverButton(this.reset, mouseX, mouseY)) {
            this.resetHoverStartTimeMS = System.nanoTime() / 1_000_000;
        }

        var resetDeltaMS = System.nanoTime() / 1_000_000 - this.resetHoverStartTimeMS;
        this.reset.active = resetDeltaMS > this.buttonDelayMS();
        if (!this.reset.active) {
            this.reset.setMessage(resetText.copy().append(" (%.1fs)".formatted((this.buttonDelayMS() - resetDeltaMS) / 1000.0)));
        } else {
            this.reset.setMessage(resetText);
        }
    }

    @Unique
    private long buttonDelayMS() {
        return 1_000;
    }

    @Unique
    private static boolean hoveringOverButton(ButtonWidget button, int mouseX, int mouseY) {
        var rect = new Rect2i(button.x, button.y, button.getWidth(), button.getHeight());

        return rect.contains(mouseX, mouseY);
    }
}
