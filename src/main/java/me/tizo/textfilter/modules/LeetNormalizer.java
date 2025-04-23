package me.tizo.textfilter.modules;

import me.tizo.textfilter.config.Config;

import java.util.*;

// TODO: check bigger variants first. (eg. |] before ])
/* TODO: other possibilities for later
a) Database/External Storage
For extremely large sets of bad word variants, consider storing the variants in a database or using an external storage solution. When the application starts, it can load the leetspeak variants from a storage system (like a disk, cloud, or database), ensuring that memory usage doesn't balloon.

b) Tries or Prefix Trees
Another advanced optimization is to store leetspeak variants using a trie (prefix tree). This allows you to efficiently represent and query leetspeak variants, reducing memory usage compared to storing every single combination.
 */

public class LeetNormalizer {
    private final Config config;

    public LeetNormalizer(Config config) {
        this.config = config;
    }

    private static final int MAX_COMBINATIONS_PER_WORD = 100; // Reasonable per-word limit
    private static final int MAX_TOTAL_COMBINATIONS = 1000;

    public Set<String> normalizeSentence(String sentence) {
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

    private Set<String> normalizeWord(String word) {
        Set<String> variants = new HashSet<>();
        normalizeRecursive(word.toLowerCase(), 0, new StringBuilder(), variants);
        return variants;
    }

    private void normalizeRecursive(String input, int index, StringBuilder current, Set<String> resultSet) {
        if (resultSet.size() >= MAX_COMBINATIONS_PER_WORD) return;
        if (index == input.length()) {
            resultSet.add(current.toString());
            return;
        }

        // Try matching longest possible substrings first
        for (int len = 1; len <= input.length() - index; len++) {
            String substring = input.substring(index, index + len);
            List<String> replacements = config.getLeetMap().getOrDefault(substring, List.of());

            if (!replacements.isEmpty()) {
                for (String repl : replacements) {
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

    private void combineWords(List<Set<String>> wordSets, int index, List<String> current, Set<String> result) {
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

    private boolean hasRepeatingLeetChars(String word, int maxAllowed) {
        Map<Character, Integer> counts = new HashMap<>();

        // Build a set of all leet-relevant characters
        Set<Character> leetRelevantChars = new HashSet<>();
        for (List<String> variants : config.getLeetMap().values()) {
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
