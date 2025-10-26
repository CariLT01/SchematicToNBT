package com.carilt01.schematictonbt;

import java.util.concurrent.*;
import java.util.function.Supplier;

public class SlowTaskWatcher<T> {

    private final long thresholdMillis; // Time after which the "slow callback" triggers
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * @param thresholdMillis time in milliseconds after which the task is considered slow
     */
    public SlowTaskWatcher(long thresholdMillis) {
        this.thresholdMillis = thresholdMillis;
    }

    /**
     * Runs a blocking task and calls a "slow callback" if it takes longer than the threshold.
     *
     * @param task         The blocking task to execute
     * @param slowCallback Called once if the task takes longer than thresholdMillis
     * @return The result of the task
     * @throws Exception if the task throws
     */
    public <E extends Exception> T run(ThrowingSupplier<T, E> task, Runnable slowCallback) throws Exception {
        // Schedule the slow callback to run after a threshold
        ScheduledFuture<?> future = scheduler.schedule(slowCallback, thresholdMillis, TimeUnit.MILLISECONDS);

        try {
            T result = task.get();   // blocking call
            future.cancel(false);    // cancel the slow callback if finished early
            return result;
        } finally {
            // optional cleanup after task
        }
    }

    /**
     * Shutdown internal scheduler when done using this watcher
     */
    public void shutdown() {
        scheduler.shutdownNow();
    }
}