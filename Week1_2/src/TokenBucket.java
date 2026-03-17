import java.util.concurrent.*;
import java.util.*;

public class RateLimiter {

    // Token Bucket class
    static class TokenBucket {
        private final int maxTokens;
        private final double refillRate; // tokens per second

        private double tokens;
        private long lastRefillTime;

        public TokenBucket(int maxTokens, int refillPerHour) {
            this.maxTokens = maxTokens;
            this.refillRate = refillPerHour / 3600.0; // per second
            this.tokens = maxTokens;
            this.lastRefillTime = System.currentTimeMillis();
        }

        // Refill tokens based on elapsed time
        private void refill() {
            long now = System.currentTimeMillis();
            double seconds = (now - lastRefillTime) / 1000.0;

            double tokensToAdd = seconds * refillRate;
            tokens = Math.min(maxTokens, tokens + tokensToAdd);

            lastRefillTime = now;
        }

        // Try consuming a token
        public synchronized boolean allowRequest() {
            refill();

            if (tokens >= 1) {
                tokens -= 1;
                return true;
            }
            return false;
        }

        public synchronized int getRemainingTokens() {
            refill();
            return (int) tokens;
        }

        public long getRetryAfterSeconds() {
            if (tokens >= 1) return 0;

            double missing = 1 - tokens;
            return (long) Math.ceil(missing / refillRate);
        }
    }

    // Client -> TokenBucket
    private ConcurrentHashMap<String, TokenBucket> clients = new ConcurrentHashMap<>();

    private final int MAX_TOKENS = 1000;

    // Check rate limit
    public String checkRateLimit(String clientId) {

        TokenBucket bucket = clients.computeIfAbsent(
                clientId,
                id -> new TokenBucket(MAX_TOKENS, MAX_TOKENS)
        );

        if (bucket.allowRequest()) {
            return "Allowed (" + bucket.getRemainingTokens() + " requests remaining)";
        } else {
            return "Denied (0 remaining, retry after "
                    + bucket.getRetryAfterSeconds() + "s)";
        }
    }

    // Get status
    public String getRateLimitStatus(String clientId) {
        TokenBucket bucket = clients.get(clientId);

        if (bucket == null) return "No data";

        int remaining = bucket.getRemainingTokens();
        int used = MAX_TOKENS - remaining;

        return "{used: " + used +
                ", limit: " + MAX_TOKENS +
                ", remaining: " + remaining + "}";
    }

    // MAIN
    public static void main(String[] args) {
        RateLimiter limiter = new RateLimiter();

        String client = "abc123";

        // Simulate requests
        for (int i = 0; i < 1005; i++) {
            String result = limiter.checkRateLimit(client);
            System.out.println("Request " + (i + 1) + ": " + result);
        }

        System.out.println(limiter.getRateLimitStatus(client));
    }
}