package com.market.metrics.processing;

import com.market.metrics.model.AnomalyEvent;
import com.market.metrics.model.TradeEvent;

public class AnomalyDetector {

    // k - sigma for adjusting the frequency of AnomalyEvents
    public AnomalyEvent detectAnomaly(TradeEvent te, PipelineMetricsPerMinute metrics, int k) {
        double curPrice = te.getPrice().doubleValue();
        double avgPrice = metrics.getAvgPrice();
        double change = curPrice - avgPrice;
        double changePercent = avgPrice == 0 ? 0 : curPrice*100/avgPrice - 100;
        if (avgPrice != 0 && Math.abs(change) > metrics.getPriceDeviation()*k) {
            //System.out.println(metrics.getPriceDeviation() + " " + change);
            if (change > 0) {
                //System.out.println(curPrice + " " + avgPrice);
                return new AnomalyEvent(AnomalyEvent.AnomalyType.PRICE_SPIKE, changePercent, te.getSymbol(), te.getTimeStamp());
            }
            else return new AnomalyEvent(AnomalyEvent.AnomalyType.PRICE_FALL, changePercent, te.getSymbol(), te.getTimeStamp());
        }

        return null;
    }
 }
