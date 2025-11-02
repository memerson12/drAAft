package draaft.client.models;

import com.mojang.authlib.GameProfile;
import draaft.client.gui.skin.SkinManager;
import draaft.draaft;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public class DraaftPlayer {
    private static final Logger logger = draaft.LOGGER;

    private final UUID uuid;
    private String username;
    private Identifier faceTexture;
    private boolean skinLoaded = false;
    private boolean skinLoading = false;
    private ReadyStatus readyStatus;

    public DraaftPlayer(UUID uuid) {
        this.uuid = uuid;
        this.readyStatus = ReadyStatus.DRAAFTING; // Default to Draafting
        loadSkin(); // Automatically start loading skin on instantiation
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public Identifier getFaceTexture() {
        return faceTexture;
    }

    public boolean isSkinLoaded() {
        return skinLoaded;
    }

    public boolean isSkinLoading() {
        return skinLoading;
    }

    public ReadyStatus getReadyStatus() {
        return readyStatus;
    }

    public void setReadyStatus(ReadyStatus readyStatus) {
        // TODO: Upsert ready status to backend
        this.readyStatus = readyStatus;
    }

    public void loadSkin() {
        if (!skinLoading && !skinLoaded) {
            skinLoading = true;
            // The Minecraft code base has no way of fetching username from UUID. We should get username from backend instead.
//            GameProfile profile;
//            profile = SkinManager.getPlayerProfile(this.uuid);
//            if (profile == null) {
//                logger.warn("Skin loading failed for {} (failed to fetch profile). Falling back to incomplete profile", Utils.getLoggableUuid(this.uuid));
//                profile = new GameProfile(UUID.fromString(this.uuid), null);
//            }
            GameProfile profile = new GameProfile(this.uuid, "Temp Names");
            this.username = profile.getName();
            SkinManager.fetchPlayerSkin(profile).thenAccept(skinId -> {
                logger.info("Skin loaded for {}", this.username);
                this.faceTexture = skinId;
                this.skinLoaded = true;
                this.skinLoading = false;
            }).exceptionally(throwable -> {
                this.skinLoading = false;
                logger.warn("Skin loading failed for {}: {}", this.uuid, throwable.getMessage());
                return null;
            });
        }
    }
}
