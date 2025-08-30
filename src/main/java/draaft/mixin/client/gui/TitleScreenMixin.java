package draaft.mixin.client.gui;

import draaft.client.ServerClient;
import me.contaria.speedrunapi.util.IdentifierUtil;
import me.contaria.speedrunapi.util.TextUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    @Unique
    private static final Identifier BUTTON_IMAGE = IdentifierUtil.ofVanilla("textures/item/bucket.png");

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(
            method = "init",
            at = @At("TAIL")
    )
    private void addDraaftLoginButton(CallbackInfo info) {
        this.addButton(new ButtonWidget(this.width / 2 + 124 - 20, this.height / 4 + 48, 20, 20, LiteralText.EMPTY, button -> {
            ServerClient.getInstance().evilConnectionTesting();
        }) {
            @Override
            public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                super.renderButton(matrices, mouseX, mouseY, delta);

                MinecraftClient.getInstance().getTextureManager().bindTexture(BUTTON_IMAGE);
                DrawableHelper.drawTexture(matrices, this.x + 2, this.y + 2, 0.0F, 0.0F, 16, 16, 16, 16);

                if (this.isHovered()) {
                    this.drawCenteredText(matrices, TitleScreenMixin.this.textRenderer, TextUtil.literal("drAAft Login"), this.x + this.width / 2, this.y - 15, 16777215);
                }
            }
        });
    }
}