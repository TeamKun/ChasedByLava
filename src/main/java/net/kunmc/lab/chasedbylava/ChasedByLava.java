package net.kunmc.lab.chasedbylava;

import net.kunmc.lab.chasedbylava.command.CommandHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChasedByLava extends JavaPlugin {
    private static ChasedByLava INSTANCE;

    public static ChasedByLava getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        INSTANCE = this;
        Bukkit.getPluginCommand("chasedbylava").setExecutor(new CommandHandler());
        Bukkit.getPluginCommand("chasedbylava").setTabCompleter(new CommandHandler());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
