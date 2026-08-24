package com.market.metrics.ingestion;

import com.market.metrics.model.TradeEvent;
import com.market.metrics.processing.PipelineMetrics;
import org.json.JSONException;
import org.json.JSONObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.time.Instant;
import java.util.concurrent.LinkedBlockingQueue;

public class BinanceWebSocketClient extends WebSocketClient {

    private LinkedBlockingQueue<TradeEvent> queue;
    private PipelineMetrics metrics;

    public BinanceWebSocketClient(URI serverUri, LinkedBlockingQueue<TradeEvent> queue, PipelineMetrics metrics) {
        super(serverUri);
        this.queue = queue;
        this.metrics = metrics;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("connected");
    }

    @Override
    public void onMessage(String s) {
        try {
            JSONObject jo = new JSONObject(s);
            TradeEvent tradeEvent = new TradeEvent(jo.getString("s"), jo.getBigDecimal("p"), jo.getBigDecimal("q"), Instant.ofEpochMilli(jo.getLong("T")));
            boolean took = queue.offer(tradeEvent); // offer doesn't block the stream and just passes the events if the queue is full

            // i.e. OutOfMemory prevention with event dropping
            if (took) metrics.incEnqueuedEvents();
            else metrics.incDroppedEvents();
        }
        catch (JSONException e) {
            metrics.incMalformedEvents();
        }
        finally {
            metrics.incReceivedEvents();
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("disconnected");
    }

    @Override
    public void onError(Exception e) {
        System.out.println("error");
        System.out.println(e.getMessage());
    }

}
