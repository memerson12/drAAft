package draaft.compat.standardsettings;

import me.contaria.standardsettings.StandardSettings;
import net.minecraft.client.MinecraftClient;

public abstract class StandardSettingsCompat {
    public static void reset() {
        // see:
        // https://github.com/contariaa/StandardSettings/blob/baf5d22dca6ad58a9f185525c2f63fad6ab3808a/src/main/java/me/contaria/standardsettings/mixin/MinecraftClientMixin.java#L43-L55

        if (!MinecraftClient.getInstance().isOnThread()) {
            return;
        }

        StandardSettings.createCache();

        if (StandardSettings.isEnabled()) {
            StandardSettings.reset();
        }
    }

    public static void onWorldJoin(String worldName) {
        // see:
        // https://github.com/contariaa/StandardSettings/blob/baf5d22dca6ad58a9f185525c2f63fad6ab3808a/src/main/java/me/contaria/standardsettings/mixin/MinecraftClientMixin.java#L57-L74

        var client = MinecraftClient.getInstance();

        if (!client.isOnThread()) {
            return;
        }

        StandardSettings.saveToWorldFile(worldName);

        if (StandardSettings.isEnabled()) {
            if (client.isWindowFocused()) {
                StandardSettings.onWorldJoin();
            } else {
                StandardSettings.onWorldJoinPending = true;
                StandardSettings.autoF3EscPending = StandardSettings.config.autoF3Esc;
            }
        }
    }
}
