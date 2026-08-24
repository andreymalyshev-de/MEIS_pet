package com.market.metrics.model;

import java.math.BigDecimal;
import java.time.Instant;

public class AnomalyEvent {
    public enum AnomalyType {
        PRICE_SPIKE,
        PRICE_FALL
    }

    private AnomalyType type;
    private double change;
    private String symbol;
    private Instant timeStamp;

    public AnomalyEvent(AnomalyType type, double change, String symbol, Instant timeStamp) {
        this.type = type;
        this.change = change;
        this.symbol = symbol;
        this.timeStamp = timeStamp;
    }

    public Instant getTimeStamp() {
        return timeStamp;
    }

    public AnomalyType getType() {
        return type;
    }

    public double getChange() {
        return change;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return "\n" + type + "\n" + new BigDecimal(Double.toString(change)).stripTrailingZeros().toPlainString() + "%" + "\n"
                + symbol + "\n" + timeStamp.toString() + "\n";
    }
}
