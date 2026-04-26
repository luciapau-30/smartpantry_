// T2: Wraps the application-managed thread pool used for periodic backend work
// (expiry checks today; recipe re-scoring etc. tomorrow). Tomcat's request thread
// pool handles HTTP connections — this pool is for jobs the application schedules
// itself.
//
// Lifecycle:
//   - SmartPantryBootstrapListener.contextInitialized() → new BackgroundJobs(...)
//                                                       → schedule jobs
//   - SmartPantryBootstrapListener.contextDestroyed()   → shutdown()
package edu.usc.csci201.group12.smartpantry.background;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BackgroundJobs {

    private static final Logger LOG = Logger.getLogger(BackgroundJobs.class.getName());

    private final ScheduledExecutorService scheduler;
    private final int poolSize;

    public BackgroundJobs(int poolSize) {
        if (poolSize < 1) throw new IllegalArgumentException("poolSize must be >= 1");
        this.poolSize = poolSize;
        this.scheduler = Executors.newScheduledThreadPool(poolSize, new NamedThreadFactory());
        LOG.info(() -> "BackgroundJobs pool started (size=" + poolSize + ")");
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable job, long initialDelay, long period, TimeUnit unit) {
        Runnable safeJob = () -> {
            try {
                job.run();
            } catch (Throwable t) {
                LOG.log(Level.WARNING, "Background job threw — pool will keep running", t);
            }
        };
        return scheduler.scheduleAtFixedRate(safeJob, initialDelay, period, unit);
    }

    public void shutdown() {
        LOG.info("BackgroundJobs pool shutting down...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException ex) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        LOG.info("BackgroundJobs pool stopped");
    }

    public int getPoolSize() { return poolSize; }

    private static final class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "smartpantry-bg-" + counter.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }
}
