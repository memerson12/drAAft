package draaft.client.ws;

import com.google.gson.Gson;
import draaft.client.DraaftServices;
import draaft.client.ServerClient;
import draaft.client.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static draaft.draaft.MOD_ID;

public class DraaftWebSocketClient {
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private final HttpClient httpClient;
    private final DraaftServices services;
    private final String token;
    private final ScheduledExecutorService scheduler;
    private final EventBus eventBus;
    private final Gson gson;

    private final AtomicReference<WebSocket> wsRef = new AtomicReference<>();
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private volatile boolean closed = false;

    // Config
    private final Duration pingInterval = Duration.ofSeconds(Long.getLong("DRAAFT_WS_PING_SECONDS", 30));
    private final int maxBackoffSeconds = 30;

    private ScheduledFuture<?> pingTask;

    public DraaftWebSocketClient(HttpClient httpClient, DraaftServices services, String token,
                                 EventBus eventBus) {
        this.httpClient = Objects.requireNonNull(httpClient);
        this.services = Objects.requireNonNull(services);
        this.token = Objects.requireNonNull(token);
        this.eventBus = Objects.requireNonNull(eventBus);
        this.gson = ServerClient.GSON; // reuse configured Gson
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "draaft-ws");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        if (Boolean.getBoolean("DRAAFT_WS_DISABLED")) {
            LOGGER.info("WS: disabled via system property");
            return;
        }
        closed = false;
        connect();
    }

    public void stop() {
        closed = true;
        cancelPing();
        WebSocket ws = wsRef.getAndSet(null);
        if (ws != null) {
            try {
                ws.sendClose(WebSocket.NORMAL_CLOSURE, "bye");
            } catch (Throwable ignored) {
            }
        }
        scheduler.shutdownNow();
    }

    private void connect() {
        String listenUrl = Utils.buildListenUri(services.apiBase().toString(), token);
        URI uri = URI.create(listenUrl);
        var uriString = uri.toString();

        var queryStart = uriString.indexOf('?');

        if (queryStart != -1) {
            uriString = uriString.substring(0, queryStart);
        }

        LOGGER.info("WS: connecting to {}", uriString);

        DraaftWebSocketListener listener = new DraaftWebSocketListener(eventBus, gson, this::scheduleReconnect);
        httpClient.newWebSocketBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .buildAsync(uri, listener)
            .whenComplete((ws, ex) -> {
                if (ex != null) {
                    LOGGER.warn("WS: connect failed", ex);
                    scheduleReconnect();
                } else {
                    wsRef.set(ws);
                    reconnectAttempts.set(0);
                    schedulePing();
                }
            });
    }

    private void schedulePing() {
        cancelPing();
        if (pingInterval.isZero() || pingInterval.isNegative())
            return;
        pingTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                WebSocket ws = wsRef.get();
                if (ws == null)
                    return;
                ws.sendPing(ByteBuffer.wrap(new byte[]{'p', 'i', 'n', 'g'}));
            } catch (Throwable t) {
                LOGGER.debug("WS: ping failed {}", t.getMessage());
            }
        }, pingInterval.toSeconds(), pingInterval.toSeconds(), TimeUnit.SECONDS);
    }

    public <T> void sendMessage(T object) {
        WebSocket ws = wsRef.get();
        if (ws == null) {
            // LOGGER.debug("No websocket existed to send text to!");
            return;
        }
        String s = gson.toJson(object, object.getClass());
        // LOGGER.debug("Sending information to server: {}", s);
        ws.sendText(s, true);
    }

    private void cancelPing() {
        if (pingTask != null) {
            pingTask.cancel(true);
            pingTask = null;
        }
    }

    private void scheduleReconnect() {
        if (closed)
            return;
        cancelPing();
        wsRef.set(null);
        int attempt = reconnectAttempts.getAndIncrement();
        int delay = Math.min(maxBackoffSeconds, (1 << Math.min(attempt, 5))); // cap exponential growth
        int jitter = ThreadLocalRandom.current().nextInt(0, 1000);
        long delayMs = delay * 1000L + jitter;
        LOGGER.info("WS: reconnecting in {} ms (attempt {})", delayMs, attempt + 1);
        scheduler.schedule(this::connect, delayMs, TimeUnit.MILLISECONDS);
    }
}
