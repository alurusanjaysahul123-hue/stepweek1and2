import java.util.*;

public class AutocompleteSystem {

    // Trie Node
    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        PriorityQueue<String> topQueries = new PriorityQueue<>(
                (a, b) -> freqMap.get(a) - freqMap.get(b) // min-heap
        );
    }

    private TrieNode root;
    private static final int K = 10;

    // Global frequency map
    private static Map<String, Integer> freqMap = new HashMap<>();

    public AutocompleteSystem() {
        root = new TrieNode();
    }

    // Insert or update query
    public void insert(String query) {
        freqMap.put(query, freqMap.getOrDefault(query, 0) + 1);

        TrieNode node = root;

        for (char c : query.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);

            // Update top queries at this prefix
            if (!node.topQueries.contains(query)) {
                node.topQueries.offer(query);
            }

            if (node.topQueries.size() > K) {
                node.topQueries.poll(); // remove lowest freq
            }
        }
    }

    // Search suggestions
    public List<String> search(String prefix) {
        TrieNode node = root;

        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return Collections.emptyList();
            }
            node = node.children.get(c);
        }

        List<String> result = new ArrayList<>(node.topQueries);

        // Sort descending by frequency
        result.sort((a, b) -> freqMap.get(b) - freqMap.get(a));

        return result;
    }

    // MAIN
    public static void main(String[] args) {
        AutocompleteSystem system = new AutocompleteSystem();

        // Insert queries
        system.insert("java tutorial");
        system.insert("javascript");
        system.insert("java download");
        system.insert("java tutorial");
        system.insert("java tutorial");
        system.insert("java 21 features");

        // Search
        System.out.println("Suggestions for 'jav':");
        List<String> suggestions = system.search("jav");

        int rank = 1;
        for (String s : suggestions) {
            System.out.println(rank++ + ". " + s + " (" + freqMap.get(s) + ")");
        }

        // Update frequency
        system.insert("java 21 features");
        system.insert("java 21 features");

        System.out.println("\nAfter trending update:");
        suggestions = system.search("jav");

        rank = 1;
        for (String s : suggestions) {
            System.out.println(rank++ + ". " + s + " (" + freqMap.get(s) + ")");
        }
    }
}