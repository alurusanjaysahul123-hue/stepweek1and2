import java.util.*;

public class MultiLevelCache {

    // Simulated DB
    private Map<String, String> database = new HashMap<>();

    // Access count for promotion
    private Map<String, Integer> accessCount = new HashMap<>();

    // Stats
    private int l1Hits = 0, l2Hits = 0, l3Hits = 0;

    // L1 Cache (LRU)
    private LinkedHashMap<String, String> L1;

    // L2 Cache (LRU)
    private LinkedHashMap<String, String> L2;

    private final int L1_CAP = 3;
    private final int L2_CAP = 5;

    public MultiLevelCache() {

        // L1
        L1 = new LinkedHashMap<>(L1_CAP, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, String> e) {
                return size() > L1_CAP;
            }
        };

        // L2
        L2 = new LinkedHashMap<>(L2_CAP, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, String> e) {
                return size() > L2_CAP;
            }
        };

        // Preload DB
        for (int i = 1; i <= 10; i++) {
            database.put("video_" + i, "VideoData_" + i);
        }
    }

    // Get video
    public String getVideo(String videoId) {

        long start = System.nanoTime();

        // L1 check
        if (L1.containsKey(videoId)) {
            l1Hits++;
            return log("L1 HIT", videoId, start);
        }

        // L2 check
        if (L2.containsKey(videoId)) {
            l2Hits++;

            // Promote to L1
            L1.put(videoId, L2.get(videoId));

            return log("L2 HIT → Promoted to L1", videoId, start);
        }

        // L3 (DB)
        if (database.containsKey(videoId)) {
            l3Hits++;

            // Add to L2
            L2.put(videoId, database.get(videoId));

            // Track access
            accessCount.put(videoId,
                    accessCount.getOrDefault(videoId, 0) + 1);

            return log("L3 HIT → Added to L2", videoId, start);
        }

        return "Video not found";
    }

    // Log response time
    private String log(String level, String videoId, long start) {
        long time = (System.nanoTime() - start) / 1_000_000;
        return level + " (" + time + " ms)";
    }

    // Invalidate cache
    public void invalidate(String videoId) {
        L1.remove(videoId);
        L2.remove(videoId);
        database.remove(videoId);
    }

    // Stats
    public void getStats() {
        int total = l1Hits + l2Hits + l3Hits;

        System.out.println("\n===== CACHE STATS =====");

        System.out.println("L1 Hit Rate: " + percent(l1Hits, total));
        System.out.println("L2 Hit Rate: " + percent(l2Hits, total));
        System.out.println("L3 Hit Rate: " + percent(l3Hits, total));
        System.out.println("Overall Hit Rate: "
                + percent(l1Hits + l2Hits, total));
    }

    private String percent(int x, int total) {
        return String.format("%.2f%%", (x * 100.0) / total);
    }

    // MAIN
    public static void main(String[] args) {
        MultiLevelCache cache = new MultiLevelCache();

        System.out.println(cache.getVideo("video_1")); // L3
        System.out.println(cache.getVideo("video_1")); // L2 → L1
        System.out.println(cache.getVideo("video_1")); // L1

        System.out.println(cache.getVideo("video_2")); // L3
        System.out.println(cache.getVideo("video_3")); // L3
        System.out.println(cache.getVideo("video_2")); // L2

        cache.getStats();
    }
}