import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FlashSaleInventoryManager {

    // Product -> Stock
    private ConcurrentHashMap<String, AtomicInteger> inventory;

    // Product -> Waiting List (FIFO)
    private ConcurrentHashMap<String, Queue<Integer>> waitingList;

    public FlashSaleInventoryManager() {
        inventory = new ConcurrentHashMap<>();
        waitingList = new ConcurrentHashMap<>();
    }

    // Add product with stock
    public void addProduct(String productId, int stock) {
        inventory.put(productId, new AtomicInteger(stock));
        waitingList.put(productId, new ConcurrentLinkedQueue<>());
    }

    // Check stock (O(1))
    public int checkStock(String productId) {
        AtomicInteger stock = inventory.get(productId);
        return (stock == null) ? 0 : stock.get();
    }

    // Purchase item (Thread-safe, O(1))
    public String purchaseItem(String productId, int userId) {
        AtomicInteger stock = inventory.get(productId);

        if (stock == null) {
            return "Product not found";
        }

        // Atomic decrement
        while (true) {
            int currentStock = stock.get();

            if (currentStock <= 0) {
                // Add to waiting list
                Queue<Integer> queue = waitingList.get(productId);
                queue.add(userId);
                return "Out of stock. Added to waiting list. Position #" + queue.size();
            }

            // Try to decrement safely
            if (stock.compareAndSet(currentStock, currentStock - 1)) {
                return "Success! Remaining stock: " + (currentStock - 1);
            }
        }
    }

    // Process waiting list when stock is refilled
    public void restock(String productId, int addedStock) {
        AtomicInteger stock = inventory.get(productId);
        Queue<Integer> queue = waitingList.get(productId);

        if (stock == null) return;

        stock.addAndGet(addedStock);

        // Serve waiting users
        while (stock.get() > 0 && !queue.isEmpty()) {
            int user = queue.poll();
            stock.decrementAndGet();
            System.out.println("User " + user + " from waiting list purchased item.");
        }
    }

    // MAIN method for testing
    public static void main(String[] args) throws InterruptedException {
        FlashSaleInventoryManager manager = new FlashSaleInventoryManager();

        String product = "IPHONE15_256GB";

        // Add product with 100 units
        manager.addProduct(product, 100);

        System.out.println("Initial stock: " + manager.checkStock(product));

        // Simulate 200 concurrent users
        ExecutorService executor = Executors.newFixedThreadPool(50);

        for (int i = 1; i <= 200; i++) {
            int userId = i;

            executor.submit(() -> {
                String result = manager.purchaseItem(product, userId);
                System.out.println("User " + userId + ": " + result);
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("Final stock: " + manager.checkStock(product));
    }
}