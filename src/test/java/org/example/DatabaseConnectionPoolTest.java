package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseConnectionPoolTest {

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void multipleConnections_ShouldNotBlock() throws Exception {
        // Given
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        // When
        for (int i = 0; i < 5; i++) {
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                try {
                    Connection conn = DriverManager.getConnection("jdbc:h2:mem:perftest", "", "");
                    Thread.sleep(100); // Simulate work
                    conn.close();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }, executor);
            futures.add(future);
        }

        // Then
        for (CompletableFuture<Boolean> future : futures) {
            assertThat(future.get()).isTrue();
        }

        executor.shutdown();
    }

    @Test
    void connectionCreation_ShouldBeFast() {
        // Given
        long startTime = System.currentTimeMillis();

        // When
        try {
            for (int i = 0; i < 10; i++) {
                Connection conn = DriverManager.getConnection("jdbc:h2:mem:speedtest" + i, "", "");
                conn.close();
            }
        } catch (Exception e) {
            // Handle exception
        }

        // Then
        long duration = System.currentTimeMillis() - startTime;
        assertThat(duration).isLessThan(1000); // Should complete in less than 1 second
    }
}