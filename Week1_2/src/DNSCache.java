import java.util.*;

public class DNSCache {

    // DNS Entry class
    static class DNSEntry {
        String domain;
        String ipAddress;
        long expiryTime;

        DNSEntry(String domain, String ipAddress, long ttlSeconds) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    // LRU Cache using LinkedHashMap
    private final int capacity;

    private LinkedHashMap<String, DNSEntry> cache;

    // Metrics
    private int hits = 0;
    private int misses = 0;
    private long totalLookupTime = 0;

    public DNSCache(int capacity) {
        this.capacity = capacity;

        this.cache = new LinkedHashMap<>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > DNSCache.this.capacity;
            }
        };

        startCleanupThread();
    }

    // Resolve domain
    public synchronized String resolve(String domain) {
        long start = System.nanoTime();

        DNSEntry entry = cache.get(domain);

        if (entry != null) {
            if (!entry.isExpired()) {
                hits++;
                long time = System.nanoTime() - start;
                totalLookupTime += time;

                return "Cache HIT → " + entry.ipAddress;
            } else {
                cache.remove(domain);
            }
        }

        // Cache miss
        misses++;

        // Simulate upstream DNS call
        String newIP = queryUpstreamDNS(domain);

        // Store with TTL (example: 5 seconds)
        cache.put(domain, new DNSEntry(domain, newIP, 5));

        long time = System.nanoTime() - start;
        totalLookupTime += time;

        return "Cache MISS → " + newIP;
    }

    // Simulated upstream DNS lookup
    private String queryUpstreamDNS(String domain) {
        try {
            Thread.sleep(100); // simulate latency
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "172.217." + new Random().nextInt(255) + "." + new Random().nextInt(255);
    }

    // Background cleanup thread
    private void startCleanupThread() {
        Thread cleaner = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000);

                    synchronized (this) {
                        Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();

                        while (it.hasNext()) {
                            Map.Entry<String, DNSEntry> entry = it.next();
                            if (entry.getValue().isExpired()) {
                                it.remove();
                            }
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        cleaner.setDaemon(true);
        cleaner.start();
    }

    // Get stats
    public String getCacheStats() {
        int total = hits + misses;
        double hitRate = (total == 0) ? 0 : (hits * 100.0 / total);

        double avgLookupMs = (total == 0) ? 0 :
                (totalLookupTime / 1_000_000.0) / total;

        return "Hit Rate: " + String.format("%.2f", hitRate) +
                "%, Avg Lookup Time: " + String.format("%.2f", avgLookupMs) + " ms";
    }

    // MAIN
    public static void main(String[] args) throws InterruptedException {
        DNSCache cache = new DNSCache(3);

        System.out.println(cache.resolve("google.com")); // MISS
        System.out.println(cache.resolve("google.com")); // HIT

        Thread.sleep(6000); // wait for TTL to expire

        System.out.println(cache.resolve("google.com")); // EXPIRED → MISS

        System.out.println(cache.resolve("openai.com")); // MISS
        System.out.println(cache.resolve("github.com")); // MISS
        System.out.println(cache.resolve("stackoverflow.com")); // triggers LRU eviction

        System.out.println(cache.getCacheStats());
    }
}