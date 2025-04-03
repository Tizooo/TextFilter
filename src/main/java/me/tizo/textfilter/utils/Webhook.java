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

    public static void sendAlert(Player player, String message, String triggeredFilter, EventListener.EventType eventType) {
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

                String jsonPayload = "{" +
                        "\"username\": \"TextFilter\"," +
                        "\"embeds\": [{" +
                        "\"title\": \"Filtered " + eventType + " Alert\"," +
                        "\"color\": 15158332," +
                        "\"fields\": [" +
                        "{\"name\": \"Player\", \"value\": \"`" + player.getName() + "`\", \"inline\": true}," +
                        "{\"name\": \"Message\", \"value\": \"`" + message + "`\", \"inline\": false}," +
                        "{\"name\": \"Triggered Filter\", \"value\": \"`" + triggeredFilter + "`\", \"inline\": true}" +
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
}
