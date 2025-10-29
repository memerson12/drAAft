package draaft.client.ws;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import draaft.client.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;

import static draaft.draaft.MOD_ID;

public class DraaftWebSocketListener implements WebSocket.Listener {
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private final RoomEventDispatcher dispatcher;
    private final Gson gson;
    private final Runnable onClosedOrError;
    private final StringBuilder textAccumulator = new StringBuilder();

    public DraaftWebSocketListener(RoomEventDispatcher dispatcher, Gson gson, Runnable onClosedOrError) {
        this.dispatcher = dispatcher;
        this.gson = gson;
        this.onClosedOrError = onClosedOrError;
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        LOGGER.info("WS: opened {}", webSocket);
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        textAccumulator.append(data);
        if (last) {
            String message = textAccumulator.toString();
            textAccumulator.setLength(0);
            try {
                LOGGER.info("Got message: {}", message);
                JsonElement el = gson.fromJson(message, JsonElement.class);
                if (el != null && el.isJsonObject()) {
                    JsonObject obj = el.getAsJsonObject();
                    String variant = getOrElse(obj, "variant", "unknown");

                    // Try to map to known types, otherwise emit raw
                    switch (variant) {
                        case "playerupdate" -> {
                            String action = getOrElse(obj, "action", "unknown");
                            String rawUuid = getOrElse(obj, "uuid", null);
                            if(rawUuid == null) {
                                LOGGER.warn("Unable to process playerupdate event with missing uuid: {}", message);
                                dispatcher.emit(new RoomEvent.Raw(variant, obj));
                                break;
                            }
                            String uuid = Utils.formatUuid(rawUuid);
                            LOGGER.info("Parsed action {} for player {}", action, uuid);
                            switch (action) {
                                case "joined" -> dispatcher.emit(new RoomEvent.PlayerJoined(uuid));
                                case "leave" -> dispatcher.emit(new RoomEvent.PlayerLeft(uuid));
                                case "kick" -> dispatcher.emit(new RoomEvent.PlayerKick(uuid));
                                case "spectator" -> dispatcher.emit(new RoomEvent.PlayerBecomeSpectator(uuid));
                                case "player" -> dispatcher.emit(new RoomEvent.PlayerBecomePlayer(uuid));
                                default -> {
                                    LOGGER.warn("WS: unknown playerupdate action: {}", action);
                                    dispatcher.emit(new RoomEvent.Raw(variant, obj));
                                }
                            }
                        }
                        default -> dispatcher.emit(new RoomEvent.Raw(variant, obj));
                    }
                } else {
                    LOGGER.warn("WS: unexpected non-object message: {}", message);
                }
            } catch (Throwable t) {
                LOGGER.error("WS: failed parsing message: {}", t.getMessage());
            }
        }
        webSocket.request(1);
        return null;
    }

    private String getOrElse(JsonObject obj, String key, String defaultValue) {
        return obj.has(key) ? obj.get(key).getAsString() : defaultValue;
    }

    @Override
    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
        webSocket.request(1);
        return null;
    }

    @Override
    public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
        webSocket.request(1);
        return null;
    }

    @Override
    public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
        webSocket.request(1);
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        LOGGER.warn("WS: error {}", error.getMessage());
        onClosedOrError.run();
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        LOGGER.info("WS: closed code={} reason={}", statusCode, reason);
        onClosedOrError.run();
        return null;
    }
}

