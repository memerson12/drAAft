package draaft.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import draaft.client.ServerClient;
import draaft.client.models.DraaftPlayer;
import draaft.client.models.ReadyStatus;
import draaft.client.models.Room;
import draaft.client.ws.RoomEvent;
import draaft.client.ws.RoomEvent.RoomEventType;
import draaft.draaft;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;

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

@Environment(EnvType.CLIENT)
public class DraaftScreen extends Screen {
    private final static Logger logger = draaft.LOGGER;

    private final Screen parent;
    private List<DraaftPlayer> players;
    private int scrollOffset = 0;
    private static final int PLAYER_ENTRY_HEIGHT = 50;
    private static final int PLAYER_FACE_SIZE = 32;
    private static final double LEFT_PANEL_WIDTH_RATIO = 0.25; // 25% of screen width

    // Current game stage
    private Stage currentStage = Stage.DRAAFT;

    // Room code functionality
    private String roomCode;
    private boolean roomCodeRevealed = false;
    private ButtonWidget copyButton;
    private int roomCodeFieldX, roomCodeFieldY, roomCodeFieldWidth, roomCodeFieldHeight;


    public DraaftScreen(Screen parent) {
        super(new TranslatableText("draaft.draaftingScreen.title"));
        this.parent = parent;
        this.roomCode = generateRoomCode();
    }

    private int getLeftPanelWidth() {
        return (int) (this.width * LEFT_PANEL_WIDTH_RATIO);
    }

    @Override
    protected void init() {
        super.init();

        assert ServerClient.getInstance() != null;
        ServerClient serverClient = ServerClient.getInstance();
        Room room = serverClient.getRoom();

        if (room != null) {
            this.players = room.members();
            this.roomCode = room.code();
        } else {
            logger.warn("Room is null");
        }

        serverClient.addRoomEventListener(event -> {
            RoomEventType eventType = RoomEventType.valueOf(event.type().toUpperCase());
            logger.info("DraaftScreen received event of type {}: ", eventType);
            switch (eventType) {
                // Add new player
                case JOINED -> {
                    RoomEvent.PlayerJoined playerJoined = (RoomEvent.PlayerJoined) event;
                    logger.info(playerJoined.playerUuid());
                    DraaftPlayer newPlayer = new DraaftPlayer(playerJoined.playerUuid());
                    this.players.add(newPlayer);
                }

                // Remove player
                case LEFT -> {
                    RoomEvent.PlayerLeft playerLeft = (RoomEvent.PlayerLeft) event;
                    this.players.removeIf(player -> player.getUuid().equals(playerLeft.playerUuid()));
                }

                // Remove kicked player
                case KICK -> {
                    RoomEvent.PlayerKick playerKick = (RoomEvent.PlayerKick) event;
                    this.players.removeIf(player -> player.getUuid().equals(playerKick.playerUuid()));
                }
                default -> logger.warn("Unhandled event type in DraaftScreen: {}", event.type());
            }
        });

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
        this.copyButton = this.addButton(new ButtonWidget(
            this.roomCodeFieldX + this.roomCodeFieldWidth + 5, this.roomCodeFieldY, 50, 20,
            new TranslatableText("draaft.draaftingScreen.button.copy"),
            button -> copyRoomCode()));

        // Add Next Stage button above Ready button
        this.addButton(new ButtonWidget(
            this.width - 100, this.height - 55, 80, 20,
            new TranslatableText("draaft.draaftingScreen.button.nextStage"),
            button -> {
                // Cycle to next stage
                Stage[] stages = Stage.values();
                int nextIndex = (currentStage.ordinal() + 1) % stages.length;
                currentStage = stages[nextIndex];
            }));

        // Add Ready button in bottom right corner
        this.addButton(new ButtonWidget(
            this.width - 100, this.height - 30, 80, 20,
            new TranslatableText("draaft.draaftingScreen.button.ready"),
            button -> {
                // For now, this button doesn't do anything
                for (DraaftPlayer player : this.players) {
                    player.setReadyStatus(ReadyStatus.values()[(player.getReadyStatus().ordinal() + 1) % 3]);
                }
            }));
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
        this.renderPlayerList(matrices);

        // Draw room code field
        this.renderRoomCodeField(matrices, mouseX, mouseY);

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void renderPlayerList(MatrixStack matrices) {
        int startY = 20;
        int leftPanelWidth = getLeftPanelWidth();
        int visiblePlayers = (this.height - startY) / PLAYER_ENTRY_HEIGHT;

        for (int i = 0; i < Math.min(visiblePlayers, this.players.size() - scrollOffset); i++) {
            int playerIndex = i + scrollOffset;
            if (playerIndex >= this.players.size())
                break;

            DraaftPlayer player = this.players.get(playerIndex);
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
            matrices.pop();
        }

        // Draw scrollbar if needed
        if (this.players.size() > visiblePlayers) {
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
        int totalPlayers = this.players.size();
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
        int currentX = (this.width - totalWidth) / 2;

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
            int maxScroll = Math.max(0, this.players.size() - visiblePlayers);

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
