package hotel.simulation;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ProducerPool {
    private final ExecutorService executor;

    public ProducerPool(int threads) {
        this.executor = Executors.newFixedThreadPool(threads);
    }

    public void submit(Runnable producer) {
        executor.execute(producer);
    }

    // Stops new work and lets the accepted producers finish. Listeners run on the producer
    // threads and nothing is queued between them, so a finished producer has delivered all
    // its events. Returns false if producers had to be interrupted after the timeout.
    public boolean shutdown(Duration timeout) {
        executor.shutdown();
        try {
            if (executor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                return true;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        executor.shutdownNow();
        return false;
    }
}
