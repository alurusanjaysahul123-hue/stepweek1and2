import java.util.*;

public class PlagiarismDetector {

    // n-gram size
    private static final int N = 5;

    // n-gram -> set of document IDs
    private HashMap<String, Set<String>> ngramIndex;

    // document -> list of its n-grams
    private HashMap<String, List<String>> documentNgrams;

    public PlagiarismDetector() {
        ngramIndex = new HashMap<>();
        documentNgrams = new HashMap<>();
    }

    // Add document to system
    public void addDocument(String docId, String text) {
        List<String> ngrams = generateNGrams(text);
        documentNgrams.put(docId, ngrams);

        for (String gram : ngrams) {
            ngramIndex
                    .computeIfAbsent(gram, k -> new HashSet<>())
                    .add(docId);
        }
    }

    // Generate n-grams
    private List<String> generateNGrams(String text) {
        List<String> result = new ArrayList<>();

        String[] words = text.toLowerCase().split("\\s+");

        for (int i = 0; i <= words.length - N; i++) {
            StringBuilder gram = new StringBuilder();

            for (int j = 0; j < N; j++) {
                gram.append(words[i + j]).append(" ");
            }

            result.add(gram.toString().trim());
        }

        return result;
    }

    // Analyze a document
    public void analyzeDocument(String docId) {
        List<String> targetNgrams = documentNgrams.get(docId);

        if (targetNgrams == null) {
            System.out.println("Document not found.");
            return;
        }

        // Count matches with other docs
        HashMap<String, Integer> matchCount = new HashMap<>();

        for (String gram : targetNgrams) {
            Set<String> docs = ngramIndex.get(gram);

            if (docs != null) {
                for (String otherDoc : docs) {
                    if (!otherDoc.equals(docId)) {
                        matchCount.put(otherDoc,
                                matchCount.getOrDefault(otherDoc, 0) + 1);
                    }
                }
            }
        }

        System.out.println("Analyzing: " + docId);
        System.out.println("Total n-grams: " + targetNgrams.size());

        // Calculate similarity
        for (Map.Entry<String, Integer> entry : matchCount.entrySet()) {
            String otherDoc = entry.getKey();
            int matches = entry.getValue();

            double similarity = (matches * 100.0) / targetNgrams.size();

            System.out.println("Matches with " + otherDoc + ": " + matches);
            System.out.println("Similarity: " + String.format("%.2f", similarity) + "%");

            if (similarity > 50) {
                System.out.println("⚠️ PLAGIARISM DETECTED!");
            } else if (similarity > 15) {
                System.out.println("⚠️ Suspicious content.");
            }

            System.out.println();
        }
    }

    // MAIN method
    public static void main(String[] args) {
        PlagiarismDetector detector = new PlagiarismDetector();

        String doc1 = "Artificial intelligence is transforming the world. " +
                "Machine learning enables systems to learn from data.";

        String doc2 = "Artificial intelligence is transforming the world. " +
                "Machine learning helps computers learn from data.";

        String doc3 = "Cooking recipes require fresh ingredients and proper techniques.";

        detector.addDocument("essay_001", doc1);
        detector.addDocument("essay_002", doc2);
        detector.addDocument("essay_003", doc3);

        detector.analyzeDocument("essay_001");
    }
}