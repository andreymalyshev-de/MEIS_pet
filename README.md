# MEIS - Market Trade-Event Ingestion System

MEIS takes real-time trading data from Binance (currently BTC/USDT trades) and processes it to provide an overview of recent price movements, trading volume, volatility, and detected anomalies as well as the ability to query historical market metrics by timestamp.


<img width="1332" height="872" alt="image" src="https://github.com/user-attachments/assets/326aa2ea-0d19-42f4-801a-9affaef90432" />


## In detail🔎

`BinanceWebSocketClient` connects to the Binance WebSocket API and receives BTC/USDT trades in real time.

For every trade, it creates a `TradeEvent` containing:

- symbol
- price
- quantity
- timestamp

Each event is inserted into a bounded shared queue.

`EventProcessor` runs as a consumer thread and processes events from this queue. If the queue is full, incoming events are dropped and counted through pipeline metrics.

The processor maintains a rolling 60-second window of trades. Events are added and removed from this window according to their timestamps.

Based on the current window, the system calculates:

- average price
- traded volume
- return volatility
- trade count

During processing, the system also performs rule-based anomaly detection. Unusual price movements relative to the recent market baseline are classified as `PRICE_SPIKE` or `PRICE_FALL` events and persisted in PostgreSQL.

The rolling metrics are shared with a separate `Logger` thread. Every 30 seconds, the logger creates a snapshot of the current 60-second metrics and stores it in PostgreSQL.

Current metrics and anomalies are published live to the local dashboard through Server-Sent Events (SSE). Historical snapshots and anomalies can be queried through REST endpoints and through the dashboard.

## Stack⚙️

The project is implemented in Java and uses:

- Spring Boot
- PostgreSQL
- REST
- Server-Sent Events (SSE)
- Binance WebSocket API

## Results📋

Synthetic pipeline load tests achieved:

- up to **250,000 events/sec sustained processing throughput with 0 dropped events**

<!-- Add synthetic benchmark screenshot here -->

Historical REST API benchmark:

- database size: **100,000 metric snapshots**
- query range: **last 3,600 seconds (1 hour)**
- test runs: **500**
- median end-to-end latency: **2,637 ms**
- p95 latency: **4,421 ms**

<!-- Add PostgreSQL benchmark screenshot here -->

The latency benchmark measures the complete local request path:

`HTTP client -> Spring REST controller -> PostgreSQL -> JSON response`

## How to run locally✍🏼

1. Clone the repository.
2. Install PostgreSQL.
3. Fill the PostgreSQL database used by the application, e.g. via
   
  INSERT INTO metric_snapshots
    (time_stamp, symbol, avgPrice, volume, volatility, tradeCount)
    SELECT
      NOW() - (i * INTERVAL '30 seconds'),
      'BTC',
      60000 + random() * 1000,
      random() * 20,
      random() * 0.01,
      (1000 + random() * 500)::int
FROM generate_series(1, 100000) AS i;


5. Adjust the database connection in `application.properties` for your postgreSQL credentials and provide the database password through the `DB_PASSWORD` environment variable for the SyntheticLoadBenchmark test.
6. Run the Spring Boot application.

The application automatically creates the required tables and indexes on startup.

Open the dashboard at:

`http://localhost:8080/`

## Benchmarks🧪

Benchmark code is located in the `benchmarks` package.

### Pipeline throughput

Run:

`SyntheticLoadBenchmark`

This generates synthetic `TradeEvent`s at increasing rates and measures:

- processed events/sec
- dropped events
- queue utilization

### Historical API latency

Before running `PostgresRangeBenchmark`, populate the database with the benchmark dataset and start the normal Spring Boot application.

Then run:

`PostgresRangeBenchmark`

It sends repeated HTTP requests to the historical metrics REST endpoint and measures median and p95 response latency.

<br><br>

P.S.: there is also a README/diary of the project development in the main/java folder. feel free to check it out :)
