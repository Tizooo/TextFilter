package me.tizo.textfilter.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.tizo.textfilter.config.Config;
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
import java.util.regex.Pattern;

public class EventListener implements Listener {
    private final Config config;
    private final PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();

    public EventListener(Config config) {
        this.config = config;
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        if (containsBlockedWords(serializer.serialize(event.message()), event.getPlayer())) {
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
    // TODO: use inventory click event and check in which inventory it was performed.
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
        for (Pattern pattern : config.getBlockedPatterns()) {
            if (pattern.matcher(text).find()) {
                Webhook.sendAlert(player, text, pattern.pattern());
                return true;
            }
        }
        return false;
    }
}


