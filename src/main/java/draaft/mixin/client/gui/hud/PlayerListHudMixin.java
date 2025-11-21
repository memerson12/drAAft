package draaft.mixin.client.gui.hud;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin extends DrawableHelper {

    @Shadow
    @Final
    private MinecraftClient client;

    @WrapMethod(method = "render")
    private void renderOverride(MatrixStack matrices, int scaleWidth, Scoreboard scoreboard, ScoreboardObjective scoreboardObjective, Operation<Void> original) {
        // Simple demo UI: 4 stacked boxes each showing draft1..draft5 for hard-coded players
        int left = 10;
        int boxWidth = 200;
        int boxHeight = 36; // enough to hold 5 small lines
        int spacing = 8;
        int baseTop = 30;

        String[] players = new String[]{"player1", "player2", "player3", "player4"};

        for (int i = 0; i < players.length; i++) {
            int top = baseTop + i * (boxHeight + spacing);
            int right = left + boxWidth;
            int bottom = top + boxHeight;

            // Background gradient for each box
            fillGradient(matrices, left, top, right, bottom, 0xFF2b2b2b, 0xFF1f1f1f);

            // Slight highlight at top
            fillGradient(matrices, left, top, right, top + 6, 0x66ffffff, 0x00000000);

            // Border lines
            drawHorizontalLine(matrices, left, right, top, 0xFF000000);
            drawHorizontalLine(matrices, left, right, bottom, 0xFF000000);
            drawVerticalLine(matrices, left, top, bottom, 0xFF000000);
            drawVerticalLine(matrices, right, top, bottom, 0xFF000000);

            // Player name header
            this.client.textRenderer.drawWithShadow(matrices, players[i], left + 8, top + 4, 0xFFD9D9D9);

            // Draft items (draft1 .. draft5)
            int textStartY = top + 14;
            int lineHeight = 4 + this.client.textRenderer.fontHeight; // small spacing
            for (int j = 1; j <= 5; j++) {
                String draftLine = "draft" + j;
                int y = textStartY + (j - 1) * lineHeight;
                this.client.textRenderer.drawWithShadow(matrices, draftLine, left + 12, y, 0xFFCCCCCC);
            }
        }

        // Optionally call original to preserve vanilla rendering; if you want to replace, comment this out
//        try {
//            original.call(matrices, scaleWidth, scoreboard, scoreboardObjective);
//        } catch (Throwable t) {
//            // ignore any errors from calling the original so our demo overlay still displays
//        }
    }


}
