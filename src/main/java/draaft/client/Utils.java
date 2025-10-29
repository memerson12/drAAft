package draaft.client;

import java.math.BigInteger;
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
    public static UUID parseUuid(String input) {
        if (input == null) {
            throw new IllegalArgumentException("UUID string cannot be null");
        }

        String normalized = input.trim();

        // Case 1: canonical UUID format with dashes
        if (normalized.contains("-")) {
            return UUID.fromString(normalized);
        }

        // Case 2: 32 hex chars (no dashes)
        if (normalized.length() == 32 && normalized.matches("[0-9a-fA-F]{32}")) {
            BigInteger bi = new BigInteger(normalized, 16);
            return new UUID(bi.shiftRight(64).longValue(), bi.longValue());
        }

        throw new IllegalArgumentException("Invalid UUID format: " + input);
    }
}
