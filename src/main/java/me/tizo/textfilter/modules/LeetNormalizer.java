package me.tizo.textfilter.modules;

import java.util.*;


/* TODO: other possibilitys for later
a) Database/External Storage
For extremely large sets of bad word variants, consider storing the variants in a database or using an external storage solution. When the application starts, it can load the leetspeak variants from a storage system (like a disk, cloud, or database), ensuring that memory usage doesn't balloon.

b) Tries or Prefix Trees
Another advanced optimization is to store leetspeak variants using a trie (prefix tree). This allows you to efficiently represent and query leetspeak variants, reducing memory usage compared to storing every single combination.
 */

public class LeetNormalizer {

    private static final int MAX_COMBINATIONS_PER_WORD = 100; // Reasonable per-word limit
    private static final int MAX_TOTAL_COMBINATIONS = 1000;

    private static final Map<String, String[]> leetInputMap = new HashMap<>();
    private static final Map<String, List<Character>> leetMap = new HashMap<>();

    static {
        // Define the input leet map
        leetInputMap.put("a", new String[]{"4", "@", "/-\\", "/\\", "^", "Д"});
        leetInputMap.put("b", new String[]{"ß", "|3", "I3", "13", "!3", ")3"});
        leetInputMap.put("c", new String[]{"(", "<", "[", "¢", "©"});
        leetInputMap.put("d", new String[]{"|)", "(|", "[)", "I>", "|>", "|}", "|]"});
        leetInputMap.put("e", new String[]{"3", "€", "£", "[-", "|=-"});
        leetInputMap.put("f", new String[]{"|=", "ƒ", "/="});
        leetInputMap.put("g", new String[]{"6", "(_+"});
        leetInputMap.put("h", new String[]{"/-/", "\\-\\", "[-]", "]-[", ")-(", "(-)", ":-:", "|~|", "|-|", "]~[", "}{", "!-!", "1-1", "\\-/", "I+I"});
        leetInputMap.put("i", new String[]{"1", "|", "][", "!", "¡", "l"});
        leetInputMap.put("j", new String[]{",_|", "_|", "._|", "._]", "_]", ",_]", "]"});
        leetInputMap.put("k", new String[]{"|<", ">|", "1<", "|c"});
        leetInputMap.put("l", new String[]{"1", "|_", "i", "|"});
        leetInputMap.put("o", new String[]{"0", "ö"});
        leetInputMap.put("n", new String[]{"И"});
        leetInputMap.put("s", new String[]{"$", "5"});
        leetInputMap.put("t", new String[]{"7", "+"});
        leetInputMap.put("z", new String[]{"2"});

        // Convert input leet map to leet -> char map
        for (Map.Entry<String, String[]> entry : leetInputMap.entrySet()) {
            char normalChar = entry.getKey().charAt(0);
            for (String leetVariant : entry.getValue()) {
                leetMap.computeIfAbsent(leetVariant.toLowerCase(), k -> new ArrayList<>()).add(normalChar);
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Leet Normalizer Ready. Type 'exit' to quit.");

        while (true) {
            System.out.print("\nEnter leet text to normalize: ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Exiting...");
                break;
            }

            long startTime = System.nanoTime(); // Start timer

            Set<String> normalized = normalizeSentence(input);

            long endTime = System.nanoTime(); // End timer
            long durationInMillis = (endTime - startTime) / 1_000_000;

            System.out.println("\nNormalized sentence variants:");
            for (String sentence : normalized) {
                System.out.println(sentence);
            }

            System.out.println("Total variants: " + normalized.size());
            System.out.println("Generated in: " + durationInMillis + " ms");
        }

        scanner.close();
    }

    public static Set<String> normalizeSentence(String sentence) {
        String[] words = sentence.split("\\s+");

        List<Set<String>> normalizedWords = new ArrayList<>();

        for (String word : words) {
            if (hasRepeatingLeetChars(word, 3)) {
                normalizedWords.add(Set.of(word)); // Skip normalization for words with repeating chars
            } else {
                normalizedWords.add(normalizeWord(word));
            }
        }

        // Combine all normalized words into sentence combinations
        Set<String> result = new HashSet<>();
        combineWords(normalizedWords, 0, new ArrayList<>(), result);
        return result;
    }

    private static Set<String> normalizeWord(String word) {
        Set<String> variants = new HashSet<>();
        normalizeRecursive(word.toLowerCase(), 0, new StringBuilder(), variants);
        return variants;
    }

    private static void normalizeRecursive(String input, int index, StringBuilder current, Set<String> resultSet) {
        if (resultSet.size() >= MAX_COMBINATIONS_PER_WORD) return;
        if (index == input.length()) {
            resultSet.add(current.toString());
            return;
        }

        // Try matching longest possible substrings first
        for (int len = 1; len <= input.length() - index; len++) {
            String substring = input.substring(index, index + len);
            List<Character> replacements = leetMap.getOrDefault(substring, List.of());

            if (!replacements.isEmpty()) {
                for (char repl : replacements) {
                    current.append(repl);
                    normalizeRecursive(input, index + len, current, resultSet);
                    current.setLength(current.length() - 1); // backtrack
                }
            }
        }

        // If no match found, proceed with the current character
        char ch = input.charAt(index);
        current.append(ch);
        normalizeRecursive(input, index + 1, current, resultSet);
        current.setLength(current.length() - 1); // backtrack
    }

    private static void combineWords(List<Set<String>> wordSets, int index, List<String> current, Set<String> result) {
        if (result.size() >= MAX_TOTAL_COMBINATIONS) return;
        if (index == wordSets.size()) {
            result.add(String.join(" ", current));
            return;
        }

        for (String word : wordSets.get(index)) {
            current.add(word);
            combineWords(wordSets, index + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    private static boolean hasRepeatingLeetChars(String word, int maxAllowed) {
        Map<Character, Integer> counts = new HashMap<>();

        // Build a set of all leet-relevant characters
        Set<Character> leetRelevantChars = new HashSet<>();
        for (String[] variants : leetInputMap.values()) {
            for (String variant : variants) {
                for (char ch : variant.toCharArray()) {
                    leetRelevantChars.add(ch);
                }
            }
        }

        for (char c : word.toCharArray()) {
            if (leetRelevantChars.contains(c)) {
                counts.put(c, counts.getOrDefault(c, 0) + 1);
                if (counts.get(c) > maxAllowed) {
                    return true;
                }
            }
        }

        return false;
    }
}
