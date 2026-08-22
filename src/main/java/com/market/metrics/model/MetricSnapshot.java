package com.market.metrics.model;

import java.time.Instant;

// DTO - data transfer object
public class MetricSnapshot {
    private Instant timeStamp;
    private String symbol;
    private double avgPrice, volume, volatility;
    private long tradeCount;

    public MetricSnapshot(Instant timeStamp, String symbol, double avgPrice, double volume, double volatility, long tradeCount) {
        this.timeStamp = timeStamp;
        this.symbol = symbol;
        this.volatility = volatility;
        this.volume = volume;
        this.tradeCount = tradeCount;
        this.avgPrice = avgPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public Instant getTimeStamp() {
        return timeStamp;
    }

    public double getAvgPrice() {
        return avgPrice;
    }

    public double getVolatility() {
        return volatility;
    }

    public double getVolume() {
        return volume;
    }

    public long getTradeCount() {
        return tradeCount;
    }
}
