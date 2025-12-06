package draaft.client;

import draaft.client.models.DraaftPlayer;
import draaft.client.models.Room;
import draaft.client.ws.events.RoomMemberEvents;
import draaft.draaft;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.function.Consumer;

public class DraaftState {

    private static DraaftState INSTANCE;

    private final Logger logger = draaft.LOGGER;

    private STATE currentState = STATE.WAITING_FOR_ROOM;
    private final ServerClient serverClient = ServerClient.getInstance();
    private Room room;

    public int advancementCount = 0;

    private DraaftState() {
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

}
