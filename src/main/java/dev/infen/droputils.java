package dev.infen;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;
import java.util.stream.Collectors;

public final class droputils extends JavaPlugin {
    private Set<UUID> spawnedFor;

    @Override
    public void onEnable() {
        // Plugin startup logic: load player dragon kill tracking and register listener
        saveDefaultConfig();
        FileConfiguration cfg = getConfig();
        spawnedFor = new HashSet<>(
            cfg.getStringList("spawned-for").stream()
               .map(UUID::fromString)
               .collect(Collectors.toSet())
        );
        getServer().getPluginManager().registerEvents(new DragonKillListener(this), this);
        getLogger().info("DropUtils enabled; loaded " + spawnedFor.size() + " players");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic: save player tracking
        getConfig().set("spawned-for", spawnedFor.stream()
            .map(UUID::toString)
            .collect(Collectors.toList())
        );
        saveConfig();
        getLogger().info("DropUtils disabled; saved " + spawnedFor.size() + " players");
    }

    public boolean hasSpawned(UUID playerId) {
        return spawnedFor.contains(playerId);
    }

    public void markSpawned(UUID playerId) {
        spawnedFor.add(playerId);
    }
}
