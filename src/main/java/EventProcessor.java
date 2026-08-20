import javax.xml.transform.TransformerFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingQueue;

public class EventProcessor implements Runnable{
    private LinkedBlockingQueue<TradeEvent> queue;
    private PipelineMetricsPerMinute metrics;
    private Deque<TradeEvent> window;

    public EventProcessor(LinkedBlockingQueue<TradeEvent> queue, PipelineMetricsPerMinute metrics) {
        this.queue = queue;
        this.metrics = metrics;
        this.window = new ArrayDeque<>();
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
                System.out.println("EventProcessor interrupted");
                break;
            }
        }
    }

    private void windowCalc() throws InterruptedException {
        TradeEvent te = queue.take(); // waits if queue is empty (MAY BLOCK OTHER THREADS)
        window.offer(te);
        metrics.incProcessedEvents();
        metrics.incTradeCount();
        metrics.addPrice(te.getPrice().floatValue());
        metrics.addVolume(te.getQuantity().floatValue());
        if (metrics.getLastPrice() != 0) {
            float last = metrics.getLastPrice();
            float cur = te.getPrice().floatValue();
            float ret = (cur - last) / last;
            metrics.addExpVal(ret);
            // ret = (price(i) - price(i-1))/price(i-1)
        }
        metrics.setLastPrice(te.getPrice().floatValue());


        //what if metrics called here, when the window has new elements but isn't cleaned from the old ones?

        Duration timeDiff = Duration.between(window.peekFirst().getTimeStamp(), window.peekLast().getTimeStamp());
        // based on [lower bound; upper bound), difference between the latest and newest trade in the window
        //window is a +Deque+ works as FIFO, i.e. peek == peekFirst == oldest element, peekLast == lastly added element
        while (timeDiff.toSeconds() > 60) {
            TradeEvent old = window.pollFirst();
            metrics.subPrice(old.getPrice().floatValue());
            metrics.decTradeCount();
            metrics.subVolume(old.getQuantity().floatValue());
            TradeEvent cur = window.peekFirst();
            if (cur != null) {
                timeDiff = Duration.between(cur.getTimeStamp(), window.peekLast().getTimeStamp());
                float crn = cur.getPrice().floatValue();
                float last = old.getPrice().floatValue();
                float ret = (crn - last) / last;
                metrics.subExpVal(ret);
            }
            else break;
        }
    }
}
