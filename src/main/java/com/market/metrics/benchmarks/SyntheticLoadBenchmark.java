package com.market.metrics.benchmarks;

import com.market.metrics.api.LiveEventPublisher;
import com.market.metrics.model.TradeEvent;
import com.market.metrics.persistence.DatabaseClient;
import com.market.metrics.processing.EventProcessor;
import com.market.metrics.processing.Logger;
import com.market.metrics.processing.PipelineMetricsPerMinute;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.io.OutputStream;
import java.io.PrintStream;

public class SyntheticLoadBenchmark {

    private static final int QUEUE_CAPACITY = 10_000;

    // 40sec - long enough for Logger to execute at ~5 s and ~35 s.
    private static final int TEST_SECONDS = 40;

    private static final int[] RATES = {
            100_000,
            250_000,
            500_000
    };

    public static void main(String[] args) throws Exception {

        PrintStream benchmarkOut = System.out;

        System.setOut(
                new PrintStream(OutputStream.nullOutputStream())
        );

        DatabaseClient database =
                createDatabaseClient();

        LiveEventPublisher publisher =
                new LiveEventPublisher();

        benchmarkOut.printf(
                "%-12s %-14s %-12s %-15s %-12s %-12s %-10s%n",
                "target/s",
                "processed/s",
                "drop %",
                "max queue %",
                "end queue",
                "processed",
                "seconds"
        );

        for (int rate : RATES) {
            run(rate, database, publisher, benchmarkOut);

            // Let CPU / GC settle slightly between runs.
            Thread.sleep(2000);
        }
    }

    private static void run(
            int targetRate,
            DatabaseClient database,
            LiveEventPublisher publisher,
            PrintStream out
    ) throws Exception {

        LinkedBlockingQueue<TradeEvent> queue =
                new LinkedBlockingQueue<>(QUEUE_CAPACITY);

        PipelineMetricsPerMinute metrics =
                new PipelineMetricsPerMinute();

        EventProcessor processor =
                new EventProcessor(
                        queue,
                        metrics,
                        database,
                        publisher
                );

        Logger logger =
                new Logger(
                        queue,
                        metrics,
                        database,
                        publisher
                );

        Thread consumerThread =
                new Thread(processor, "benchmark-consumer");

        Thread loggerThread =
                new Thread(logger, "benchmark-logger");

        consumerThread.setDaemon(true);
        loggerThread.setDaemon(true);

        consumerThread.start();
        loggerThread.start();


        AtomicLong attempted =
                new AtomicLong();

        AtomicLong dropped =
                new AtomicLong();

        AtomicInteger maxQueue =
                new AtomicInteger();


        long startProcessed =
                metrics.getProcessedEvents().get();

        long start =
                System.nanoTime();

        long durationNanos =
                TEST_SECONDS * 1_000_000_000L;


        Thread producerThread = new Thread(() -> {

            while (true) {

                long elapsed =
                        System.nanoTime() - start;

                if (elapsed >= durationNanos) {
                    break;
                }

                /*
                 * Number of events that should have been produced
                 * by this point in time.
                 */
                long expected =
                        (elapsed * targetRate)
                                / 1_000_000_000L;


                while (attempted.get() < expected) {

                    TradeEvent event =
                            new TradeEvent(
                                    "BTCUSDT",
                                    new BigDecimal("60000.00"),
                                    new BigDecimal("0.001"),
                                    Instant.now()
                            );

                    attempted.incrementAndGet();
                    metrics.incReceivedEvents();

                    if (queue.offer(event)) {
                        metrics.incEnqueuedEvents();
                    } else {
                        metrics.incDroppedEvents();
                        dropped.incrementAndGet();
                    }

                    maxQueue.accumulateAndGet(
                            queue.size(),
                            Math::max
                    );
                }

                /*
                 * Avoid burning an entire CPU core while waiting
                 * for the next production interval.
                 */
                LockSupport.parkNanos(50_000);
            }

        }, "benchmark-producer");


        producerThread.start();
        producerThread.join();


        long end =
                System.nanoTime();

        /*
         * Capture results immediately.
         * Do NOT wait for the queue to drain because we're measuring
         * sustainable real-time throughput.
         */
        long processed =
                metrics.getProcessedEvents().get()
                        - startProcessed;

        int endQueue =
                queue.size();

        double seconds =
                (end - start) / 1_000_000_000.0;

        double processedPerSecond =
                processed / seconds;

        double dropPercent =
                attempted.get() == 0
                        ? 0
                        : dropped.get()
                        * 100.0
                        / attempted.get();

        double maxQueuePercent =
                maxQueue.get()
                        * 100.0
                        / QUEUE_CAPACITY;


        out.printf(
                "%-12d %-14.0f %-12.3f %-15.2f %-12d %-12d %.1fs%n",
                targetRate,
                processedPerSecond,
                dropPercent,
                maxQueuePercent,
                endQueue,
                processed,
                seconds
        );


        consumerThread.interrupt();
        loggerThread.interrupt();

        consumerThread.join(1000);
        loggerThread.join(1000);
    }


    private static DatabaseClient createDatabaseClient() {

        String url = "jdbc:postgresql://localhost:5432/metrics_analytics";

        String user = "postgres";

        String password = System.getenv("DB_PASSWORD");

        if (url == null || user == null || password == null) {
            throw new IllegalStateException(
                    "Set DB_URL, DB_USER and DB_PASSWORD"
            );
        }

        DriverManagerDataSource dataSource =
                new DriverManagerDataSource();

        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);

        JdbcTemplate jdbcTemplate =
                new JdbcTemplate(dataSource);

        return new DatabaseClient(jdbcTemplate);
    }
}