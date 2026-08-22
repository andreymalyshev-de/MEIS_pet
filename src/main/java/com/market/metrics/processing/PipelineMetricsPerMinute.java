package com.market.metrics.processing;

import java.math.BigDecimal;

public class PipelineMetricsPerMinute extends PipelineMetrics{
    private long tradeCount, tradesWithNoSpike;
    private double totalPrice, totalVolume, totalReturn, totalReturnSq, lastPrice;
    private double totalPriceSq;
    // bigDec for prices

    public PipelineMetricsPerMinute() {
        super();
        lastPrice = totalPrice = totalVolume = totalReturn = totalReturnSq = 0;
        tradeCount = 0;
        //window = new double();
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public double getVolatilityOfReturns() {
        if (tradeCount < 2) return 0;
        double expsq1 = getTotalReturn()/(getTradeCount() - 1);
        double expsq2 = getTotalReturnSq()/(getTradeCount() - 1);
        double dev = (double)Math.sqrt(expsq2 - (expsq1*expsq1));
        return dev;
    }

    public long getTradeCount() {
        return tradeCount;
    }

    public double getTotalVolume() {
        return totalVolume;
    }

    public double getTotalReturn() {
        return totalReturn;
    }

    public double getTotalReturnSq() {
        return totalReturnSq;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }

    public void incTradeCount() {
        this.tradeCount++;
    }

    public void incTradesWithNoSpike() {
        this.tradesWithNoSpike++;
    }

    public long getTradesWithNoSpike() {
        return tradesWithNoSpike;
    }
    public void resetTWNS() {
        this.tradesWithNoSpike = 0;
    }

    public void decTradeCount() {
        this.tradeCount--;
    }

    public void addPrice(double price) {
        this.totalPrice+=(price);
        this.totalPriceSq+=(price*price);
    }

    public void subPrice(double price) {
        this.totalPrice+=(-price);
        this.totalPriceSq-=(price*price);
    }

    public double getPriceDeviation() {
        if (tradeCount == 0) {
            return 0;
        }
        double mean = totalPrice / tradeCount;
        double variance = totalPriceSq / tradeCount - mean * mean;

        return Math.sqrt(variance);
    }

    public void addRetExpVal(double ret) {
        this.totalReturn+=(ret);
        this.totalReturnSq+=(ret*ret);
    }

    public void subRetExpVal(double ret) {
        this.totalReturn+=(-ret);
        this.totalReturnSq+=(-(ret*ret));
    }

    public void addVolume(double volume) {
        this.totalVolume+=(volume);
    }

    public void subVolume(double volume) {
        this.totalVolume+=(-volume);
    }

    public double getAvgPrice() {
        return tradeCount == 0 ? 0 :  totalPrice / tradeCount;
    }

    @Override
    public String toString() {
        String s = new BigDecimal(Double.toString(getVolatilityOfReturns() * 100)).toPlainString();
        //"\nWINDOW SIZE: " + getWindow() +
        return "60s metrics: " + "\navgPrice: " + getAvgPrice() +
                "\ntradeCount: " + tradeCount + "\nvolume: " + totalVolume + "\nreturn volatility: " + s + " %\n\n"; //+ super.toString();
    }
}
