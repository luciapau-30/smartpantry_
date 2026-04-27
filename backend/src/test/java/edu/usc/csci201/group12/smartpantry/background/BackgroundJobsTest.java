package edu.usc.csci201.group12.smartpantry.background;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BackgroundJobsTest {

    @Test
    void scheduledRunnableExecutesRepeatedly() throws InterruptedException {
        BackgroundJobs jobs = new BackgroundJobs(2);
        try {
            CountDownLatch latch = new CountDownLatch(3);
            jobs.scheduleAtFixedRate(latch::countDown, 0, 50, TimeUnit.MILLISECONDS);
            assertTrue(latch.await(2, TimeUnit.SECONDS),
                    "Job should have executed at least 3 times within 2s");
        } finally {
            jobs.shutdown();
        }
    }

    @Test
    void exceptionInJobDoesNotKillThePool() throws InterruptedException {
        BackgroundJobs jobs = new BackgroundJobs(1);
        try {
            AtomicInteger ranAfterFailure = new AtomicInteger();
            CountDownLatch first = new CountDownLatch(1);
            CountDownLatch second = new CountDownLatch(1);

            jobs.scheduleAtFixedRate(() -> {
                if (first.getCount() > 0) {
                    first.countDown();
                    throw new RuntimeException("intentional");
                }
                ranAfterFailure.incrementAndGet();
                second.countDown();
            }, 0, 50, TimeUnit.MILLISECONDS);

            assertTrue(first.await(1, TimeUnit.SECONDS), "First tick should have run");
            assertTrue(second.await(2, TimeUnit.SECONDS),
                    "Pool must still execute subsequent ticks after a thrown exception");
            assertTrue(ranAfterFailure.get() >= 1);
        } finally {
            jobs.shutdown();
        }
    }

    @Test
    void shutdownStopsFurtherExecutions() throws InterruptedException {
        BackgroundJobs jobs = new BackgroundJobs(1);
        AtomicInteger counter = new AtomicInteger();
        jobs.scheduleAtFixedRate(counter::incrementAndGet, 0, 20, TimeUnit.MILLISECONDS);

        Thread.sleep(150);
        jobs.shutdown();
        int afterShutdown = counter.get();
        Thread.sleep(150);
        assertEquals(afterShutdown, counter.get(),
                "Counter must not advance once the pool has been shut down");
    }

    @Test
    void poolSizeMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new BackgroundJobs(0));
        assertThrows(IllegalArgumentException.class, () -> new BackgroundJobs(-1));
    }
}
