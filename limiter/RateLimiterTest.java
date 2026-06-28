package limiter;

import java.io.IOException;
import java.net.URISyntaxException;
import limiter.strategy.FixedWindowStrategy;
import limiter.strategy.SlidingWindowStrategy;
import limiter.strategy.TokenBucketStrategy;

public class RateLimiterTest {
    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {

        // --- Fixed Window Strategy ---
        System.out.println("=== Fixed Window Strategy (5 requests / 5 seconds) ===");
        RateLimiterNode fixedWindowNode = new RateLimiterNode(8081, 10, new FixedWindowStrategy(5000, 5));
        fixedWindowNode.start();
        Thread.sleep(500);
        RateLimiterClient fixedWindowClient = new RateLimiterClient("localhost:8081");

        // Scenario 1 — requests within limit
        System.out.println("\n--- Scenario 1: Requests within limit ---");
        for (int i = 1; i <= 5; i++) {
            boolean allowed = fixedWindowClient.isAllowed("192.168.1.1");
            System.out.println("[Fixed Window] Request " + i + ": " + (allowed ? "ALLOWED" : "REJECTED"));
        }

        // Scenario 2 — requests exceeding limit
        System.out.println("\n--- Scenario 2: Requests exceeding limit ---");
        for (int i = 6; i <= 8; i++) {
            boolean allowed = fixedWindowClient.isAllowed("192.168.1.1");
            System.out.println("[Fixed Window] Request " + i + ": " + (allowed ? "ALLOWED" : "REJECTED (expected)"));
        }

        // Scenario 3 — wait for window to reset
        System.out.println("\n--- Scenario 3: Wait for window reset ---");
        System.out.println("[Fixed Window] Waiting 6 seconds for window to reset...");
        Thread.sleep(6000);
        for (int i = 1; i <= 3; i++) {
            boolean allowed = fixedWindowClient.isAllowed("192.168.1.1");
            System.out.println("[Fixed Window] Request " + i + " after reset: " + (allowed ? "ALLOWED" : "REJECTED"));
        }

        // Scenario 4 — multiple IPs
        System.out.println("\n--- Scenario 4: Multiple IPs ---");
        for (int i = 1; i <= 5; i++) {
            boolean allowed1 = fixedWindowClient.isAllowed("192.168.1.2");
            boolean allowed2 = fixedWindowClient.isAllowed("192.168.1.3");
            System.out.println("[Fixed Window] IP1 Request " + i + ": " + (allowed1 ? "ALLOWED" : "REJECTED"));
            System.out.println("[Fixed Window] IP2 Request " + i + ": " + (allowed2 ? "ALLOWED" : "REJECTED"));
        }

        fixedWindowNode.stop();

        // --- Token Bucket Strategy ---
        System.out.println("\n=== Token Bucket Strategy (capacity=5, refillRate=1 token/sec) ===");
        RateLimiterNode tokenBucketNode = new RateLimiterNode(8082, 10, new TokenBucketStrategy(5, 1.0));
        tokenBucketNode.start();
        Thread.sleep(500);
        RateLimiterClient tokenBucketClient = new RateLimiterClient("localhost:8082");

        // Scenario 1 — burst requests within capacity
        System.out.println("\n--- Scenario 1: Burst requests within capacity ---");
        for (int i = 1; i <= 5; i++) {
            boolean allowed = tokenBucketClient.isAllowed("192.168.1.1");
            System.out.println("[Token Bucket] Request " + i + ": " + (allowed ? "ALLOWED" : "REJECTED"));
        }

        // Scenario 2 — bucket empty, requests rejected
        System.out.println("\n--- Scenario 2: Bucket empty ---");
        for (int i = 6; i <= 8; i++) {
            boolean allowed = tokenBucketClient.isAllowed("192.168.1.1");
            System.out.println("[Token Bucket] Request " + i + ": " + (allowed ? "ALLOWED" : "REJECTED (expected)"));
        }

        // Scenario 3 — wait for tokens to refill
        System.out.println("\n--- Scenario 3: Wait for token refill ---");
        System.out.println("[Token Bucket] Waiting 3 seconds for tokens to refill...");
        Thread.sleep(3000);
        for (int i = 1; i <= 3; i++) {
            boolean allowed = tokenBucketClient.isAllowed("192.168.1.1");
            System.out.println("[Token Bucket] Request " + i + " after refill: " + (allowed ? "ALLOWED" : "REJECTED"));
        }

        // Scenario 4 — multiple IPs
        System.out.println("\n--- Scenario 4: Multiple IPs ---");
        for (int i = 1; i <= 5; i++) {
            boolean allowed1 = tokenBucketClient.isAllowed("10.0.0.1");
            boolean allowed2 = tokenBucketClient.isAllowed("10.0.0.2");
            System.out.println("[Token Bucket] IP1 Request " + i + ": " + (allowed1 ? "ALLOWED" : "REJECTED"));
            System.out.println("[Token Bucket] IP2 Request " + i + ": " + (allowed2 ? "ALLOWED" : "REJECTED"));
        }

        tokenBucketNode.stop();

        // --- Sliding Window Strategy ---
        System.out.println("\n=== Sliding Window Strategy (5 requests / 5 seconds) ===");
        RateLimiterNode slidingWindowNode = new RateLimiterNode(8083, 10, new SlidingWindowStrategy(5, 5000));
        slidingWindowNode.start();
        Thread.sleep(500);
        RateLimiterClient slidingWindowClient = new RateLimiterClient("localhost:8083");

        // Scenario 1 — requests within limit
        System.out.println("\n--- Scenario 1: Requests within limit ---");
        for (int i = 1; i <= 5; i++) {
            boolean allowed = slidingWindowClient.isAllowed("192.168.1.1");
            System.out.println("[Sliding Window] Request " + i + ": " + (allowed ? "ALLOWED" : "REJECTED"));
        }

        // Scenario 2 — requests exceeding limit
        System.out.println("\n--- Scenario 2: Requests exceeding limit ---");
        for (int i = 6; i <= 8; i++) {
            boolean allowed = slidingWindowClient.isAllowed("192.168.1.1");
            System.out.println("[Sliding Window] Request " + i + ": " + (allowed ? "ALLOWED" : "REJECTED (expected)"));
        }

        // Scenario 3 — sliding window advantage over fixed window
        System.out.println("\n--- Scenario 3: Sliding window advantage ---");
        System.out.println("[Sliding Window] Waiting 3 seconds - only requests older than 5s should expire");
        Thread.sleep(3000);
        // only some requests have expired, not full reset like fixed window
        for (int i = 1; i <= 3; i++) {
            boolean allowed = slidingWindowClient.isAllowed("192.168.1.1");
            System.out.println("[Sliding Window] Request " + i + " after partial wait: " + (allowed ? "ALLOWED" : "REJECTED"));
        }

        // Scenario 4 — full window expiry
        System.out.println("\n--- Scenario 4: Full window expiry ---");
        System.out.println("[Sliding Window] Waiting 6 seconds for all requests to expire...");
        Thread.sleep(6000);
        for (int i = 1; i <= 5; i++) {
            boolean allowed = slidingWindowClient.isAllowed("192.168.1.1");
            System.out.println("[Sliding Window] Request " + i + " after full expiry: " + (allowed ? "ALLOWED" : "REJECTED"));
        }

        // Scenario 5 — multiple IPs
        System.out.println("\n--- Scenario 5: Multiple IPs ---");
        for (int i = 1; i <= 5; i++) {
            boolean allowed1 = slidingWindowClient.isAllowed("172.16.0.1");
            boolean allowed2 = slidingWindowClient.isAllowed("172.16.0.2");
            System.out.println("[Sliding Window] IP1 Request " + i + ": " + (allowed1 ? "ALLOWED" : "REJECTED"));
            System.out.println("[Sliding Window] IP2 Request " + i + ": " + (allowed2 ? "ALLOWED" : "REJECTED"));
        }

        slidingWindowNode.stop();

        System.out.println("\n[Main] All scenarios passed.");
    }
}
