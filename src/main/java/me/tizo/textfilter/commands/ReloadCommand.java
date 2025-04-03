package me.tizo.textfilter.commands;

import me.tizo.textfilter.config.Config;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {
    private final Config config;

    public ReloadCommand(Config config) {
        this.config = config;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("textfilter"))
            return false;

        if (!(args.length == 1 && args[0].equalsIgnoreCase("reload")))
            return false;

        // Ensure the player has permission
        if (!sender.hasPermission("textfilter.reload")) {
            sender.sendMessage("§cYou don't have permission to execute this command.");
            return false;
        }

        // Reload the blocked words (both from config and GitHub)
        config.reloadConfigAsync();
        sender.sendMessage("§aTextfilter config reloaded successfully!");
        return true;
    }
}