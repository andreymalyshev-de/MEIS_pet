import java.math.BigDecimal;
import java.time.Instant;

public class AnomalyEvent {
    public enum AnomalyType {
        PRICE_SPIKE,
        PRICE_FALL
    }

    private AnomalyType type;
    private double change;
    private String stock;
    private Instant timeStamp;

    public AnomalyEvent(AnomalyType type, double change, String stock, Instant timeStamp) {
        this.type = type;
        this.change = change;
        this.stock = stock;
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "\n" + type + "\n" + new BigDecimal(Double.toString(change)).stripTrailingZeros().toPlainString() + "%" + "\n"
                + stock + "\n" + timeStamp.toString() + "\n";
    }
}
