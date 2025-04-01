package me.tizo.textfilter;

import me.tizo.textfilter.commands.ReloadCommand;
import me.tizo.textfilter.listener.EventListener;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class Textfilter extends JavaPlugin {

    public Set<Pattern> blockedPatterns = new HashSet<>();
    public String webhookUrl;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getCommand("textfilter").setExecutor(new ReloadCommand(this));

        loadConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void loadConfig() {
        reloadConfig();

        blockedPatterns.clear();

        getConfig().getStringList("blocked-words").forEach(word ->
                blockedPatterns.add(Pattern.compile(word, Pattern.CASE_INSENSITIVE))
        );

        // Fetch and add blocked words from GitHub repositories
        List<String> githubRepos = getConfig().getStringList("github");
        githubRepos.forEach(repoUrl -> {
            try {
                Set<String> wordsFromRepo = fetchWordsFromGitHub(repoUrl);
                wordsFromRepo.forEach(word ->
                        blockedPatterns.add(Pattern.compile(word, Pattern.CASE_INSENSITIVE))
                );
            } catch (Exception e) {
                getLogger().warning("Failed to fetch blocked words from GitHub repository: " + repoUrl);
            }
        });

        webhookUrl = getConfig().getString("webhook-url", "");

        HandlerList.unregisterAll(this);
        getServer().getPluginManager().registerEvents(new EventListener(getLogger(), blockedPatterns, webhookUrl), this);
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
