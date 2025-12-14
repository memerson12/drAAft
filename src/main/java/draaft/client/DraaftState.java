package draaft.client;

import com.google.gson.JsonElement;
import com.mojang.util.UUIDTypeAdapter;
import dev.menx.worldimporter.RegionId;
import draaft.client.gui.DraaftToast;
import draaft.client.models.DraaftPlayer;
import draaft.client.models.Room;
import draaft.client.world.DraaftWorldSpec;
import draaft.client.world.Worlds;
import draaft.client.ws.events.GameEvent;
import draaft.client.ws.events.RoomMemberEvents;
import draaft.draaft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

public class DraaftState {

    private static DraaftState INSTANCE;

    private final Logger logger = draaft.LOGGER;

    private STATE currentState = STATE.WAITING_FOR_ROOM;
    private final ServerClient serverClient = ServerClient.getInstance();
    private Room room;

    public int advancementCount = 0;

    public HashMap<String, Integer> advancementCounts = new HashMap<String, Integer>();

    private DraaftState() {
        serverClient.addGameStateEventListener(event -> {
            GameEvent.GameEventType eventType = event.type();
            logger.info("Draaft Game received event of type {}: ", eventType);
            switch (eventType) {
                case ADVANCEMENTCOUNT -> {
                    GameEvent.AdvancementCount advCount = (GameEvent.AdvancementCount) event;
                    advancementCounts.put(advCount.playerUuid().toString(), advCount.count());
                }

                default -> logger.warn("Unhandled event type in Draaft Game: {}", event.type());
            }
        });

        serverClient.addRoomMemberEventListener(event -> {
            RoomMemberEvents.RoomEventType eventType = event.type();
            logger.info("DraaftScreen received event of type {}: ", eventType);
            ArrayList<DraaftPlayer> players = this.room.members();
            switch (eventType) {
                // Add new player
                case JOINED -> {
                    RoomMemberEvents.PlayerJoined playerJoined = (RoomMemberEvents.PlayerJoined) event;
                    logger.info(playerJoined.playerUuid());
                    DraaftPlayer newPlayer = new DraaftPlayer(playerJoined.playerUuid());
                    players.add(newPlayer);
                }

                // Remove player
                case LEFT -> {
                    RoomMemberEvents.PlayerLeft playerLeft = (RoomMemberEvents.PlayerLeft) event;
                    players.removeIf(player -> player.getUuid().equals(playerLeft.playerUuid()));
                }

                // Remove kicked player
                case KICK -> {
                    RoomMemberEvents.PlayerKick playerKick = (RoomMemberEvents.PlayerKick) event;
                    players.removeIf(player -> player.getUuid().equals(playerKick.playerUuid()));
                }
                default -> logger.warn("Unhandled event type in DraaftScreen: {}", event.type());
            }

            // TODO: Maybe we want to fetch the whole room again instead of updating locally? Or do that on a scheduled basis to fix if we get in a bad state?
            this.room = new Room(
                this.room.code(),
                players,
                this.room.admin(),
                this.room.config()
            );
        });

        serverClient.addRoomStateEventListener(event -> {
            switch (event.type()) {
                case CLOSED, COMMENCED, CONFIG -> {
                    // TODO?
                }
                case DRAFT_COMPLETE -> createWorld();
            }
        });
    }

    public boolean inDraaftWorld() {
        return true; // TODO - We need to know if the world we're in is a draaft world
    }

    public boolean isTournament() {
        return true; // TODO - We need to know if the world we're in is a tournament'
    }

    public enum STATE {
        WAITING_FOR_ROOM,
        IN_ROOM,
        DRAAFTING,
        PREPARING_GAME,
        IN_GAME,
        SPECTATING,
        GAME_OVER
    }

    public void addAdvancement(String advancementName) {
        serverClient.addAdvancement(advancementName);
        this.advancementCount++;
    }

    public static DraaftState getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DraaftState();
        }
        return INSTANCE;
    }

    public static void with(Consumer<DraaftState> func) {
        if (INSTANCE == null && !ServerClient.hasInstance()) {
            return;
        }
        func.accept(getInstance());
    }

    public static boolean isAccessible() {
        return INSTANCE != null || ServerClient.hasInstance();
    }

    public STATE getCurrentState() {
        return currentState;
    }

    public void setCurrentState(STATE currentState) {
        this.currentState = currentState;
    }

    public Room getRoom() {
        if (this.room == null) {
            this.room = serverClient.getRoom();
        }
        return room;
    }

    public Room refreshRoomFromServer() {
        this.room = serverClient.getRoom();
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    private void createWorld() {
        HttpResponse<JsonElement> worldGen;

        try {
            worldGen = this.serverClient.httpSend(
                this.serverClient.authenticatedHttpRequestBuilder("draft/worldgen")
                    .GET()
                    .build(),

                Utils.jsonBody(JsonElement.class)
            );
        } catch (IOException | InterruptedException e) {
            this.logger.error("failed to get worldgen settings");
            DraaftToast.showError(
                new TranslatableText("draaft.preparing.worldGenLoadError"),
                Text.of(e.toString())
            );
            return;
        }

        HttpResponse<InputStream> datapackResponse;

        try {
            datapackResponse = this.serverClient.httpSend(
                this.serverClient.authenticatedHttpRequestBuilder("draft/download")
                    .GET()
                    .build(),
                HttpResponse.BodyHandlers.ofInputStream()
            );
        } catch (IOException | InterruptedException e) {
            this.logger.error("failed to download the datapack", e);
            DraaftToast.showError(
                new TranslatableText("draaft.preparing.datapackDownloadError"),
                Text.of(e.toString())
            );
            return;
        }

        var room = this.getRoom();

        var ownUuid = UUIDTypeAdapter.fromString(MinecraftClient.getInstance().getSession().getUuid());

        var otherPlayers = room.members()
            .stream()
            .filter(player -> !player.getUuid().equals(ownUuid))
            .map(DraaftPlayer::getUsername)
            .toList();

        var world = DraaftWorldSpec.fromJson(worldGen.body(), new RegionId[0], datapackResponse.body());

        MinecraftClient.getInstance().execute(() -> Worlds.create(world, room.code(), otherPlayers));
    }
}
