package me.tizo.textfilter.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.tizo.textfilter.utils.Webhook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.view.AnvilView;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class EventListener implements Listener {
    private final Logger logger;
    private final Set<Pattern> blockedPatterns;
    private final String webhookUrl;

    public EventListener(Logger logger, Set<Pattern> blockedPatterns, String webhookUrl) {
        this.logger = logger;
        this.blockedPatterns = blockedPatterns;
        this.webhookUrl = webhookUrl;
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        if (containsBlockedWords(PlainTextComponentSerializer.plainText().serialize(event.message()), event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("Your message contains blocked words!", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (containsBlockedWords(event.getMessage(), event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("Your message contains blocked words!", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onPlayerEditBook(PlayerEditBookEvent event) {
        BookMeta newBookMeta = event.getNewBookMeta();
        String title = newBookMeta.getTitle();
        List<Component> pages = newBookMeta.pages();
        PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();

        // Check title
        if (title != null && containsBlockedWords(title, event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("Your book title contains blocked words!", NamedTextColor.RED));
            return;
        }

        // Combine all pages into a single string
        StringBuilder combinedText = new StringBuilder();
        for (Component page : pages) {
            combinedText.append(serializer.serialize(page)).append(" ");
        }

        // Check the entire book content
        if (containsBlockedWords(combinedText.toString(), event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("Your book contains blocked words!", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        StringBuilder combinedLines = new StringBuilder();
        PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();

        for (Component line : event.lines()) {
            String plainText = serializer.serialize(line);
            combinedLines.append(plainText).append(" ");
        }

        String allLines = combinedLines.toString().toLowerCase().trim();

        if (containsBlockedWords(allLines, event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("Your sign contains blocked words!", NamedTextColor.RED));
        }
    }

    // TODO: only check if the player is about to take the renamed item, this way its not checked everytime the player adds or removed letters while renaming.
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilView anvilView = event.getView(); // Directly cast

        String renameText = anvilView.getRenameText();
        if (renameText != null && containsBlockedWords(renameText, (Player) anvilView.getPlayer())) {
            event.setResult(null);
            anvilView.getPlayer().sendMessage(Component.text("The chosen name contains blocked words!", NamedTextColor.RED));
        }
    }

    private boolean containsBlockedWords(String text, Player player) {
        for (Pattern pattern : blockedPatterns) {
            if (pattern.matcher(text).find()) {

                if (webhookUrl != null && !webhookUrl.isEmpty()) {
                    new Webhook(webhookUrl).sendAlert(player, text, pattern.pattern());
                }

                return true;
            }
        }
        return false;
    }
}


