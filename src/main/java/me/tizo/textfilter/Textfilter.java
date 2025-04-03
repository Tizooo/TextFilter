package me.tizo.textfilter;

import me.tizo.textfilter.commands.ReloadCommand;
import me.tizo.textfilter.config.Config;
import me.tizo.textfilter.listener.EventListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Textfilter extends JavaPlugin {

    private Config config;

    @Override
    public void onEnable() {
        config = new Config(this);

        getCommand("textfilter").setExecutor(new ReloadCommand(config));
        getServer().getPluginManager().registerEvents(new EventListener(config), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
