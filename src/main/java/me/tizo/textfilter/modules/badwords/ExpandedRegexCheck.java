package me.tizo.textfilter.modules.badwords;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class ExpandedRegexCheck {
    private static final Set<Pattern> regexPatterns = new HashSet<>();
    private static final String GITHUB_RAW_URL = "https://raw.githubusercontent.com/Tizooo/Regex-filter/main/badwords";

    static {
        try {
            URI uri = new URI(GITHUB_RAW_URL);
            URL url = uri.toURL();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;

                    System.out.println("Original regex: " + line);

                    String expandedRegex = expandRegex(line);

                    System.out.println("Expanded regex: " + expandedRegex);

                    regexPatterns.add(Pattern.compile(expandedRegex, Pattern.CASE_INSENSITIVE));
                }

            }
        } catch (Exception e) {
            System.err.println("Failed to load regex patterns: " + e.getMessage());
        }
    }

    private static String expandRegex(String input) {
        StringBuilder result = new StringBuilder();

        for (char ch : input.toCharArray()) {
            switch (ch) {
                case 'i': case 'l':
                    result.append("[il1!|]+");
                    break;
                case 'c':
                    result.append("[c<({\\[]+");
                    break;
                default:
                    result.append(ch);
            }
        }

        return result.toString();
    }

    public static boolean regexCheck(String text) {
        for (Pattern pattern : regexPatterns) {
            if (pattern.matcher(text).find()) {
                return true;
            }
        }
        return false;
    }
}
