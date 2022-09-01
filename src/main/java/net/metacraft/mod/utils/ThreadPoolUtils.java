package net.metacraft.mod.utils;

import java.util.concurrent.*;

public enum ThreadPoolUtils {
    /**
     * INSTANCE
     */
    INSTANCE;

    private static final int CORE_SIZE = 5;

    private static final int MAX_POOL_SIZE = 10;

    private static final int KEEP_ALIVE_TIME = 60;

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(CORE_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100), new ThreadPoolExecutor.DiscardOldestPolicy());

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);

    private ExecutorService singleExecutor = Executors.newSingleThreadExecutor();

    private Future<?> singleFuture;

    public void clearQueue() {
        executor.getQueue().clear();
    }

    public Future<?> execute(Runnable runnable) {
        return executor.submit(runnable);
    }

    public void singleExecute(Runnable runnable) {
        if (singleFuture != null) {
            singleFuture.cancel(true);
        }
        singleFuture = singleExecutor.submit(runnable);
    }

    public ScheduledFuture<?> scheduleAtFixedRateExecute(Runnable runnable, int initialDelay, int period, TimeUnit unit) {
        return scheduledThreadPoolExecutor.scheduleAtFixedRate(runnable, initialDelay, period, unit);
    }
}
