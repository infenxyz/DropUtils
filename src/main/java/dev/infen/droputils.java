package dev.infen;

import org.bukkit.plugin.java.JavaPlugin;

public final class droputils extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new DragonKillListener(this), this);
        getLogger().info("DropUtils enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("DropUtils disabled");
    }
}
