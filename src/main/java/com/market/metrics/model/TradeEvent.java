package com.market.metrics.model;

import java.math.BigDecimal;
import java.time.Instant;


public class TradeEvent {
    private final String symbol;
    private final BigDecimal price; // more precise than float
    private final BigDecimal quantity;
    private final Instant timeStamp; // is used for machine timestamps

    public TradeEvent(String symbol, BigDecimal price, BigDecimal quantity, Instant timeStamp) {
        this.price = price;
        this.symbol = symbol;
        this.quantity = quantity;
        this.timeStamp = timeStamp;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public Instant getTimeStamp() {
        return timeStamp;
    }

    public String getSymbol() {
        return symbol;
    }
}
