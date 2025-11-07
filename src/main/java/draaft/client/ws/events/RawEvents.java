package draaft.client.ws.events;

import com.google.gson.JsonObject;

public interface RawEvents {
    String type();

    record raw(String type, JsonObject payload) implements RawEvents {
    }
}

