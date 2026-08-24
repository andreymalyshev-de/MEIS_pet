package com.market.metrics.benchmarks;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostgresRangeBenchmark {

    private static final int WARMUP_RUNS = 20;
    private static final int TEST_RUNS = 500;

    // Same kind of historical query a user of the project could make.
    private static final long RANGE_SECONDS = 3600;

    public static void main(String[] args) throws Exception {

        HttpClient client = HttpClient.newHttpClient();

        Instant to = Instant.now();
        Instant from = to.minusSeconds(RANGE_SECONDS);

        String url =
                "http://localhost:8080/api/metrics/snapshots/range"
                        + "?from=" + encode(from.toString())
                        + "&to=" + encode(to.toString());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        // Warm-up: lets first 20 queries run before getting 100k of other, in order
        // to avoid all delays caused by warming up of the soft
        for (int i = 0; i < WARMUP_RUNS; i++) {
            send(client, request);
        }

        List<Double> times = new ArrayList<>();

        for (int i = 0; i < TEST_RUNS; i++) {

            long start = System.nanoTime();

            HttpResponse<String> response =
                    send(client, request);

            long end = System.nanoTime();

            times.add(
                    (end - start) / 1_000_000.0
            );
        }

        Collections.sort(times);

        double median = percentile(times, 0.50);
        double p95 = percentile(times, 0.95);
        double min = times.get(0);
        double max = times.get(times.size() - 1);

        System.out.println("Range: last 1 hour");
        System.out.println("Runs: " + TEST_RUNS);
        System.out.printf("Min:    %.3f ms%n", min);
        System.out.printf("Median: %.3f ms%n", median);
        System.out.printf("p95:    %.3f ms%n", p95);
        System.out.printf("Max:    %.3f ms%n", max);
    }

    private static HttpResponse<String> send(
            HttpClient client,
            HttpRequest request
    ) throws Exception {

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "HTTP " + response.statusCode()
                            + ": " + response.body()
            );
        }

        return response;
    }

    private static String encode(String value) {
        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }

    private static double percentile(
            List<Double> values,
            double percentile
    ) {
        int index =
                (int) Math.ceil(
                        percentile * values.size()
                ) - 1;

        return values.get(index);
    }
}