package me.tizo.textfilter.utils;

import lombok.Setter;
import me.tizo.textfilter.TextFilter;
import me.tizo.textfilter.listener.EventListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Webhook {
    @Setter
    private static String webhookUrl;

    public static void sendAlert(Player player, String message, EventListener.EventType eventType) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            return; // Don't attempt to send if no webhook is set
        }

        Bukkit.getScheduler().runTaskAsynchronously(TextFilter.getPlugin(TextFilter.class), () -> {
            try {
                URL url = new URI(webhookUrl).toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Escape the message and normalized text for safe JSON formatting
                String escapedMessage = escapeJson(message);

                String jsonPayload = "{" +
                        "\"username\": \"TextFilter\"," +
                        "\"embeds\": [{" +
                        "\"title\": \"Filtered " + eventType + " Alert\"," +
                        "\"color\": 15158332," +
                        "\"fields\": [" +
                        "{\"name\": \"Player\", \"value\": \"`" + player.getName() + "`\", \"inline\": true}," +
                        "{\"name\": \"Message\", \"value\": \"`" + escapedMessage + "`\", \"inline\": false}" +
                        "]}]" +
                        "}";

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode != 204) {
                    Bukkit.getLogger().warning("Failed to send webhook: " + responseCode);
                }
            } catch (Exception e) {
                Bukkit.getLogger().warning("Error sending webhook: " + e.getMessage());
            }
        });
    }

    // Helper method to escape special characters for JSON
    private static String escapeJson(String input) {
        if (input == null) return "";

        // Escape backslashes, quotes, and control characters
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\f", "\\f");
    }
}
