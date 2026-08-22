package com.market.metrics.processing;

import com.market.metrics.api.LiveEventPublisher;
import com.market.metrics.model.AnomalyEvent;
import com.market.metrics.model.TradeEvent;
import com.market.metrics.persistance.DatabaseClient;

import java.time.Duration;
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
                //System.out.println(queue.size() + " queue size");
                //Thread.sleep(1000);
                synchronized (metrics) {
                    windowCalc();
                }
                // if the queue is empty, it waits. if interrupted -> exception
                //System.out.println("Symbol: " + te.getSymbol());
                //System.out.println("Price: " + te.getPrice());
                //System.out.println("Quantity: " + te.getQuantity());
                //System.out.println("Timestamp: " + te.getTimeStamp().toString());
            } catch (InterruptedException e) {
                System.out.println("com.market.metrics.processing.EventProcessor interrupted");
                break;
            }
        }
    }

    private void windowCalc() throws InterruptedException {
        TradeEvent te = queue.take(); // waits if queue is empty (MAY BLOCK OTHER THREADS)

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


        //what if metrics called here, when the window has new elements but isn't cleaned from the old ones?

        Duration timeDiff = Duration.between(window.peekFirst().getTimeStamp(), window.peekLast().getTimeStamp());
        // based on [lower bound; upper bound), difference between the latest and newest trade in the window
        //window is a +Deque+ works as FIFO, i.e. peek == peekFirst == oldest element, peekLast == lastly added element
        while (timeDiff.toSeconds() > 60) {
            TradeEvent old = window.pollFirst();
            metrics.subPrice(old.getPrice().doubleValue());
            metrics.decTradeCount();
            metrics.subVolume(old.getQuantity().doubleValue());
            TradeEvent cur = window.peekFirst();
            if (cur != null) {
                timeDiff = Duration.between(cur.getTimeStamp(), window.peekLast().getTimeStamp());
                double crn = cur.getPrice().doubleValue();
                double last = old.getPrice().doubleValue();
                double ret = (crn - last) / last;
                metrics.subRetExpVal(ret);
            }
            else break;
        }
    }
}
