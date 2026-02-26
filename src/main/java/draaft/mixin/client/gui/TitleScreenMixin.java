package draaft.mixin.client.gui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import draaft.client.DraaftServices;
import draaft.client.ServerClient;
import draaft.client.gui.LoginButton;
import draaft.draaft;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
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

        ButtonWidget.TooltipSupplier tooltipSupplier = (ButtonWidget button, MatrixStack matrices, int mouseX, int mouseY) -> {
            int x = btnX + 10;
            int y = baseBtnY - 15;

            this.drawCenteredText(matrices, this.textRenderer, new TranslatableText("draaft.login.tooltip"), x, y, 0xffffff);
        };

        this.addButton(new LoginButton(
            btnX,
            baseBtnY,
            Items.WATER_BUCKET,
            () -> ServerClient.getInstanceOrNull() != null,
            tooltipSupplier,
            DraaftServices.DEFAULT,
            this
        ));

        var altDraaftServices = DraaftServices.fromJvmArgs();

        if (altDraaftServices != null) {
            this.addButton(new LoginButton(
                btnX,
                baseBtnY + 24,
                Items.LAVA_BUCKET,
                () -> ServerClient.getInstanceOrNull() != null,
                tooltipSupplier,
                altDraaftServices,
                this
            ));
        }
    }

    @WrapOperation(
        method = "render",
        at = @At(
            value = "INVOKE",
            ordinal = 0,
            target = "Lnet/minecraft/client/gui/screen/TitleScreen;drawStringWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)V"
        )
    )
    void drawDraaftVersionString(
        TitleScreen instance,
        MatrixStack matrixStack,
        TextRenderer textRenderer,
        String string,
        int x,
        int y,
        int color,
        Operation<Void> original
    ) {
        String draaftInfo = "drAAft v." + draaft.DRAAFT_VERSION + (draaft.IS_DEBUG ? " (dev)" : "");

        this.drawStringWithShadow(matrixStack, textRenderer, draaftInfo, x, y - 10, color);

        original.call(instance, matrixStack, textRenderer, string, x, y, color);
    }
}
