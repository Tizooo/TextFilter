package me.tizo.textfilter.modules.badwords;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Scanner;
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

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);

            // Handle escape sequences like \b, \s
            if (ch == '\\' && i + 1 < input.length()) {
                result.append(ch).append(input.charAt(i + 1));
                i++; // Skip next char
                continue;
            }

            // Expand specific letters to characters that look like them
            if (ch == 'i' || ch == 'l') {
                result.append("[il1!|]");
            } else if (ch == 'c') {
                result.append("[c<({\\[]");
            } else {
                result.append(ch);
            }

            if (Character.isLetter(ch)) {
                if (i + 1 < input.length()) { // Ensure we're not going out of bounds
                    char nextChar = input.charAt(i + 1);
                    if (nextChar == '+' || nextChar == '*') {
                        result.append(nextChar).append("(_|\\W|\\s)*");
                        i++;
                    } else {
                        result.append("+(_|\\W|\\s)*");
                    }
                } else {
                    result.append("+(_|\\W|\\s)*"); // Handle the last character
                }
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

    // For quick code testing
    public static void main(String[] args) {
        // Create a scanner to read input from the terminal
        Scanner scanner = new Scanner(System.in);

        // Infinite loop to keep the program running
        while (true) {
            // Ask for user input
            System.out.print("Enter text to check (or 'exit' to quit): ");
            String inputText = scanner.nextLine();

            // Check if the user wants to exit
            if (inputText.equalsIgnoreCase("exit")) {
                System.out.println("Exiting program...");
                break; // Exit the loop
            }

            // Perform the regex check
            boolean containsBadWords = regexCheck(inputText);

            // Output the result
            if (containsBadWords) {
                System.out.println("The input contains bad words.");
            } else {
                System.out.println("The input does not contain bad words.");
            }
        }

        // Close the scanner resource
        scanner.close();
    }
}
