import java.util.*;

class Transaction {
    int id;
    int amount;
    String merchant;
    String account;
    long timestamp; // epoch millis

    public Transaction(int id, int amount, String merchant, String account, long timestamp) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.account = account;
        this.timestamp = timestamp;
    }
}

public class FinancialAnalyzer {

    private List<Transaction> transactions;

    public FinancialAnalyzer(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    // ✅ 1. Classic Two-Sum (O(n))
    public List<String> findTwoSum(int target) {
        Map<Integer, Transaction> map = new HashMap<>();
        List<String> result = new ArrayList<>();

        for (Transaction t : transactions) {
            int complement = target - t.amount;

            if (map.containsKey(complement)) {
                Transaction t2 = map.get(complement);
                result.add("(" + t2.id + ", " + t.id + ")");
            }

            map.put(t.amount, t);
        }

        return result;
    }

    // ✅ 2. Two-Sum with Time Window (1 hour)
    public List<String> findTwoSumWithTimeWindow(int target, long windowMillis) {
        List<String> result = new ArrayList<>();

        // Sort by time
        transactions.sort(Comparator.comparingLong(t -> t.timestamp));

        for (int i = 0; i < transactions.size(); i++) {
            Map<Integer, Transaction> map = new HashMap<>();

            for (int j = i; j < transactions.size(); j++) {
                if (transactions.get(j).timestamp - transactions.get(i).timestamp > windowMillis)
                    break;

                Transaction t = transactions.get(j);
                int complement = target - t.amount;

                if (map.containsKey(complement)) {
                    result.add("(" + map.get(complement).id + ", " + t.id + ")");
                }

                map.put(t.amount, t);
            }
        }

        return result;
    }

    // ✅ 3. K-Sum (recursive)
    public List<List<Integer>> findKSum(int k, int target) {
        List<List<Integer>> result = new ArrayList<>();
        backtrack(0, k, target, new ArrayList<>(), result);
        return result;
    }

    private void backtrack(int start, int k, int target,
                           List<Integer> current, List<List<Integer>> result) {

        if (k == 0 && target == 0) {
            result.add(new ArrayList<>(current));
            return;
        }

        if (k == 0 || target < 0) return;

        for (int i = start; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);

            current.add(t.id);
            backtrack(i + 1, k - 1, target - t.amount, current, result);
            current.remove(current.size() - 1);
        }
    }

    // ✅ 4. Duplicate Detection
    public List<String> detectDuplicates() {
        Map<String, Set<String>> map = new HashMap<>();
        List<String> result = new ArrayList<>();

        for (Transaction t : transactions) {
            String key = t.amount + "_" + t.merchant;

            map.computeIfAbsent(key, k -> new HashSet<>()).add(t.account);
        }

        for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
            if (entry.getValue().size() > 1) {
                result.add("Duplicate → " + entry.getKey()
                        + " Accounts: " + entry.getValue());
            }
        }

        return result;
    }

    // MAIN
    public static void main(String[] args) {
        List<Transaction> txns = Arrays.asList(
                new Transaction(1, 500, "StoreA", "acc1", System.currentTimeMillis()),
                new Transaction(2, 300, "StoreB", "acc2", System.currentTimeMillis()),
                new Transaction(3, 200, "StoreC", "acc3", System.currentTimeMillis()),
                new Transaction(4, 500, "StoreA", "acc4", System.currentTimeMillis())
        );

        FinancialAnalyzer analyzer = new FinancialAnalyzer(txns);

        System.out.println("Two Sum: " + analyzer.findTwoSum(500));

        System.out.println("Time Window Two Sum: "
                + analyzer.findTwoSumWithTimeWindow(500, 3600_000));

        System.out.println("K-Sum (k=3, target=1000): "
                + analyzer.findKSum(3, 1000));

        System.out.println("Duplicates: "
                + analyzer.detectDuplicates());
    }
}