package com.market.metrics.processing;

import java.util.concurrent.atomic.AtomicLong;

public class PipelineMetrics {
    // AtomicLong - thread safe
    private AtomicLong receivedEvents, enqueuedEvents, processedEvents, droppedEvents, malformedEvents;

    public PipelineMetrics() {
        receivedEvents = new AtomicLong();
        enqueuedEvents = new AtomicLong();
        processedEvents = new AtomicLong();
        droppedEvents = new AtomicLong();
        malformedEvents = new AtomicLong();
    }

    public AtomicLong getDroppedEvents() {
        return droppedEvents;
    }

    public AtomicLong getEnqueuedEvents() {
        return enqueuedEvents;
    }

    public AtomicLong getMalformedEvents() {
        return malformedEvents;
    }

    public AtomicLong getProcessedEvents() {
        return processedEvents;
    }

    public AtomicLong getReceivedEvents() {
        return receivedEvents;
    }

    public void incDroppedEvents() {
        this.droppedEvents.incrementAndGet();
    }

    public void incEnqueuedEvents() {
        this.enqueuedEvents.incrementAndGet();
    }

    public void incMalformedEvents() {
        this.malformedEvents.incrementAndGet();
    }

    public void incProcessedEvents() {
        this.processedEvents.incrementAndGet();
    }

    public void incReceivedEvents() {
        this.receivedEvents.incrementAndGet();
    }

    @Override
    public String toString() {
        return "total metrics:\nreceivedEvents: " + getReceivedEvents() + "\nenqueuedEvents: " + getEnqueuedEvents() + "\ndroppedEvents: " + getDroppedEvents() + "\nprocessedEvents: " + getProcessedEvents() + "\nmalformedEvents: " + getMalformedEvents() + "\n--------";
    }
}
