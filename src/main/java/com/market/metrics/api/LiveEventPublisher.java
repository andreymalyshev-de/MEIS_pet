package com.market.metrics.api;

import com.market.metrics.model.AnomalyEvent;
import com.market.metrics.model.MetricSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class LiveEventPublisher {

    // CopyOnWriteArrayList is thread safe for HTTP Connections
    private List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter sseEmitter = new SseEmitter(0L);
        emitters.add(sseEmitter);
        sseEmitter.onCompletion(() -> emitters.remove(sseEmitter));
        sseEmitter.onTimeout(() -> emitters.remove(sseEmitter));
        sseEmitter.onError(e -> emitters.remove(sseEmitter));
        return sseEmitter;
    }

    public void publishMetric(MetricSnapshot metric) {
        publish("metric", metric);
    }

    public void publishAnomaly(AnomalyEvent anomalyEvent) {
        publish("anomaly", anomalyEvent);
    }

    public void publish(String eventType, Object data) {
        for (SseEmitter emitter: emitters) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name(eventType)
                                .data(data)
                );
            }
            catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

}
