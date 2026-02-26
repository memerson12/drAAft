package draaft.client;

import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;

import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

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

    public static <T> HttpResponse.BodyHandler<@Nullable T> jsonBody(Class<T> t) {
        return new JsonBodyHandler<>(t, ServerClient.GSON);
    }

    public static <T> HttpResponse.BodyHandler<@Nullable T> jsonBody(Class<T> t, Gson gson) {
        return new JsonBodyHandler<>(t, gson);
    }

    private static class JsonBodyHandler<T> implements HttpResponse.BodyHandler<@Nullable T> {
        private final Class<T> t;
        private final Gson gson;
        private final HttpResponse.BodyHandler<String> inner = HttpResponse.BodyHandlers.ofString();

        public JsonBodyHandler(Class<T> t, Gson gson) {
            this.t = t;
            this.gson = gson;
        }

        @Override
        public HttpResponse.BodySubscriber<T> apply(HttpResponse.ResponseInfo responseInfo) {
            return new JsonBodySubscriber<>(inner.apply(responseInfo), responseInfo, t, gson);
        }

        private record JsonBodySubscriber<T>(
            HttpResponse.BodySubscriber<String> inner,
            HttpResponse.ResponseInfo responseInfo,
            Class<T> t,
            Gson gson
        ) implements HttpResponse.BodySubscriber<@Nullable T> {
            @Override
            public CompletionStage<T> getBody() {
                int statusCode = this.responseInfo.statusCode();

                return (statusCode >= 200 && statusCode < 300)
                    ? this.inner.getBody().thenApply(body -> gson.fromJson(body, t))
                    : CompletableFuture.completedStage(null);
            }

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.inner.onSubscribe(subscription);
            }

            @Override
            public void onNext(List<ByteBuffer> item) {
                this.inner.onNext(item);
            }

            @Override
            public void onError(Throwable throwable) {
                this.inner.onError(throwable);
            }

            @Override
            public void onComplete() {
                this.inner.onComplete();
            }
        }
    }
}
