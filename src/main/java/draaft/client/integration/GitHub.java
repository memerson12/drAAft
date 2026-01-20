package draaft.client.integration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import draaft.client.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public record GitHub(String repo) {
    private static final URI BASE = URI.create("https://api.github.com/");
    private static final String API_VERSION = "2022-11-28";
    private static final HttpClient HTTP = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Release.class, Release.DESERIALIZER)
        .registerTypeAdapter(ReleaseAsset.class, ReleaseAsset.DESERIALIZER)
        .create();

    public List<Release> releases() throws IOException, InterruptedException, GitHubException {
        var req = reposRequest("releases")
            .GET()
            .build();

        var resp = HTTP.send(req, Utils.jsonBody(Release[].class, GSON));

        ensureStatusCode(resp);

        return List.of(resp.body());
    }

    public List<ReleaseAsset> releaseAssets(int releaseId) throws IOException, InterruptedException, GitHubException {
        var req = reposRequest("releases/%d/assets".formatted(releaseId))
            .GET()
            .build();

        var resp = HTTP.send(req, Utils.jsonBody(ReleaseAsset[].class, GSON));

        ensureStatusCode(resp);

        return List.of(resp.body());
    }

    public InputStream downloadReleaseAsset(int assetId) throws IOException, InterruptedException, GitHubException {
        var req = reposRequest("releases/assets/" + assetId)
            .header("Accept", "application/octet-stream")
            .GET()
            .build();

        var resp = HTTP.send(req, HttpResponse.BodyHandlers.ofInputStream());

        ensureStatusCode(resp);

        return resp.body();
    }

    private HttpRequest.Builder reposRequest(String relativeUri) {
        var repoUrl = this.repo.endsWith("/")
            ? this.repo
            : this.repo + '/';

        return HttpRequest.newBuilder(BASE.resolve("/repos/").resolve(repoUrl).resolve(relativeUri))
            .header("X-GitHub-Api-Version", API_VERSION);
    }

    private static void ensureStatusCode(HttpResponse<?> response) throws GitHubException {
        if (response.statusCode() >= 400) {
            throw new GitHubException(response.statusCode());
        }
    }

    public record Release(String tagName, int id, String htmlUrl) {
        private static final JsonDeserializer<Release> DESERIALIZER = (json, typeOfT, context) -> {
            var object = json.getAsJsonObject();

            return new Release(
                object.get("tag_name").getAsString(),
                object.get("id").getAsInt(),
                object.get("html_url").getAsString()
            );
        };
    }

    public record ReleaseAsset(String name, int id) {
        public static final JsonDeserializer<ReleaseAsset> DESERIALIZER = (json, typeOfT, context) -> {
            var object = json.getAsJsonObject();

            return new ReleaseAsset(
                object.get("name").getAsString(),
                object.get("id").getAsInt()
            );
        };
    }

    public static class GitHubException extends Exception {
        private final int statusCode;

        GitHubException(int statusCode) {
            this.statusCode = statusCode;
        }

        @Override
        public String toString() {
            return "GitHub returned status code %d.".formatted(statusCode);
        }
    }
}
