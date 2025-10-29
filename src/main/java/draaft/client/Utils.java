package draaft.client;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Utils {
    // might not be needed with parseUuid method
    public static String getLoggableUuid(String uuid) {
        return uuid.substring(0, 16) + "-" + uuid.substring(16);
    }

    /**
     * Parses a UUID from either dashed or 32-character hex string format.
     * Examples:
     * "810ad7db-704a-4603-9dd3-eaacd2908553"
     * "810ad7db704a46039dd3eaacd2908553"
     */
    public static String formatUuid(String input) {
        if (input == null) {
            throw new IllegalArgumentException("UUID string cannot be null");
        }

        String normalized = input.trim();

        // Case 1: canonical UUID format with dashes
        if (normalized.contains("-")) {
            return normalized;
        }

        // Case 2: 32 hex chars (no dashes)
        if (normalized.length() == 32 && normalized.matches("[0-9a-fA-F]{32}")) {
            BigInteger bi = new BigInteger(normalized, 16);
            return new UUID(bi.shiftRight(64).longValue(), bi.longValue()).toString();
        }

        throw new IllegalArgumentException("Invalid UUID format: " + input);
    }

    public static String toWsUri(String apiBase) {
        if (apiBase.startsWith("https://"))
            return "wss://" + apiBase.substring(8);
        if (apiBase.startsWith("http://"))
            return "ws://" + apiBase.substring(7);
        return apiBase;
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
