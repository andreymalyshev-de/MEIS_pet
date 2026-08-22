package com.market.metrics.api;

import com.market.metrics.model.MetricSnapshot;
import com.market.metrics.persistance.DatabaseClient;
import com.market.metrics.model.AnomalyEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {
    private DatabaseClient databaseClient;

    public MetricsController(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @GetMapping("/snapshots")
    public List<MetricSnapshot> getMetrics(){
        return databaseClient.selectAllSnapshots();
    }

    @GetMapping("/snapshots/range")
    public List<MetricSnapshot> getMetricsRange(@RequestParam Instant from, @RequestParam Instant to){
        return databaseClient.selectRangeSnapshots(from, to);
    }

    @GetMapping("/anomalies")
    public List<AnomalyEvent> getAnomalies(){
        return databaseClient.selectAllAnomalies();
    }

    @GetMapping("/anomalies/range")
    public List<AnomalyEvent> getAnomaliesRange(@RequestParam Instant from, @RequestParam Instant to){
        return databaseClient.selectRangeAnomalies(from, to);
    }
}
