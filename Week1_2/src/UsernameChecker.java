import java.util.*;

public class UsernameChecker {

    // Stores registered usernames
    private HashMap<String, Integer> usernameToUserId;

    // Tracks how many times a username was searched
    private HashMap<String, Integer> attemptFrequency;

    public UsernameChecker() {
        usernameToUserId = new HashMap<>();
        attemptFrequency = new HashMap<>();
    }

    // Register a username
    public void registerUser(String username, int userId) {
        usernameToUserId.put(username, userId);
    }

    // Check availability in O(1)
    public boolean checkAvailability(String username) {
        // Track attempt frequency
        attemptFrequency.put(username, attemptFrequency.getOrDefault(username, 0) + 1);

        return !usernameToUserId.containsKey(username);
    }

    // Suggest alternative usernames
    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();

        if (checkAvailability(username)) {
            suggestions.add(username);
            return suggestions;
        }

        // Strategy 1: append numbers
        for (int i = 1; i <= 5; i++) {
            String newUsername = username + i;
            if (!usernameToUserId.containsKey(newUsername)) {
                suggestions.add(newUsername);
            }
        }

        // Strategy 2: replace underscore with dot
        if (username.contains("_")) {
            String modified = username.replace("_", ".");
            if (!usernameToUserId.containsKey(modified)) {
                suggestions.add(modified);
            }
        }

        return suggestions;
    }

    // Get most attempted username
    public String getMostAttempted() {
        String result = null;
        int max = 0;

        for (Map.Entry<String, Integer> entry : attemptFrequency.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                result = entry.getKey();
            }
        }

        return result + " (" + max + " attempts)";
    }

    // Main method for testing
    public static void main(String[] args) {
        UsernameChecker checker = new UsernameChecker();

        // Pre-register some users
        checker.registerUser("john_doe", 1);
        checker.registerUser("admin", 2);

        // Check availability
        System.out.println("john_doe available? " + checker.checkAvailability("john_doe"));
        System.out.println("jane_smith available? " + checker.checkAvailability("jane_smith"));

        // Suggestions
        System.out.println("Suggestions for john_doe: " + checker.suggestAlternatives("john_doe"));

        // Simulate repeated attempts
        for (int i = 0; i < 100; i++) {
            checker.checkAvailability("admin");
        }

        // Most attempted
        System.out.println("Most attempted: " + checker.getMostAttempted());
    }
}