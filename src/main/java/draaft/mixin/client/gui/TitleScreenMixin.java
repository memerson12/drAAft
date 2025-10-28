package draaft.mixin.client.gui;

import draaft.client.DraaftServices;
import draaft.client.ServerClient;
import draaft.client.gui.LoginButton;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;
import java.util.function.BooleanSupplier;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    @Unique
    private static final String DEFAULT_BUTTON_HOVER = "drAAft Login";

    @Unique
    private BooleanSupplier anyButtonHovered;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(
            method = "init",
            at = @At("TAIL")
    )
    private void addDraaftLoginButton(CallbackInfo info) {
        int singlePlayerButtonHalfWidth = 100;
        int horizontalButtonSpacing = 4;
        int btnX = this.width / 2 + singlePlayerButtonHalfWidth + horizontalButtonSpacing;
        int baseBtnY = this.height / 4 + 48;

        var btn = this.addButton(new LoginButton(
                btnX,
                baseBtnY,
                Items.WATER_BUCKET,
                () -> ServerClient.getInstance() != null,
                DraaftServices.DEFAULT,
                this
        ));

        var altDraaftServices = DraaftServices.fromJvmArgs();

        LoginButton altBtn;

        if (altDraaftServices != null) {
            altBtn = this.addButton(new LoginButton(
                    btnX,
                    baseBtnY + 24,
                    Items.LAVA_BUCKET,
                    () -> ServerClient.getInstance() != null,
                    altDraaftServices,
                    this
            ));
        } else {
            altBtn = null;
        }

        this.anyButtonHovered = () -> altBtn != null ? (btn.isHovered() || altBtn.isHovered()) : btn.isHovered();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // TODO(me-nx): switch to toasts
        int x = this.width / 2 + 104;
        int y = this.height / 4 + 48;

        if (this.anyButtonHovered.getAsBoolean() || ServerClient.connectingStatus != null) {
            String s = ServerClient.connectingStatus;
            if (s == null) {
                s = DEFAULT_BUTTON_HOVER;
            } else {
                if (ServerClient.connectingStatusTimestamp == 0 && !this.anyButtonHovered.getAsBoolean()) {
                    // Timed out already, just return
                    return;
                }
                long t = Instant.now().getEpochSecond();
                if (t - ServerClient.connectingStatusTimestamp > 10) {
                    ServerClient.connectingStatusTimestamp = 0;
                }
            }

            this.drawCenteredText(matrices, TitleScreenMixin.this.textRenderer, new LiteralText(s), x + 10, y - 15, 0xffffff);
        }
    }
}