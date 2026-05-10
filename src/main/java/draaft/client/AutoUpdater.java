package draaft.client;

import draaft.client.gui.DraaftToast;
import draaft.client.integration.GitHub;
import draaft.draaft;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModOrigin;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public abstract class AutoUpdater {
    private static final Logger LOGGER = LogManager.getLogger(draaft.MOD_ID + "-updater");

    public static boolean shouldCheckForUpdates() {
        return System.getProperty("DRAAFT_DISABLE_AUTOUPDATE") == null
            && draaftOrigin().getKind() == ModOrigin.Kind.PATH
            && draaftOrigin().getPaths().size() == 1;
    }

    public static @Nullable DraaftRelease checkForUpdates(DraaftServices draaftServices) {
        if (draaftServices.githubRepo() == null) {
            return null;
        }

        var gh = new GitHub(draaftServices.githubRepo());

        try {
            var latest = gh.releases()
                .stream()
                .flatMap(ghRelease -> {
                    var release = DraaftRelease.tryFromGitHub(ghRelease);

                    return release != null
                        ? Stream.of(release)
                        : Stream.empty();
                })
                .filter(release -> DraaftRelease.CURRENT.compareTo(release) < 0)
                .sorted()
                .reduce((acc, release) -> release); // find the last item

            return latest.orElse(null);
        } catch (IOException | InterruptedException | GitHub.GitHubException e) {
            LOGGER.warn("Update check failed", e);

            DraaftToast.showError(
                new TranslatableText("draaft.login.update.checkFailed"),
                Text.of(e.toString())
            );

            return null;
        }
    }

    public static boolean update(DraaftServices draaftServices, DraaftRelease release) {
        var stream = downloadUpdate(draaftServices, release);

        if (stream == null) {
            return false;
        }

        try {
            var oldJar = draaftOrigin().getPaths().get(0);

            var target = oldJar.resolveSibling(release.jarName());

            var tempFile = Files.createTempFile("draaft-", ".tmp");
            tempFile.toFile().deleteOnExit();

            Files.copy(stream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            // TODO(me-nx): hash check?

            Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING);

            Files.delete(oldJar);

            return true;
        } catch (IOException e) {
            LOGGER.error("Exception occurred while downloading an update", e);
            DraaftToast.showError(
                new TranslatableText("draaft.login.update.failedToUpdate"),
                Text.of(e.toString())
            );

            return false;
        }
    }

    private static @Nullable InputStream downloadUpdate(DraaftServices draaftServices, DraaftRelease release) {
        assert draaftServices.githubRepo() != null;

        var gh = new GitHub(draaftServices.githubRepo());

        try {
            var assets = gh.releaseAssets(release.id());

            var asset = getUpdateAsset(release, assets);

            if (asset == null) {
                return null;
            }

            return gh.downloadReleaseAsset(asset.id());
        } catch (IOException | InterruptedException | GitHub.GitHubException e) {
            throw new RuntimeException(e);
        }
    }

    private static @Nullable GitHub.ReleaseAsset getUpdateAsset(DraaftRelease release, List<GitHub.ReleaseAsset> assets) {
        var matchingAssets = assets.stream()
            .filter(asset -> asset.name().endsWith(".jar") && !asset.name().endsWith("-sources.jar"))
            .toList();

        if (matchingAssets.size() != 1) {
            LOGGER.error(
                "Invalid update `{}` #{}, unexpected asset count ({}).",
                release.toString(),
                release.id,
                matchingAssets.size()
            );

            DraaftToast.showError(
                new TranslatableText("draaft.login.update.failedToUpdate"),
                new TranslatableText("draaft.login.update.failedToUpdate.invalidUpdate", release.toString())
            );

            return null;
        } else {
            return matchingAssets.get(0);
        }
    }

    private static ModOrigin draaftOrigin() {
        //noinspection OptionalGetWithoutIsPresent
        return FabricLoader.getInstance().getModContainer(draaft.MOD_ID).get().getOrigin();
    }

    public record DraaftRelease(int id, int minor, int patch, @Nullable String prerelease, String htmlUrl) implements Comparable<DraaftRelease> {
        private static final Pattern TAG_NAME = Pattern.compile("^v?2\\.(?<minor>\\d+)\\.(?<patch>\\d+)(?<prerelease>-.+)?$");

        @NotNull
        public static final DraaftRelease CURRENT = Objects.requireNonNull(
            DraaftRelease.tryFromGitHub(new GitHub.Release(
                draaft.DRAAFT_VERSION,
                0,
                ""
            ))
        );

        public static @Nullable DraaftRelease tryFromGitHub(GitHub.Release release) {
            var matcher = TAG_NAME.matcher(release.tagName());

            boolean matchesFound = matcher.matches();

            if (!matchesFound) {
                return null;
            }

            return new DraaftRelease(
                release.id(),
                Integer.parseInt(matcher.group("minor")),
                Integer.parseInt(matcher.group("patch")),
                matcher.group("prerelease"),
                release.htmlUrl()
            );
        }

        public String jarName() {
            return "draaft-%s.jar".formatted(this.toString());
        }

        @Override
        public @NotNull String toString() {
            return "v2.%d.%d%s".formatted(this.minor, this.patch, this.prerelease);
        }

        @Override
        public int compareTo(@NotNull AutoUpdater.DraaftRelease o) {
            return cmpOr(
                this.minor,
                o.minor,
                () -> cmpOr(
                    this.patch,
                    o.patch,
                    () -> cmpOpt(this.prerelease, o.prerelease)
                )
            );
        }

        private static <T extends Comparable<T>> int cmpOr(T a, T b, IntSupplier eqFallback) {
            var cmp = a.compareTo(b);

            return cmp == 0
                ? eqFallback.getAsInt()
                : cmp;
        }

        private static <T extends Comparable<T>> int cmpOpt(@Nullable T a, @Nullable T b) {
            if (a == null) {
                return b == null
                    ? 0
                    : 1;
            } else if (b == null) {
                return -1;
            }

            return a.compareTo(b);
        }
    }
}
