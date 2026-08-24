package com.market.metrics.processing;

import com.market.metrics.api.LiveEventPublisher;
import com.market.metrics.model.AnomalyEvent;
import com.market.metrics.model.TradeEvent;
import com.market.metrics.persistence.DatabaseClient;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingQueue;

public class EventProcessor implements Runnable{
    private LinkedBlockingQueue<TradeEvent> queue;
    private PipelineMetricsPerMinute metrics;
    private Deque<TradeEvent> window;
    private AnomalyDetector anomalyDetector;
    private DatabaseClient databaseClient;
    private LiveEventPublisher liveEventPublisher;

    public EventProcessor(LinkedBlockingQueue<TradeEvent> queue, PipelineMetricsPerMinute metrics, DatabaseClient databaseClient, LiveEventPublisher liveEventPublisher) {
        this.queue = queue;
        this.metrics = metrics;
        this.window = new ArrayDeque<>();
        this.anomalyDetector = new AnomalyDetector();
        this.databaseClient = databaseClient;
        this.liveEventPublisher = liveEventPublisher;
    }

    @Override
    public void run() {
        while (true) {
            try {
                TradeEvent te = queue.take(); // waits if queue is empty (MAY BLOCK OTHER THREADS)
                AnomalyEvent event = null;
                synchronized (metrics) {
                    event = windowCalc(te);
                }
                if (event != null) databaseClient.insertAnomaly(event);
            } catch (InterruptedException e) {
                System.out.println("EventProcessor interrupted");
                break;
            }
        }
    }

    private AnomalyEvent windowCalc(TradeEvent te) throws InterruptedException {
        //Duration timeDiff = Duration.between(window.peekFirst().getTimeStamp(), window.peekLast().getTimeStamp());
        // based on [lower bound; upper bound), difference between the latest and newest trade in the window
        // window is a +Deque+ works as FIFO, i.e. peek == peekFirst == oldest element, peekLast == lastly added element
        while (!window.isEmpty() && te.getTimeStamp().minusSeconds(60).isAfter(window.peekFirst().getTimeStamp())) {
            TradeEvent old = window.pollFirst();
            metrics.subPrice(old.getPrice().doubleValue());
            metrics.decTradeCount();
            metrics.subVolume(old.getQuantity().doubleValue());
            TradeEvent cur = window.peekFirst();
            if (cur != null) {
                double crn = cur.getPrice().doubleValue();
                double last = old.getPrice().doubleValue();
                double ret = (crn - last) / last;
                metrics.subRetExpVal(ret);
            }
            else break;
        }

        //Anomaly Handling
        AnomalyEvent event = anomalyDetector.detectAnomaly(te, metrics, 5);
        if (event != null && metrics.getTradesWithNoSpike() > 10) {
            System.out.println(event);
            databaseClient.insertAnomaly(event);
            liveEventPublisher.publishAnomaly(event);
            metrics.resetTWNS();
        }

        window.offer(te);
        metrics.incProcessedEvents();
        metrics.incTradeCount();
        metrics.incTradesWithNoSpike();
        metrics.addPrice(te.getPrice().doubleValue());
        metrics.addVolume(te.getQuantity().doubleValue());
        if (metrics.getLastPrice() != 0) {
            double last = metrics.getLastPrice();
            double cur = te.getPrice().doubleValue();
            double ret = (cur - last) / last;
            metrics.addRetExpVal(ret);
            // ret = (price(i) - price(i-1))/price(i-1)
        }
        metrics.setLastPrice(te.getPrice().doubleValue());

        return event;
    }
}
