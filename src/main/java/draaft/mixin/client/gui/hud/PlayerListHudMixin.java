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

//    @Unique
//    private static final DraaftPlayer[] players = new DraaftPlayer[]{
//        new DraaftPlayer("Memerson", MinecraftClient.getInstance()),
//        new DraaftPlayer("DesktopFolder", MinecraftClient.getInstance()),
//        new DraaftPlayer("me_nx", MinecraftClient.getInstance()),
//        new DraaftPlayer("PacManMVC", MinecraftClient.getInstance())
//    };
//    private static final HashMap<String, Identifier> skins = new HashMap<>();

//    @Inject(method = "<init>", at = @At(value = "RETURN", target = "Lnet/minecraft/client/gui/hud/PlayerListHud;<init>(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/gui/hud/InGameHud;)V"))
//    private static void init(MinecraftClient client, InGameHud inGameHud, CallbackInfo ci) {
//        for(String playerName : players) {
//            assert client.getServer() != null;
//            GameProfile profile = client.getServer().getUserCache().findByName(playerName);
//            SkinManager.fetchPlayerSkin(profile).thenAccept((skinId) -> {
//                skins.put(playerName, skinId);
//            }).exceptionally(throwable -> {
//                System.out.println(throwable.getMessage());
//                return null;
//            });
//        }
//    }
    @Unique

    @WrapMethod(method = "render")
    private void renderOverride(MatrixStack matrices, int scaleWidth, Scoreboard scoreboard, ScoreboardObjective scoreboardObjective, Operation<Void> original) {
        ArrayList<DraaftPlayer> players = DraaftState.getInstance().getRoom().members();

        final int cornerRadius = 10;
        final int margin = 10;
        final int boxColor = 0x90D5D5D5;

        double guiScale = this.client.getWindow().getScaleFactor();

        int screenHeight = this.client.getWindow().getScaledHeight();
        int screenWidth = this.client.getWindow().getScaledWidth();

        int boxX = margin;
        int boxWidth = (int) (screenWidth * 0.20);
        int boxHeight = (int) (screenHeight * 0.90);
        int boxY = (screenHeight / 2) - (boxHeight / 2);

        // Draw the background box
        RenderUtils.drawRoundedRect(matrices, boxX, boxY, boxWidth, boxHeight, cornerRadius, boxColor);

        // Draw header
        String headerText = "Advancement Counts";
        int headerX = boxX + 10;
        int headerY = boxY + 10;
        this.client.textRenderer.drawWithShadow(matrices, headerText, headerX, headerY, 0xFFFFFFFF);

        // Player entry settings
        final int playerHeadSize = 24;
        final int entryHeight = 40;
        final int entryStartY = headerY + 20;
        final int headX = boxX + 10;
        final int textX = headX + playerHeadSize + 8;

        // Draw each player entry
        for (int i = 0; i < players.size(); i++) {
            int entryY = entryStartY + (i * entryHeight);
            int headY = entryY;
            int textY = entryY + (playerHeadSize / 2) - (this.client.textRenderer.fontHeight / 2);

            // Draw player head
            RenderUtils.drawPlayerHead(matrices, headX, headY, playerHeadSize, players.get(i));
//            RenderUtils.drawPlayerHead(matrices, headX, headY, playerHeadSize, skins.get(players[i]));

            // Draw player name and advancement count
            String playerText = players.get(i).getUsername() + ": 22";
            this.client.textRenderer.drawWithShadow(matrices, playerText, textX, textY, 0xFFFFFFFF);
        }
        matrices.pop();
    }


}
