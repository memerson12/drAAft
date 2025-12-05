package draaft.client.gui.skin;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import draaft.client.ServerClient;
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
        LOGGER.info("Initializing user cache...");
        if (userCache == null) {
            LOGGER.info("Initializing user cache... (was NULL)");
            synchronized (userCacheLock) {
                if (userCache == null) {
                    System.out.println("Initializing UserCache");
                    MinecraftClient client = MinecraftClient.getInstance();
                    Proxy proxy = client.netProxy;
                    LOGGER.info("Using proxy for UserCache: {}", proxy);
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
        } else {
            LOGGER.info("User cache already initialized.");
        }
    }

    public static Identifier getSkin(UUID uuid) {
        Identifier skin = SKIN_CACHE.getIfPresent(uuid.toString());
       return skin != null ? skin : getDefaultSkin();
    }

    /**
     * Fetches a player skin asynchronously using Minecraft's built-in
     * PlayerSkinProvider
     */
    public static CompletableFuture<Identifier> fetchPlayerSkin(GameProfile profile) {
        return CompletableFuture.supplyAsync(() -> {
            String uuid = profile.getId().toString();
            try {
                // Check cache first
                Identifier cachedSkin = SKIN_CACHE.getIfPresent(uuid);
                if (cachedSkin != null) {
                    return cachedSkin;
                }

                MinecraftClient client = MinecraftClient.getInstance();
                PlayerSkinProvider skinProvider = client.getSkinProvider();

                // Load skin using the built-in system
                CompletableFuture<Identifier> skinFuture = new CompletableFuture<>();
                skinProvider.loadSkin(profile, (type, identifier, texture) -> {
                    System.out.println("type=" + type + ", identifier=" + identifier + ", texture=" + texture);
                    if (type == MinecraftProfileTexture.Type.SKIN) {
                        SKIN_CACHE.put(uuid, identifier);
                        skinFuture.complete(identifier);
                    }
                }, false);

                // Wait for the skin to load (with timeout)
                try {
                    return skinFuture.get(10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    LOGGER.warn(e);
                    LOGGER.warn("1 Timeout or error loading skin for {}: {}", uuid, e.getMessage());
                    return getDefaultSkin();
                }

            } catch (Exception e) {
                LOGGER.error("2 Error fetching skin for player {}: {}", uuid, e.getMessage());
                return getDefaultSkin();
            }
        }, Util.getServerWorkerExecutor());
    }

    public static GameProfile getPlayerProfile(UUID uuid) {
        try {
            // Initialize UserCache if needed
            initializeUserCache();

            if(userCache.getByUuid(uuid) != null) {
                return userCache.getByUuid(uuid);
            }

            LOGGER.info("Getting player profile for {}", uuid);
            String username = ServerClient.getInstance().getUsernameFromUUID(uuid);
            GameProfile profile = new GameProfile(uuid, username);
            userCache.add(profile);
            return profile;

        } catch (Exception e) {
            LOGGER.error("Error fetching profile for {}: {}", uuid, e.getMessage());
            return new GameProfile(uuid, "Unknown");
        }
    }

    private static Identifier getDefaultSkin() {
        // Return a default skin identifier (Steve skin)
        return new Identifier("textures/entity/steve.png");
    }
}
