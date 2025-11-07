package draaft.client.ws;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.util.UUIDTypeAdapter;
import draaft.client.models.RoomConfig;
import draaft.client.ws.events.DraftPickEvents;
import draaft.client.ws.events.RawEvents;
import draaft.client.ws.events.RoomMemberEvents;
import draaft.client.ws.events.RoomStateEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.http.WebSocket;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import static draaft.draaft.MOD_ID;

public class DraaftWebSocketListener implements WebSocket.Listener {
    private static final Logger logger = LogManager.getLogger(MOD_ID);

    private final EventBus eventBus;
    private final Gson gson;
    private final Runnable onClosedOrError;
    private final StringBuilder textAccumulator = new StringBuilder();

    public DraaftWebSocketListener(EventBus eventBus, Gson gson, Runnable onClosedOrError) {
        this.eventBus = eventBus;
        this.gson = gson;
        this.onClosedOrError = onClosedOrError;
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        logger.info("WS: opened {}", webSocket);
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        textAccumulator.append(data);
        if (last) {
            String message = textAccumulator.toString();
            textAccumulator.setLength(0);
            try {
                logger.info("Got message: {}", message);
                JsonElement el = gson.fromJson(message, JsonElement.class);
                if (el != null && el.isJsonObject()) {
                    JsonObject obj = el.getAsJsonObject();
                    String variant = getOrElse(obj, "variant", "unknown");

                    // Try to map to known types, otherwise emit raw
                    switch (variant) {
                        case "playerupdate" -> {
                            String action = getOrElse(obj, "action", "unknown");
                            String rawUuid = getOrElse(obj, "uuid", null);
                            if (rawUuid == null) {
                                logger.warn("Unable to process playerupdate event with missing uuid: {}", message);
                                eventBus.post(new RawEvents.raw(variant, obj));
                                break;
                            }
                            UUID uuid = UUIDTypeAdapter.fromString(rawUuid);
                            logger.info("Parsed action {} for player {}", action, uuid);
                            switch (action) {
                                case "joined" -> eventBus.post(new RoomMemberEvents.PlayerJoined(uuid));
                                case "leave" -> eventBus.post(new RoomMemberEvents.PlayerLeft(uuid));
                                case "kick" -> eventBus.post(new RoomMemberEvents.PlayerKick(uuid));
                                case "spectator" -> eventBus.post(new RoomMemberEvents.PlayerBecomeSpectator(uuid));
                                case "player" -> eventBus.post(new RoomMemberEvents.PlayerBecomePlayer(uuid));
                                default -> {
                                    logger.warn("WS: unknown playerupdate action: {}", action);
                                    eventBus.post(new RawEvents.raw(variant, obj));
                                }
                            }
                        }
                        case "roomupdate" -> {
                            String updateType = getOrElse(obj, "update", "unknown");
                            switch (updateType) {
                                case "closed" -> eventBus.post(new RoomStateEvents.closed());
                                case "config" -> {
                                    RoomConfig config = gson.fromJson(obj.getAsJsonObject("config"), RoomConfig.class);
                                    eventBus.post(new RoomStateEvents.configUpdate(config));
                                }
                                case "commenced" -> eventBus.post(new RoomStateEvents.commenced());
                                default -> {
                                    logger.warn("WS: unknown roomupdate update type: {}", updateType);
                                    eventBus.post(new RawEvents.raw(variant, obj));
                                }
                            }
                        }
                        case "draftpick" -> {
                            String pickKey = getOrElse(obj, "key", null);
                            String pickerRawUuid = getOrElse(obj, "picker_uuid", null);
                            int index = obj.has("index") ? obj.get("index").getAsInt() : -1;
                            if (pickKey == null || pickerRawUuid == null || index == -1) {
                                logger.warn("Unable to process draftpick event with missing fields: {}", message);
                                eventBus.post(new RawEvents.raw(variant, obj));
                                break;
                            }
                            UUID pickerUuid = UUIDTypeAdapter.fromString(pickerRawUuid);
                            eventBus.post(new DraftPickEvents.Pick(pickerUuid, pickKey, index));
                        }
                        default -> eventBus.post(new RawEvents.raw(variant, obj));
                    }
                } else {
                    logger.warn("WS: unexpected non-object message: {}", message);
                }
            } catch (Throwable t) {
                logger.error("WS: failed parsing message: {}", t.getMessage());
            }
        }
        webSocket.request(1);
        return null;
    }

    private String getOrElse(JsonObject obj, String key, String defaultValue) {
        return obj.has(key) ? obj.get(key).getAsString() : defaultValue;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        logger.warn("WS: error {}", error.getMessage());
        onClosedOrError.run();
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        logger.info("WS: closed code={} reason={}", statusCode, reason);
        onClosedOrError.run();
        return null;
    }
}
