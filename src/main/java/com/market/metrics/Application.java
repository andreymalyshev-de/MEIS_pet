package com.market.metrics;

import com.market.metrics.api.LiveEventPublisher;
import com.market.metrics.ingestion.BinanceWebSocketClient;
import com.market.metrics.model.TradeEvent;
import com.market.metrics.persistance.DatabaseClient;
import com.market.metrics.processing.EventProcessor;
import com.market.metrics.processing.Logger;
import com.market.metrics.processing.PipelineMetricsPerMinute;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.LinkedBlockingQueue;
@SpringBootApplication

public class Application implements CommandLineRunner {
    private BinanceWebSocketClient client;
    private EventProcessor processor;
    private Logger logger;

    public Application(DatabaseClient databaseClient, LiveEventPublisher liveEventPublisher) { // dependency injection pattern -> allows to adjust queue strategies by passing the shared object from here
        try {
            URI uri = new URI("wss://stream.binance.com:9443/ws/btcusdt@trade");
            LinkedBlockingQueue<TradeEvent> queue = new LinkedBlockingQueue<>(10000);
            // LinkedBlockingQueue is thread-safe
            PipelineMetricsPerMinute metrics = new PipelineMetricsPerMinute();
            this.logger = new Logger(queue, metrics, databaseClient, liveEventPublisher);
            this.client = new BinanceWebSocketClient(uri, queue, metrics);
            this.processor = new EventProcessor(queue, metrics, databaseClient, liveEventPublisher);
        } catch (URISyntaxException e) {
            System.out.println("unsupported URL");
        }
    }

    public void start() {
        // processor.run(); -> starts the main thread, since it doesn't stop it never reaches connect()
        new Thread(processor).start(); // starts another thread, not the main one
        client.connect();
        new Thread(logger).start();
        // 1. start the consumer, 2. start the producer
    }

    // this method tells SpringBoot to start the start() method
    @Override
    public void run(String... args) throws Exception {
        start();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
