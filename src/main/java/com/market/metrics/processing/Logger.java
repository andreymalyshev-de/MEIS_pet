package com.market.metrics.processing;

import com.market.metrics.api.LiveEventPublisher;
import com.market.metrics.model.MetricSnapshot;
import com.market.metrics.model.TradeEvent;
import com.market.metrics.persistence.DatabaseClient;

import java.time.Instant;
import java.util.concurrent.LinkedBlockingQueue;

public class Logger implements Runnable{
    private LinkedBlockingQueue<TradeEvent> queue;
    private PipelineMetricsPerMinute metrics;
    private DatabaseClient databaseClient;
    private LiveEventPublisher liveEventPublisher;

    public Logger(LinkedBlockingQueue<TradeEvent> queue, PipelineMetricsPerMinute metrics, DatabaseClient databaseClient, LiveEventPublisher liveEventPublisher) {
        this.queue = queue;
        this.metrics = metrics;
        this.databaseClient = databaseClient;
        this.liveEventPublisher = liveEventPublisher;
    }

    @Override
    public void run() {
        try {
            long startTime = System.nanoTime();
            Thread.sleep(5000);
            while (true) {
                MetricSnapshot metricSnapshot = null;
                synchronized (metrics) {
                    metricSnapshot =  writeLog(startTime);
                }
                // inserting a snapshot into the DB can take time that would affect the overall speed of code
                databaseClient.insertSnapshot(metricSnapshot);
                liveEventPublisher.publishMetric(metricSnapshot);
                Thread.sleep(30000);
            }
        }
        catch (InterruptedException e) {
            System.out.println("Logger interrupted");
        }
    }

    private MetricSnapshot writeLog(long startTime) {
        long curTime = System.nanoTime();
        long difference = (curTime - startTime) / 1_000_000_000; // 1sec = 1*10^9 nsec
        float event_delta = metrics.getProcessedEvents().get() / (float)difference;
        System.out.println("processed trades/sec: " + event_delta);
        System.out.println("queue size: " + queue.size());
        System.out.println(metrics.toString());
        MetricSnapshot metricSnapshot = new MetricSnapshot(Instant.now(), "BTC", metrics.getAvgPrice(), metrics.getTotalVolume(), metrics.getVolatilityOfReturns(), metrics.getTradeCount());
        return metricSnapshot;
    }
}
