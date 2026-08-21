import java.util.concurrent.LinkedBlockingQueue;

public class Logger implements Runnable{
    private LinkedBlockingQueue<TradeEvent> queue;
    private PipelineMetricsPerMinute metrics;

    public Logger(LinkedBlockingQueue<TradeEvent> queue, PipelineMetricsPerMinute metrics) {
        this.queue = queue;
        this.metrics = metrics;
    }

    @Override
    public void run() {
        try {
            long startTime = System.nanoTime();
            Thread.sleep(5000);
            while (true) {
                synchronized (metrics) {
                    writeLog(startTime);
                }
                Thread.sleep(30000);
            }
        }
        catch (InterruptedException e) {
            System.out.println("Logger interrupted");
        }
    }

    private void writeLog(long startTime) {
        long curTime = System.nanoTime();
        long difference = (curTime - startTime) / 1_000_000_000; // 1sec = 1*10^9 nsec
        float event_delta = metrics.getProcessedEvents().get() / (float)difference;
        System.out.println("processed trades/sec: " + event_delta);
        System.out.println("queue size: " + queue.size());
        System.out.println(metrics.toString());
        DatabaseClient.insertSnapshot(metrics);
    }
}
