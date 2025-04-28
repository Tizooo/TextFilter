package me.tizo.textfilter.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.tizo.textfilter.config.Config;
import me.tizo.textfilter.modules.badwords.Flow;
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

public class EventListener implements Listener {
    private final Config config;
    public enum EventType {CHAT, COMMAND, BOOK, BOOK_TITLE, SIGN, ANVIL}
    private final PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();

    public EventListener(Config config) {
        this.config = config;
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        if (Flow.badwords(serializer.serialize(event.message()))) {
            //event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("Your message contains blocked words!", NamedTextColor.RED));
            Webhook.sendAlert(event.getPlayer(), serializer.serialize(event.message()), EventType.CHAT);
        }
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (Flow.badwords(event.getMessage())) {
            //event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("Your message contains blocked words!", NamedTextColor.RED));
            Webhook.sendAlert(event.getPlayer(), event.getMessage(), EventType.COMMAND);
        }
    }

    // TODO: do not append space at the end
    @EventHandler
    public void onPlayerEditBook(PlayerEditBookEvent event) {
        BookMeta newBookMeta = event.getNewBookMeta();
        String title = newBookMeta.getTitle();
        List<Component> pages = newBookMeta.pages();

        // Check title
        if (title != null && Flow.badwords(title)) {
            //event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("Your book title contains blocked words!", NamedTextColor.RED));
            Webhook.sendAlert(event.getPlayer(), title, EventType.BOOK_TITLE);
            return;
        }

        // Combine all pages into a single string
        StringBuilder combinedText = new StringBuilder();
        for (Component page : pages) {
            combinedText.append(serializer.serialize(page)).append(" ");
        }

        // Check the entire book content
        if (Flow.badwords(combinedText.toString())) {
            //event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("Your book contains blocked words!", NamedTextColor.RED));
            Webhook.sendAlert(event.getPlayer(), combinedText.toString(), EventType.BOOK);
        }
    }

    // TODO: do not append space at the end
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        StringBuilder combinedLines = new StringBuilder();

        for (Component line : event.lines()) {
            String plainText = serializer.serialize(line);
            combinedLines.append(plainText).append(" ");
        }

        String allLines = combinedLines.toString().toLowerCase().trim();

        if (Flow.badwords(allLines)) {
            //event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("Your sign contains blocked words!", NamedTextColor.RED));
            Webhook.sendAlert(event.getPlayer(), allLines, EventType.SIGN);
        }
    }

    // TODO: only check if the player is about to take the renamed item, this way its not checked everytime the player adds or removed letters while renaming.
    // TODO: use inventory click event and check in which inventory it was performed.
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilView anvilView = event.getView();
        String renameText = anvilView.getRenameText();

        if (renameText != null && Flow.badwords(renameText)) {
            //event.setResult(null);
            anvilView.getPlayer().sendMessage(Component.text("The chosen name contains blocked words!", NamedTextColor.RED));
            Webhook.sendAlert( (Player) anvilView.getPlayer(), renameText, EventType.ANVIL);
        }
    }
}