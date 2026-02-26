package draaft.client;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import draaft.client.gui.skin.SkinManager;
import draaft.client.models.DraaftPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;

public class RenderUtils {

    public static void drawRoundedRect(MatrixStack matrices, int x, int y, int width, int height, int radius, int color) {
        if (radius <= 0) {
            DrawableHelper.fill(matrices, x, y, x + width, y + height, color);
            return;
        }

        // Clamp radius to half of the smallest dimension to prevent weird artifacts
        if (radius > width / 2) radius = width / 2;
        if (radius > height / 2) radius = height / 2;

        // Draw the main body (cross shape)
        // Center vertical
        DrawableHelper.fill(matrices, x + radius, y, x + width - radius, y + height, color);
        // Left part
        DrawableHelper.fill(matrices, x, y + radius, x + radius, y + height - radius, color);
        // Right part
        DrawableHelper.fill(matrices, x + width - radius, y + radius, x + width, y + height - radius, color);

        // Draw corners
        drawArc(matrices, x + radius, y + radius, radius, 180, 270, color); // Top-Left
        drawArc(matrices, x + width - radius, y + radius, radius, 270, 360, color); // Top-Right
        drawArc(matrices, x + width - radius, y + height - radius, radius, 0, 90, color); // Bottom-Right
        drawArc(matrices, x + radius, y + height - radius, radius, 90, 180, color); // Bottom-Left
    }

    public static void drawArc(MatrixStack matrices, int x, int y, int radius, int startAngle, int endAngle, int color) {
        Matrix4f matrix = matrices.peek().getModel();
        float f = (float) (color >> 24 & 255) / 255.0F;
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float k = (float) (color & 255) / 255.0F;

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        bufferBuilder.begin(GL11.GL_TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float) x, (float) y, 0.0F).color(g, h, k, f).next();

        for (int i = startAngle; i <= endAngle; i++) {
            double rad = Math.toRadians(i);
            bufferBuilder.vertex(matrix, (float) (x + Math.cos(rad) * radius), (float) (y + Math.sin(rad) * radius), 0.0F).color(g, h, k, f).next();
        }

        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);

        RenderSystem.enableCull();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawPlayerHead(MatrixStack matrices, int x, int y, int size, DraaftPlayer player) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.getServer() != null;

        Identifier skinTexture = player.getFaceTexture();
        client.getTextureManager().bindTexture(skinTexture);

        // Enable blending for proper transparency
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        // Draw the face portion of the skin (8x8 pixels at UV 8,8 on 64x64 texture)
        DrawableHelper.drawTexture(matrices, x, y, size, size, 8.0F, 8.0F, 8, 8, 64, 64);

        // Draw the hat layer (overlay) on top
        DrawableHelper.drawTexture(matrices, x, y, size, size, 40.0F, 8.0F, 8, 8, 64, 64);

        RenderSystem.disableBlend();
    }

}
