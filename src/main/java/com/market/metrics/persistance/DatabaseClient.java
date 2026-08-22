package com.market.metrics.persistance;

import com.market.metrics.model.MetricSnapshot;
import com.market.metrics.model.AnomalyEvent;
import com.market.metrics.processing.PipelineMetricsPerMinute;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository // tells that it is functionality part of a bigger SpringBoot application
public class DatabaseClient {
    private JdbcTemplate jdbcTemplate;

    public DatabaseClient(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public MetricSnapshot insertSnapshot(PipelineMetricsPerMinute metrics) {
        String insert = "insert into metric_snapshots " +
                "(time_stamp, symbol, avgPrice, volume, volatility, tradeCount)" +
                "values (NOW(), ?, ?, ?, ?, ?)";

        MetricSnapshot metricSnapshot = new MetricSnapshot(Instant.now(), "BTC", metrics.getAvgPrice(), metrics.getTotalVolume(), metrics.getVolatilityOfReturns(), metrics.getTradeCount());

        jdbcTemplate.update(insert, "BTC", metrics.getAvgPrice(), metrics.getTotalVolume(), metrics.getVolatilityOfReturns(), metrics.getTradeCount());
        return metricSnapshot;
    }

    public void insertAnomaly(AnomalyEvent anomalyEvent) {
        String insert = "insert into anomaly_events " +
                "(time_stamp, eventType, symbol, change)" +
                "values (?, ?, ?, ?)";

        jdbcTemplate.update(insert, Timestamp.from(anomalyEvent.getTimeStamp()), anomalyEvent.getType().toString(), anomalyEvent.getStock(), anomalyEvent.getChange());
    }

    public List<MetricSnapshot> selectAllSnapshots() {
        String sql = "select * from metric_snapshots";

        return jdbcTemplate.query(sql, (rs, rowNr) -> new MetricSnapshot(
                rs.getTimestamp("time_stamp").toInstant(),
                rs.getString("symbol"),
                rs.getDouble("avgPrice"),
                rs.getDouble("volume"),
                rs.getDouble("volatility"),
                rs.getInt("tradeCount")

        ));
    }

    public List<MetricSnapshot> selectRangeSnapshots(Instant from, Instant to) {
        String sql = "select * from metric_snapshots " +
                "where time_stamp >= ? and time_stamp <= ? order by time_stamp";

        return jdbcTemplate.query(sql, (rs, rowNr) -> new MetricSnapshot(
                rs.getTimestamp("time_stamp").toInstant(),
                rs.getString("symbol"),
                rs.getDouble("avgPrice"),
                rs.getDouble("volume"),
                rs.getDouble("volatility"),
                rs.getInt("tradeCount")
            ), Timestamp.from(from), Timestamp.from(to));
    }

    public List<AnomalyEvent> selectAllAnomalies() {
        String sql = "select * from anomaly_events";

        return jdbcTemplate.query(sql, (rs, rowNr) -> new AnomalyEvent(
                AnomalyEvent.AnomalyType.valueOf(rs.getString("eventType")),
                rs.getDouble("change"),
                rs.getString("symbol"),
                rs.getTimestamp("time_stamp").toInstant()
        ));
    }

    public List<AnomalyEvent> selectRangeAnomalies(Instant from, Instant to) {
        String sql = "select * from anomaly_events " +
                "where time_stamp >= ? and time_stamp <= ? order by time_stamp";

        return jdbcTemplate.query(sql, (rs, rowNr) -> new AnomalyEvent(
                AnomalyEvent.AnomalyType.valueOf(rs.getString("eventType")),
                rs.getDouble("change"),
                rs.getString("symbol"),
                rs.getTimestamp("time_stamp").toInstant()
        ), Timestamp.from(from), Timestamp.from(to));
    }
}
