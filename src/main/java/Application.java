import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.LinkedBlockingQueue;

public class Application {
    private BinanceWebSocketClient client;
    private EventProcessor processor;
    private Logger logger;

    public Application() { // dependency injection pattern -> allows to adjust queue strategies by passing the shared object from here
        try {
            URI uri = new URI("wss://stream.binance.com:9443/ws/btcusdt@trade");
            LinkedBlockingQueue<TradeEvent> queue = new LinkedBlockingQueue<>(10000);
            // LinkedBlockingQueue is thread-safe
            PipelineMetricsPerMinute metrics = new PipelineMetricsPerMinute();
            this.logger = new Logger(queue, metrics);
            this.client = new BinanceWebSocketClient(uri, queue, metrics);
            this.processor = new EventProcessor(queue, metrics);
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

    public static void main(String[] args) {
        Application app = new Application();
        app.start();
    }
}
