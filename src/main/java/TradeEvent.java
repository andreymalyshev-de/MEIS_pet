import java.math.BigDecimal;
import java.time.Instant;

// no setters - immutability keeps it simpler

public class TradeEvent {
    private String symbol;
    private BigDecimal price; // more precise than float
    private BigDecimal quantity;
    private Instant timeStamp; // is used for machine timestamps

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
