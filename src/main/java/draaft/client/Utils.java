package draaft.client;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Utils {
    public static String toWsUri(String apiBase) {
        if (apiBase.startsWith("https://"))
            return "wss://" + apiBase.substring(8);
        if (apiBase.startsWith("http://"))
            return "ws://" + apiBase.substring(7);
        throw new IllegalArgumentException("API base must start with http:// or https://");
    }

    public static String buildListenUri(String apiBase, String token) {
        String base = toWsUri(apiBase);
        // ensure no trailing slash duplication
        if (base.endsWith("/"))
            base = base.substring(0, base.length() - 1);
        String q = URLEncoder.encode(token, StandardCharsets.UTF_8);
        return base + "/listen?token=" + q;
    }
}
