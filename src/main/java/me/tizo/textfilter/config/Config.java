package me.tizo.textfilter.config;

import lombok.Getter;
import me.tizo.textfilter.utils.Webhook;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Getter
public class Config {
    private final Plugin plugin;
    private FileConfiguration config;
    private final Set<Pattern> blockedPatterns = new HashSet<>();
    private String webhookUrl;

    public Config(Plugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
        reloadConfigAsync();
    }

    // TODO: reloadConfigAsync & loadConfig zusammenfügen?
    public void reloadConfigAsync() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::loadConfig);
    }

    private void loadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        blockedPatterns.clear();

        config.getStringList("blocked-words").forEach(word ->
                blockedPatterns.add(Pattern.compile(word, Pattern.CASE_INSENSITIVE))
        );

        // Fetch and add blocked words from GitHub repositories
        List<String> githubRepos = config.getStringList("github");
        githubRepos.forEach(repoUrl -> {
            try {
                Set<String> wordsFromRepo = fetchWordsFromGitHub(repoUrl);
                wordsFromRepo.forEach(word ->
                        blockedPatterns.add(Pattern.compile(word, Pattern.CASE_INSENSITIVE))
                );
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to fetch blocked words from GitHub repository: " + repoUrl);
            }
        });

        webhookUrl = config.getString("webhook-url", "");
        Webhook.setWebhookUrl(webhookUrl);
    }

    // Method to fetch words from a GitHub raw URL
    public Set<String> fetchWordsFromGitHub(String repoUrl) throws Exception {
        Set<String> words = new HashSet<>();

        URI uri = new URI(repoUrl);
        URL url = uri.toURL();

        // Open connection and read the content
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip empty lines or lines starting with '#'
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                words.add(line); // Add each valid word from the file
            }
        }
        return words;
    }
}
