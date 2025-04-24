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
                    regexPatterns.add(Pattern.compile(line, Pattern.CASE_INSENSITIVE));
                }

            }
        } catch (Exception e) {
            System.err.println("Failed to load regex patterns: " + e.getMessage());
        }
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
