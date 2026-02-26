package draaft.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringRenderable;
import net.minecraft.util.Identifier;

import java.util.List;

@Environment(EnvType.CLIENT)
public class DraaftToast implements Toast {
    private static final Identifier ICON = new Identifier("draaft", "logo.png");
    private static final Identifier ERROR_ICON = new Identifier("textures/item/barrier.png");
    private static final long TIME_TO_HIDE_MS = 10_000;
    private static final int TEXT_BASE_X = 30;

    private final StringRenderable title;
    private final List<StringRenderable> content;
    private final boolean error;

    private long startTime = -1;
    private int height = 32;

    private DraaftToast(StringRenderable title, StringRenderable content, boolean error) {
        this.title = title;
        this.content = MinecraftClient.getInstance().textRenderer.wrapLines(content, this.getWidth() - TEXT_BASE_X - 6);
        this.error = error;
    }

    @Override
    public Visibility draw(MatrixStack matrices, ToastManager manager, long time) {
        if (this.startTime == -1) {
            this.startTime = time;
        }

        var client = MinecraftClient.getInstance();

        client.getTextureManager().bindTexture(TOASTS_TEX);

        // Top of the toast texture
        manager.drawTexture(matrices, 0, 0, 0, 0, this.getWidth(), 16);

        // Center
        int textureY = 16;
        for (int i = 0; i < this.content.size(); i++) {
            manager.drawTexture(matrices, 0, textureY, 0, 16, this.getWidth(), client.textRenderer.fontHeight + 2);
            textureY += client.textRenderer.fontHeight + 2;
        }

        // Bottom
        manager.drawTexture(matrices, 0, textureY, 0, 28, this.getWidth(), 4);

        this.height = textureY + 4;

        client.getTextureManager().bindTexture(this.error ? ERROR_ICON : ICON);

        int middleY = this.height / 2;

        DrawableHelper.drawTexture(matrices, 6, middleY - 10, 0, 0, 20, 20, 20, 20);

        client.textRenderer.draw(matrices, this.title, TEXT_BASE_X, 7.0f, 0xffffff00);

        float textY = 18.0f;
        for (var line : this.content) {
            client.textRenderer.draw(matrices, line, TEXT_BASE_X, textY, 0xffffffff);
            textY += client.textRenderer.fontHeight + 2;
        }

        return (time - this.startTime) < TIME_TO_HIDE_MS ? Visibility.SHOW : Visibility.HIDE;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    public static DraaftToast of(StringRenderable title, StringRenderable content) {
        return new DraaftToast(title, content, false);
    }

    public static DraaftToast error(StringRenderable title, StringRenderable content) {
        return new DraaftToast(title, content, true);
    }

    public static void show(StringRenderable title, StringRenderable content) {
        MinecraftClient.getInstance().getToastManager()
            .add(DraaftToast.of(title, content));
    }

    public static void showError(StringRenderable title, StringRenderable content) {
        MinecraftClient.getInstance().getToastManager()
            .add(DraaftToast.error(title, content));
    }
}
