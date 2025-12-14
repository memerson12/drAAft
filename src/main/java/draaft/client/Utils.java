package draaft.client;

import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
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

    public static <T> HttpResponse.BodyHandler<T> jsonBody(Class<T> t) {
        return new JsonBodyHandler<>(t);
    }

    private static class JsonBodyHandler<T> implements HttpResponse.BodyHandler<T> {
        private final Class<T> t;
        private final HttpResponse.BodyHandler<String> inner = HttpResponse.BodyHandlers.ofString();

        public JsonBodyHandler(Class<T> t) {
            this.t = t;
        }

        @Override
        public HttpResponse.BodySubscriber<T> apply(HttpResponse.ResponseInfo responseInfo) {
            return new JsonBodySubscriber<>(inner.apply(responseInfo), t);
        }

        private record JsonBodySubscriber<T>(
            HttpResponse.BodySubscriber<String> inner,
            Class<T> t
        ) implements HttpResponse.BodySubscriber<T> {
            @Override
            public CompletionStage<T> getBody() {
                return this.inner.getBody().thenApply(body -> ServerClient.GSON.fromJson(body, t));
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
