package draaft.client.gui.skin;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.*;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.UserCache;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Environment(EnvType.CLIENT)
public class SkinManager {
    private static final Logger LOGGER = LogManager.getLogger();

    // Cache for skin textures to avoid repeated API calls
    private static final Cache<String, Identifier> SKIN_CACHE = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    // Minecraft's built-in UserCache for profile lookups
    private static volatile UserCache userCache;
    private static final Object userCacheLock = new Object();

    /**
     * Initialize the UserCache with Minecraft's authentication system
     */
    private static void initializeUserCache() {
        if (userCache == null) {
            synchronized (userCacheLock) {
                if (userCache == null) {
                    System.out.println("Initializing UserCache");
                    MinecraftClient client = MinecraftClient.getInstance();
                    Proxy proxy = client.netProxy;

                    YggdrasilAuthenticationService yggdrasilAuthenticationService = new YggdrasilAuthenticationService(
                            proxy,
                            UUID.randomUUID().toString());
                    GameProfileRepository gameProfileRepository = yggdrasilAuthenticationService
                            .createProfileRepository();

                    // Use the same cache file location as Minecraft
                    File cacheFile = new File(client.runDirectory, MinecraftServer.USER_CACHE_FILE.getName());
                    userCache = new UserCache(gameProfileRepository, cacheFile);
                }
            }
        }
    }

    /**
     * Fetches a player skin asynchronously using Minecraft's built-in
     * PlayerSkinProvider
     */
    public static CompletableFuture<Identifier> fetchPlayerSkin(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check cache first
                Identifier cachedSkin = SKIN_CACHE.getIfPresent(username);
                if (cachedSkin != null) {
                    return cachedSkin;
                }

                // Get GameProfile for the username
                GameProfile profile = getPlayerProfile(username);
                if (profile == null) {
                    LOGGER.warn("Could not find profile for player: {}", username);
                    return getDefaultSkin();
                }
                MinecraftClient client = MinecraftClient.getInstance();
                PlayerSkinProvider skinProvider = client.getSkinProvider();

                // Load skin using the built-in system
                CompletableFuture<Identifier> skinFuture = new CompletableFuture<>();
                skinProvider.loadSkin(profile, (type, identifier, texture) -> {
                    if (type == MinecraftProfileTexture.Type.SKIN) {
                        SKIN_CACHE.put(username, identifier);
                        skinFuture.complete(identifier);
                    }
                }, false);

                // Wait for the skin to load (with timeout)
                try {
                    return skinFuture.get(10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    LOGGER.warn("Timeout or error loading skin for {}: {}", username, e.getMessage());
                    return getDefaultSkin();
                }

            } catch (Exception e) {
                LOGGER.error("Error fetching skin for player {}: {}", username, e.getMessage());
                return getDefaultSkin();
            }
        }, Util.getServerWorkerExecutor());
    }

    private static GameProfile getPlayerProfile(String username) {
        try {
            // Initialize UserCache if needed
            initializeUserCache();

            // Use Minecraft's built-in UserCache to find the profile
            // UserCache handles its own caching internally
            System.out.println("Looking up profile for: " + username);
            GameProfile profile = userCache.findByName(username);
            if (profile != null && profile.getId() != null) {
                System.out.println(profile);
                return profile;
            } else {
                return null;
            }

        } catch (Exception e) {
            LOGGER.error("Error fetching profile for {}: {}", username, e.getMessage());
            return null;
        }
    }

    private static Identifier getDefaultSkin() {
        // Return a default skin identifier (Steve skin)
        return new Identifier("textures/entity/steve.png");
    }
}
