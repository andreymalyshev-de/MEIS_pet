import javax.swing.plaf.nimbus.State;
import java.sql.*;
import java.time.Instant;
import java.util.concurrent.locks.Condition;

public class DatabaseClient {
    private static final String URL = "jdbc:postgresql://localhost:5432/metrics_analytics";
    private static final String USER = "postgres"; //default username
    private static final String PASSWORD = System.getenv("DB_PASSWORD");
    private static final Connection connection;

    static {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void insertSnapshot(PipelineMetricsPerMinute metrics) {
        String insert = "insert into metric_snapshots " +
                "(time_stamp, symbol, avgPrice, volume, volatility, tradeCount)" +
                "values (NOW(), ?, ?, ?, ?, ?)";

        try {
            PreparedStatement stmt = connection.prepareStatement(insert);
            stmt.setString(1, "BTC");
            stmt.setDouble(2, metrics.getAvgPrice());
            stmt.setDouble(3, metrics.getTotalVolume());
            stmt.setDouble(4, metrics.getVolatilityOfReturns());
            stmt.setLong(5, metrics.getTradeCount());

            stmt.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println("the snapshot could not be inserted"
            + e.getMessage());
        }
    }

    public static void insertAnomaly(AnomalyEvent anomalyEvent) {
        String insert = "insert into anomaly_events " +
                "(time_stamp, eventType, symbol, change)" +
                "values (?, ?, ?, ?)";

        try {
            PreparedStatement stmt = connection.prepareStatement(insert);
            stmt.setTimestamp(1, Timestamp.from(anomalyEvent.getTimeStamp()));
            stmt.setString(2, anomalyEvent.getType().toString());
            stmt.setString(3, anomalyEvent.getStock());
            stmt.setDouble(4, anomalyEvent.getChange());

            stmt.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println("the anomaly could not be inserted" +"\n"
             + e.getMessage());
        }
    }
}
