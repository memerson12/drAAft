package draaft.client.models;

import com.mojang.authlib.GameProfile;
import draaft.client.gui.skin.SkinManager;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DraaftPlayer {
        private static final Logger logger = LogManager.getLogger();

        private final String uuid;
        private String username;
        private Identifier faceTexture;
        private boolean skinLoaded = false;
        private boolean skinLoading = false;
        private ReadyStatus readyStatus;

        public DraaftPlayer(String uuid) {
                this.uuid = uuid;
                this.readyStatus = ReadyStatus.DRAAFTING; // Default to Draafting
                loadSkin(); // Automatically start loading skin on instantiation
        }

        public String getUuid() {
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
                        GameProfile profile = SkinManager.getPlayerProfile(this.uuid);
                        if (profile == null) {
                                this.skinLoading = false;
                                logger.warn("Skin loading failed for {} (failed to fetch profile).", this.uuid);
                                return;
                        }
                        this.username = profile.getName();
                        SkinManager.fetchPlayerSkin(profile).thenAccept(skinId -> {
                                this.faceTexture = skinId;
                                this.skinLoaded = true;
                                this.skinLoading = false;
                        }).exceptionally(throwable -> {
                                this.skinLoading = false;
                                return null;
                        });
                }
        }
}
