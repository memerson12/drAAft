package draaft;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackgroundTaskRunner {
    private static final ExecutorService executor = Executors.newFixedThreadPool(1);

    public static void runAsync(Runnable task) {
        executor.submit(task);
    }

    public static void shutdown() {
        executor.shutdown();
    }
}
