package com.market.metrics.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/live")
public class LiveController {
    private LiveEventPublisher liveEventPublisher;

    public LiveController(LiveEventPublisher liveEventPublisher) {
        this.liveEventPublisher = liveEventPublisher;
    }

    @GetMapping
    public SseEmitter subscribe() {
        return liveEventPublisher.subscribe();
    }
}
