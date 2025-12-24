package draaft.mixin.client.gui.hud;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import draaft.client.DraaftState;
import draaft.client.RenderUtils;
import draaft.client.models.DraaftPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin extends DrawableHelper {

    @Shadow
    @Final
    private MinecraftClient client;

    @WrapMethod(method = "render")
    private void renderOverride(MatrixStack matrices, int scaleWidth, Scoreboard scoreboard, ScoreboardObjective scoreboardObjective, Operation<Void> original) {
        if (!DraaftState.isAccessible()) return;
        final DraaftState draaftState = DraaftState.getInstance();
        if (!DraaftState.inActiveDraaftWorld()) return; // does nothing at present

        ArrayList<DraaftPlayer> players = draaftState.getRoom().getNonSpectatorPlayers();

        int screenHeight = this.client.getWindow().getScaledHeight();
        int screenWidth = this.client.getWindow().getScaledWidth();

        float scale = screenHeight / 380f;

        final int cornerRadius = (int) (10 * scale);
        final int margin = (int) (10 * scale);
        final int boxColor = 0x90D5D5D5;

        int boxWidth = (int) (screenWidth * 0.20);

        double boxHeightPercentage = Math.min(0.90, players.size() * 0.15);

        int boxHeight = (int) (screenHeight * boxHeightPercentage);
        int boxY = (screenHeight / 2) - (boxHeight / 2);

        drawAdvancementCountsPanel(matrices, players, margin, boxY, boxWidth, boxHeight, cornerRadius, scale, boxColor);
    }

    @Unique
    private void drawAdvancementCountsPanel(MatrixStack matrices, ArrayList<DraaftPlayer> players, int x, int y, int width, int height, int cornerRadius, float scale, int boxColor) {
        // Draw the background box
        RenderUtils.drawRoundedRect(matrices, x, y, width, height, cornerRadius, boxColor);

        // Draw header
        String headerText = "Advancement Counts";
        int headerY = y + (int) (10 * scale);
        drawHeader(matrices, x, headerY, (float) width, scale, headerText);

        // Draw divider line
        fill(matrices, x, headerY + (int) (20 * scale), x + width, headerY + (int) (20 * scale) + Math.max(1, (int) (1 * scale)), 0xA0FFFFFF);

        // Player entry settings
        final int playerHeadSize = (int) (28 * scale);
        final int entryHeight = (int) (44 * scale);
        final int entryStartY = headerY + (int) (30 * scale);
        final int headX = x + (int) (10 * scale);
        final int textX = headX + playerHeadSize + (int) (6 * scale);

        // Draw each player entry
        for (int i = 0; i < players.size(); i++) {
            int entryY = entryStartY + (i * entryHeight);
            int headY = entryY;
            int textY = entryY + (playerHeadSize / 2) - (int) ((this.client.textRenderer.fontHeight * scale) / 2);

            // Draw player head
            RenderUtils.drawPlayerHead(matrices, headX, headY, playerHeadSize, players.get(i));

            // Draw player name and advancement count
            String playerText = players.get(i).getUsername() + ": " + DraaftState.getInstance().advancementCounts.getOrDefault(players.get(i).getUuid().toString(), 0).toString();

            matrices.push();
            matrices.translate(textX, textY, 0);
            matrices.scale(scale * 1.1f, scale * 1.1f, 1.0f);
            this.client.textRenderer.drawWithShadow(matrices, playerText, 0, 0, 0xFFFFFFFF);
            matrices.pop();
        }
    }

    @Unique
    private void drawHeader(MatrixStack matrices, int boxX, int headerY, float boxWidth, float scale, String headerText) {
        float headerX = (boxX + boxWidth / 2) - (this.client.textRenderer.getWidth(headerText) * (scale * 1.2f)) / 2;

        matrices.push();
        matrices.translate(headerX, headerY, 0);
        matrices.scale(scale * 1.2f, scale * 1.2f, 1.0f);
        this.client.textRenderer.draw(matrices, headerText, 0, 0, 0xFFFFFFFF);
        matrices.pop();
    }
}
