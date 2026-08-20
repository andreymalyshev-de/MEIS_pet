import java.math.BigDecimal;

public class PipelineMetricsPerMinute extends PipelineMetrics{
    private long tradeCount;
    private float totalPrice, totalVolume, totalExpValue, totalExpValueSq, lastPrice;
    // bigDec for prices

    public PipelineMetricsPerMinute() {
        super();
        lastPrice = totalPrice = totalVolume = totalExpValue = totalExpValueSq = 0;
        tradeCount = 0;
        //window = new float();
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public float getLastPrice() {
        return lastPrice;
    }

    public float getVolatilityOfReturns() {
        if (tradeCount < 2) return 0;
        float expsq1 = getTotalExpValue()/(getTradeCount() - 1);
        float expsq2 = getTotalExpValueSq()/(getTradeCount() - 1);
        float dev = (float)Math.sqrt(expsq2 - (expsq1*expsq1));
        return dev;
    }

    public long getTradeCount() {
        return tradeCount;
    }

    public float getTotalVolume() {
        return totalVolume;
    }

    public float getTotalExpValue() {
        return totalExpValue;
    }

    public float getTotalExpValueSq() {
        return totalExpValueSq;
    }

    public void setLastPrice(float lastPrice) {
        this.lastPrice = lastPrice;
    }

    public void incTradeCount() {
        this.tradeCount++;
    }

    public void decTradeCount() {
        this.tradeCount--;
    }

    public void addPrice(float price) {
        this.totalPrice+=(price);
    }

    public void subPrice(float price) {
        this.totalPrice+=(-price);
    }

    public void addExpVal(float ret) {
        this.totalExpValue+=(ret);
        this.totalExpValueSq+=(ret*ret);
    }

    public void subExpVal(float ret) {
        this.totalExpValue+=(-ret);
        this.totalExpValueSq+=(-(ret*ret));
    }

    public void addVolume(float volume) {
        this.totalVolume+=(volume);
    }

    public void subVolume(float volume) {
        this.totalVolume+=(-volume);
    }

    @Override
    public String toString() {
        String s = new BigDecimal(Float.toString(getVolatilityOfReturns() * 100)).toPlainString();
        //"\nWINDOW SIZE: " + getWindow() +
        return "60s metrics: " + "\navgPrice: " + (tradeCount == 0 ? 0 :  totalPrice / tradeCount) +
                "\ntradeCount: " + tradeCount + "\nvolume: " + totalVolume + "\nreturn volatility: " + s + " %"; //+ super.toString();
    }
}
