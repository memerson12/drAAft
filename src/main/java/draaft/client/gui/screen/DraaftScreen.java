package draaft.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import draaft.client.DraaftState;
import draaft.client.models.DraaftPlayer;
import draaft.client.models.ReadyStatus;
import draaft.client.models.Room;
import draaft.draaft;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.SaveLevelScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.MoreOptionsDialog;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.FileNameUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryTracker;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

import static net.minecraft.world.gen.GeneratorOptions.createOverworldGenerator;
import static net.minecraft.world.gen.GeneratorOptions.getRegistryWithReplacedOverworldGenerator;

// Enum for game stages
enum Stage {
    DRAAFT("Draaft"),
    BUILD_DATAPACK("Build Datapack"),
    DOWNLOAD_WORLD("Download World"),
    READY_UP("Ready Up"),
    PLAY("Play");

    private final String displayName;

    Stage(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

/// TODO(s) (non-exhaustive):
/// - Show room config setting somewhere
///   - Editable by admin?
/// - Update the stages to mirror what actually happens (eg, download datapack instead of build, no download world step)
/// - Make scaling work better
///   - Check GUI scale? MC might handle this for us already
/// - Give admin power to remove player?
/// - Leave room button
/// - Show actual draaft status (duh)
/// - Custom draaft background?

@Environment(EnvType.CLIENT)
public class DraaftScreen extends Screen {
    private final static Logger logger = draaft.LOGGER;

    private final Screen parent;
    private int scrollOffset = 0;
    private final DraaftState draaftState = DraaftState.getInstance();
    private static final int PLAYER_ENTRY_HEIGHT = 50;
    private static final int PLAYER_FACE_SIZE = 16;
    private static final double LEFT_PANEL_WIDTH_RATIO = 0.2; // 20% of screen width

    // Current game stage
    private Stage currentStage = Stage.DRAAFT;

    // Room code functionality
    private String roomCode;
    private boolean roomCodeRevealed = false;
    private ButtonWidget copyButton;
    private int roomCodeFieldX, roomCodeFieldY, roomCodeFieldWidth, roomCodeFieldHeight;
    private String saveDirectoryName = "Draaft-World";


    public DraaftScreen(Screen parent) {
        super(new TranslatableText("draaft.draaftingScreen.title"));
        this.parent = parent;
    }

    private int getLeftPanelWidth() {
        return (int) (this.width * LEFT_PANEL_WIDTH_RATIO);
    }

    @Override
    protected void init() {
        super.init();

        Room room = draaftState.getRoom();
        this.roomCode = room.code();

        // Add a back button
        this.addButton(new ButtonWidget(
            this.width - 100, 10, 80, 20,
            new TranslatableText("draaft.draaftingScreen.button.back"),
            button -> this.onClose()));

        // Setup room code field dimensions
        int leftPanelWidth = getLeftPanelWidth();
        this.roomCodeFieldWidth = leftPanelWidth - 70;
        System.out
            .println("Left panel width: " + leftPanelWidth + ", room code field width: " + this.roomCodeFieldWidth);
        this.roomCodeFieldHeight = 20;
        this.roomCodeFieldX = 10;
        this.roomCodeFieldY = this.height - 30;

        // Add copy button (inline with room code field)
        this.addButton(new ButtonWidget(
            this.roomCodeFieldX + this.roomCodeFieldWidth + 5, this.roomCodeFieldY, 50, 20,
            new TranslatableText("draaft.draaftingScreen.button.copy"),
            button -> copyRoomCode()));

        // Add Next Stage button above Ready button
        this.addButton(new ButtonWidget(
            this.width - 100, this.height - 80, 80, 20,
            new LiteralText("Start Game"),
            button -> {
                //debug set to correct state
                draaftState.setCurrentState(DraaftState.STATE.PREPARING_GAME);

                if (draaftState.getCurrentState() != DraaftState.STATE.PREPARING_GAME) {
                    logger.error("Tried to start game when not in PREPARING_GAME state!");
                    return;
                }

                this.client.setScreenAndRender(new SaveLevelScreen(new TranslatableText("createWorld.preparing")));

                LevelInfo levelInfo = new LevelInfo(
                    "Draaft Game",
                    GameMode.SURVIVAL,
                    false,
                    Difficulty.EASY,
                    false,
                    new GameRules(),
                    DataPackSettings.SAFE_MODE
                );

                long seed = "asdf".hashCode();
                GeneratorOptions generatorOptions = new GeneratorOptions(
                    seed, true, false, getRegistryWithReplacedOverworldGenerator(DimensionType.createDefaultDimensionOptions(seed), createOverworldGenerator(seed)));
                MoreOptionsDialog moreOptionsDialog = new MoreOptionsDialog(RegistryTracker.create(), generatorOptions);

                updateSaveFolderName();

                this.client.createWorld(this.saveDirectoryName, levelInfo, moreOptionsDialog.getRegistryManager(), generatorOptions);
                draaftState.setCurrentState(DraaftState.STATE.IN_GAME);
            }));

        // Add Next Stage button above Ready button
        this.addButton(new ButtonWidget(
            this.width - 100, this.height - 55, 80, 20,
            new TranslatableText("draaft.draaftingScreen.button.nextStage"),
            button -> {
                // Cycle to next stage
                Stage[] stages = Stage.values();
                int index = currentStage.ordinal();
                int nextIndex = (index + 1) % stages.length;

                if (stages[nextIndex] == Stage.PLAY) {
                    for (DraaftPlayer player : draaftState.getRoom().members()) {
                        if (!player.getReadyStatus().equals(ReadyStatus.READY)) {
                            return;
                        }
                    }
                }
                currentStage = stages[nextIndex];
            }));

        // Add Ready button in bottom right corner
        this.addButton(new ButtonWidget(
            this.width - 100, this.height - 30, 80, 20,
            new TranslatableText("draaft.draaftingScreen.button.ready"),
            button -> {
                // if we are still draafting, do nothing
                // otherwise, toggle between ready / not ready
                if (currentStage.equals(Stage.READY_UP)) {
                    button.active = true;
                } else {
                    button.active = false;
                    return;
                }
                DraaftPlayer self = getSelf();
                if (self == null) {
                    logger.error("idk how you did this but the client session doesn't have a player attached");
                    return;
                }
                self.setReadyStatus(self.getReadyStatus().equals(ReadyStatus.READY) ? ReadyStatus.NOT_READY : ReadyStatus.READY);
            }));
    }

    @Nullable
    private DraaftPlayer getSelf() {
        String username = MinecraftClient.getInstance().getSession().getUsername();
        for (DraaftPlayer player : draaftState.getRoom().members()) {
            if (player.getUsername().equals(username)) {
                return player;
            }
        }
        return null;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        // Draw title
        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 17, 16777215);

        // Draw stage indicator
        this.renderStageIndicator(matrices);

        // Draw left panel background
        int leftPanelWidth = getLeftPanelWidth();
        fill(matrices, 0, 0, leftPanelWidth, this.height, 0x80000000);

        // Draw left panel border
        this.drawVerticalLine(matrices, leftPanelWidth, 0, this.height, 0xFFFFFFFF);

        // Draw player list
        this.renderPlayerList(matrices, mouseX, mouseY);

        // Draw room code field
        this.renderRoomCodeField(matrices, mouseX, mouseY);

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void renderPlayerList(MatrixStack matrices, int mouseX, int mouseY) {
        int startY = 20;
        int leftPanelWidth = getLeftPanelWidth();
        int visiblePlayers = (this.height - startY) / PLAYER_ENTRY_HEIGHT;
        List<DraaftPlayer> players = draaftState.getRoom().members();
        DraaftPlayer admin = draaftState.getRoom().admin();

        for (int i = 0; i < Math.min(visiblePlayers, players.size() - scrollOffset); i++) {
            int playerIndex = i + scrollOffset;
            if (playerIndex >= players.size())
                break;

            DraaftPlayer player = players.get(playerIndex);
            int y = startY + i * PLAYER_ENTRY_HEIGHT;

            int bgColor = 0x20FFFFFF;
            fill(matrices, 0, y, leftPanelWidth, y + PLAYER_ENTRY_HEIGHT, bgColor);

            // Draw separator line
            this.drawHorizontalLine(matrices, 0, leftPanelWidth, y + PLAYER_ENTRY_HEIGHT - 1, 0x40FFFFFF);

            // Draw player face
            this.renderPlayerFace(matrices, player, 4, y + 4);

            // Draw username
            this.textRenderer.draw(matrices, player.getUsername(),
                PLAYER_FACE_SIZE + 8, y + 8, 0xFFFFFF);

            // Draw ready status below username (smaller text)
            int statusColor = getStatusColor(player.getReadyStatus());
            matrices.push();
            matrices.scale(0.75f, 0.75f, 1.0f); // Scale down to 75% size
            this.textRenderer.draw(matrices, "Status: ",
                (PLAYER_FACE_SIZE + 8) / 0.75f, (y + 22) / 0.75f, 0xFFFFFF); // White
            this.textRenderer.draw(matrices, player.getReadyStatus().getDisplayName(),
                (PLAYER_FACE_SIZE + this.textRenderer.getWidth("Status: ")) / 0.75f, (y + 22) / 0.75f,
                statusColor);
            this.textRenderer.draw(matrices, player.isSpectator() ? "Spectator" : "Player",
                (PLAYER_FACE_SIZE + 8) / 0.75f, (y + 30) / 0.75f, 0x80808080);
            matrices.pop();

            // Draw admin label if player is admin
            if (player.getUuid().equals(admin.getUuid())) {
                matrices.push();
                matrices.scale(0.75f, 0.75f, 1.0f);
                int textureX = (int) ((PLAYER_FACE_SIZE + 4) / 0.75 + (this.textRenderer.getWidth(player.getUsername() + 4) / 0.75));
                int textureY = (int) ((y + 6) / 0.75);
                this.client.getTextureManager().bindTexture(new Identifier("textures/block/command_block_front.png"));
                drawTexture(matrices, textureX, textureY, 0, 0, 16, 16, 16, 64);
                matrices.pop();
                if (mouseX >= textureX * 0.75 && mouseX <= textureX * 0.75 + 16 * 0.75 && mouseY >= textureY * 0.75 && mouseY <= textureY * 0.75 + 16) {
                    renderTooltip(matrices, new TranslatableText("draaft.draaftingScreen.admin"), (int) (textureX * 0.75), (int) (textureY * 0.75) - 2);
                }
            }


        }

        // Draw scrollbar if needed
        if (players.size() > visiblePlayers) {
            this.renderScrollbar(matrices);
        }
    }

    private void renderPlayerFace(MatrixStack matrices, DraaftPlayer player, int x, int y) {
        if (player.isSkinLoading() || !player.isSkinLoaded()) {
            // Show loading indicator (animated dots or spinner)
            this.renderLoadingIndicator(matrices, x, y);
        } else {
            // Render the player face texture
            this.drawPlayerFace(matrices, x, y, player.getFaceTexture());
        }
    }

    private void renderLoadingIndicator(MatrixStack matrices, int x, int y) {
        // Draw a simple loading indicator - animated dots
        long time = System.currentTimeMillis();
        int dotCount = (int) ((time / 500) % 4); // Change every 500ms, cycle through 0-3

        for (int i = 0; i < 3; i++) {
            int dotX = x + 8 + i * 8;
            int dotY = y + 12;
            int color = (i == dotCount) ? 0xFFFFFFFF : 0x80FFFFFF;
            fill(matrices, dotX, dotY, dotX + 4, dotY + 4, color);
        }
    }

    private void drawPlayerFace(MatrixStack matrixStack, int x, int y, Identifier textureId) {
        // Bind the texture and render it, similar to how PlayerListHud renders player
        // faces
        this.client.getTextureManager().bindTexture(textureId);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        // Draw the face portion (8x8 pixels from the skin texture) scaled up
        // Face is located at coordinates (8, 8) in the 64x64 skin texture
        DrawableHelper.drawTexture(matrixStack, x, y, PLAYER_FACE_SIZE, PLAYER_FACE_SIZE, 8.0F, 8.0F, 8, 8, 64, 64);

        // Draw the head layer (hat/helmet) if present
        // Head layer is located at coordinates (40, 8) in the 64x64 skin texture
        // This is the same as the face but offset by 32 pixels to the right
        DrawableHelper.drawTexture(matrixStack, x, y, PLAYER_FACE_SIZE, PLAYER_FACE_SIZE, 40.0F, 8.0F, 8, 8, 64, 64);

        RenderSystem.disableBlend();
    }

    private void renderScrollbar(MatrixStack matrices) {
        int leftPanelWidth = getLeftPanelWidth();
        int scrollbarX = leftPanelWidth - 8;
        int scrollbarY = 50;
        int scrollbarHeight = this.height - 50;

        // Draw scrollbar background
        fill(matrices, scrollbarX, scrollbarY, scrollbarX + 8, scrollbarY + scrollbarHeight, 0x40000000);

        // Calculate thumb position and size
        int visiblePlayers = scrollbarHeight / PLAYER_ENTRY_HEIGHT;
        int totalPlayers = draaftState.getRoom().members().size();
        int thumbHeight = Math.max(20, (visiblePlayers * scrollbarHeight) / totalPlayers);
        int thumbY = scrollbarY + (scrollOffset * scrollbarHeight) / totalPlayers;

        // Draw scrollbar thumb
        fill(matrices, scrollbarX, thumbY, scrollbarX + 8, thumbY + thumbHeight, 0x80FFFFFF);
    }

    private int getStatusColor(ReadyStatus status) {
        return switch (status) {
            case READY -> 0xFF00FF00; // Green
            case NOT_READY -> 0xFFFF0000; // Red
            case DRAAFTING -> 0x80808080; // Grey
        };
    }

    private void renderStageIndicator(MatrixStack matrices) {
        Stage[] stages = Stage.values();
        int startY = 40; // Below the title
        int arrowSpacing = 10; // Space between stage name and arrow

        // Calculate total width needed
        int totalWidth = 0;
        for (int i = 0; i < stages.length; i++) {
            totalWidth += this.textRenderer.getWidth(stages[i].getDisplayName());
            if (i < stages.length - 1) {
                totalWidth += this.textRenderer.getWidth("→") + (arrowSpacing * 2); // Padding on both sides
            }
        }

        // Start position to center the entire indicator
        int currentX = (int) (((this.width - (this.width * LEFT_PANEL_WIDTH_RATIO) - totalWidth) / 2) + (this.width * LEFT_PANEL_WIDTH_RATIO));

        for (int i = 0; i < stages.length; i++) {
            Stage stage = stages[i];
            int color;

            if (stage == currentStage) {
                color = 0xFFFFFFFF; // White for current stage
            } else if (stage.ordinal() < currentStage.ordinal()) {
                color = 0x8800FF00; // Semi-transparent green for past stages
            } else {
                color = 0xFF909090; // Light grey for upcoming stages
            }

            // Draw stage name
            this.textRenderer.draw(matrices, stage.getDisplayName(), currentX, startY, color);
            currentX += this.textRenderer.getWidth(stage.getDisplayName());

            // Draw arrow between stages (except for the last one)
            if (i < stages.length - 1) {
                currentX += arrowSpacing; // Padding before arrow
                int arrowColor = stage.ordinal() < currentStage.ordinal() ? 0xFF404040 : 0xFF808080;

                // Animate the arrow if it's from current stage to next stage
                int arrowX = currentX;
                if (stage == currentStage) {
                    // Smooth back and forth animation without pauses
                    long time = System.currentTimeMillis();
                    float animationOffset = (float) ((float) Math.sin(time * 0.009) * 2.5); // Smoother movement
                    arrowX += (int) animationOffset;
                }

                this.textRenderer.draw(matrices, "→", arrowX, startY, arrowColor);
                currentX += this.textRenderer.getWidth("→") + arrowSpacing; // Arrow width + padding after
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int leftPanelWidth = getLeftPanelWidth();
        if (mouseX >= 0 && mouseX <= leftPanelWidth) {
            int visiblePlayers = (this.height - 50) / PLAYER_ENTRY_HEIGHT;
            int maxScroll = Math.max(0, draaftState.getRoom().members().size() - visiblePlayers);

            this.scrollOffset = (int) Math.max(0, Math.min(maxScroll, this.scrollOffset - amount));
            return true;
        }
        return false;
    }

    @Override
    public void onClose() {
        this.client.openScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private String generateRoomCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    private void copyRoomCode() {
        if (this.client != null && this.client.keyboard != null) {
            this.client.keyboard.setClipboard(this.roomCode);
        }
    }

    /*
     Stolen from [[net.minecraft.client.gui.screen.world.CreateWorldScreen.updateSaveFolderName]]
     */
    private void updateSaveFolderName() {
        if (this.saveDirectoryName.isEmpty()) {
            this.saveDirectoryName = "Draaft-World";
        }

        try {
            this.saveDirectoryName = FileNameUtil.getNextUniqueName(this.client.getLevelStorage().getSavesDirectory(), this.saveDirectoryName, "");
        } catch (Exception var4) {
            this.saveDirectoryName = "World";

            try {
                this.saveDirectoryName = FileNameUtil.getNextUniqueName(this.client.getLevelStorage().getSavesDirectory(), this.saveDirectoryName, "");
            } catch (Exception var3) {
                throw new RuntimeException("Could not create save folder", var3);
            }
        }
    }

    private void renderRoomCodeField(MatrixStack matrices, int mouseX, int mouseY) {
        // Draw "Room Code" label
        this.textRenderer.draw(matrices, "Room Code",
            this.roomCodeFieldX, this.roomCodeFieldY - 15, 0xFFFFFF);

        // Draw room code field background
        int fieldColor = 0x80000000;
        fill(matrices, this.roomCodeFieldX, this.roomCodeFieldY,
            this.roomCodeFieldX + this.roomCodeFieldWidth,
            this.roomCodeFieldY + this.roomCodeFieldHeight, fieldColor);

        // Draw field border
        this.drawHorizontalLine(matrices, this.roomCodeFieldX,
            this.roomCodeFieldX + this.roomCodeFieldWidth, this.roomCodeFieldY, 0xFFFFFFFF);
        this.drawHorizontalLine(matrices, this.roomCodeFieldX,
            this.roomCodeFieldX + this.roomCodeFieldWidth,
            this.roomCodeFieldY + this.roomCodeFieldHeight, 0xFFFFFFFF);
        this.drawVerticalLine(matrices, this.roomCodeFieldX,
            this.roomCodeFieldY, this.roomCodeFieldY + this.roomCodeFieldHeight, 0xFFFFFFFF);
        this.drawVerticalLine(matrices, this.roomCodeFieldX + this.roomCodeFieldWidth,
            this.roomCodeFieldY, this.roomCodeFieldY + this.roomCodeFieldHeight, 0xFFFFFFFF);

        // Draw room code text (obfuscated or revealed)
        String displayText = this.roomCodeRevealed ? this.roomCode : "******";
        int textColor = this.roomCodeRevealed ? 0xFFFFFF : 0x808080;
        this.textRenderer.draw(matrices, displayText,
            this.roomCodeFieldX + 4, this.roomCodeFieldY + 6, textColor);

        // Check if mouse is over the room code field for tooltip
        if (mouseX >= this.roomCodeFieldX && mouseX <= this.roomCodeFieldX + this.roomCodeFieldWidth &&
            mouseY >= this.roomCodeFieldY && mouseY <= this.roomCodeFieldY + this.roomCodeFieldHeight) {

            if (!this.roomCodeRevealed) {
                this.renderTooltip(matrices, new TranslatableText("draaft.draaftingScreen.tooltip.revealRoomCode"),
                    mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check if room code field was clicked
        if (button == 0 && // Left click
            mouseX >= this.roomCodeFieldX && mouseX <= this.roomCodeFieldX + this.roomCodeFieldWidth &&
            mouseY >= this.roomCodeFieldY && mouseY <= this.roomCodeFieldY + this.roomCodeFieldHeight) {

            this.roomCodeRevealed = !this.roomCodeRevealed;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

}
