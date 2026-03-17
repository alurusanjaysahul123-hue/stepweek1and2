import java.util.*;
import java.util.concurrent.*;

public class RealTimeAnalytics {

    // Page URL -> total visits
    private ConcurrentHashMap<String, Integer> pageViews = new ConcurrentHashMap<>();

    // Page URL -> unique users
    private ConcurrentHashMap<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();

    // Traffic source -> count
    private ConcurrentHashMap<String, Integer> trafficSources = new ConcurrentHashMap<>();

    // Process incoming event
    public void processEvent(String url, String userId, String source) {

        // Update page views
        pageViews.merge(url, 1, Integer::sum);

        // Update unique visitors
        uniqueVisitors
                .computeIfAbsent(url, k -> ConcurrentHashMap.newKeySet())
                .add(userId);

        // Update traffic sources
        trafficSources.merge(source, 1, Integer::sum);
    }

    // Get top 10 pages
    public List<String> getTopPages() {
        PriorityQueue<Map.Entry<String, Integer>> minHeap =
                new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));

        for (Map.Entry<String, Integer> entry : pageViews.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > 10) {
                minHeap.poll();
            }
        }

        List<String> result = new ArrayList<>();

        while (!minHeap.isEmpty()) {
            Map.Entry<String, Integer> entry = minHeap.poll();
            String url = entry.getKey();
            int views = entry.getValue();
            int unique = uniqueVisitors.get(url).size();

            result.add(url + " - " + views + " views (" + unique + " unique)");
        }

        Collections.reverse(result);
        return result;
    }

    // Get traffic source stats
    public String getTrafficStats() {
        int total = trafficSources.values().stream().mapToInt(i -> i).sum();

        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, Integer> entry : trafficSources.entrySet()) {
            double percent = (entry.getValue() * 100.0) / total;
            sb.append(entry.getKey())
                    .append(": ")
                    .append(String.format("%.1f", percent))
                    .append("%, ");
        }

        return sb.toString();
    }

    // Dashboard
    public void printDashboard() {
        System.out.println("\n===== REAL-TIME DASHBOARD =====");

        System.out.println("Top Pages:");
        List<String> topPages = getTopPages();
        int rank = 1;
        for (String page : topPages) {
            System.out.println(rank++ + ". " + page);
        }

        System.out.println("\nTraffic Sources:");
        System.out.println(getTrafficStats());
    }

    // MAIN
    public static void main(String[] args) {
        RealTimeAnalytics analytics = new RealTimeAnalytics();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // Simulate incoming traffic
        Runnable eventGenerator = () -> {
            String[] urls = {"/article/breaking-news", "/sports/championship", "/tech/ai"};
            String[] sources = {"google", "facebook", "direct"};

            Random rand = new Random();

            for (int i = 0; i < 100; i++) {
                String url = urls[rand.nextInt(urls.length)];
                String user = "user_" + rand.nextInt(50);
                String source = sources[rand.nextInt(sources.length)];

                analytics.processEvent(url, user, source);
            }
        };

        // Generate events continuously
        scheduler.scheduleAtFixedRate(eventGenerator, 0, 1, TimeUnit.SECONDS);

        // Update dashboard every 5 seconds
        scheduler.scheduleAtFixedRate(analytics::printDashboard, 5, 5, TimeUnit.SECONDS);
    }
}