package me.tizo.textfilter.commands;

import me.tizo.textfilter.Textfilter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {
    private final Textfilter plugin;

    public ReloadCommand(Textfilter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("textfilter")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("textfilter.reload")) {  // Ensure the player has permission
                    plugin.reloadConfig();  // Reload the config file
                    plugin.loadConfig();  // Reload the blocked words (both from config and GitHub)
                    sender.sendMessage("§aTextfilter config reloaded successfully!");
                } else {
                    sender.sendMessage("§cYou don't have permission to execute this command.");
                }
                return true;
            }
        }
        return false;
    }
}