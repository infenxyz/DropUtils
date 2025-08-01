package dev.infen;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DragonKillListener implements Listener {
    private final droputils plugin;

    public DragonKillListener(droputils plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) return;
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        UUID id = killer.getUniqueId();
        if (plugin.hasSpawned(id)) return;
        // read drop location (world and coordinates) from config
        String wName = plugin.getConfig().getString("drop-location.world", event.getEntity().getWorld().getName());
        World preferred = Bukkit.getWorld(wName);
        World dropWorld = (preferred != null ? preferred : event.getEntity().getWorld());
        int x = plugin.getConfig().getInt("drop-location.x", 0);
        int y = plugin.getConfig().getInt("drop-location.y", 80);
        int z = plugin.getConfig().getInt("drop-location.z", 0);
        Location dropLoc = new Location(dropWorld, x, y, z);
        // schedule egg block placement next tick to avoid portal overwrite
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            boolean giveEgg = plugin.getConfig().getBoolean("give-egg", false);
            if (giveEgg) {
                // create custom egg item with name and lore
                ItemStack egg = new ItemStack(Material.DRAGON_EGG);
                ItemMeta meta = egg.getItemMeta();
                if (meta != null) {
                    String rawName = plugin.getConfig().getString("egg-item.name", "");
                    if (!rawName.isEmpty()) {
                        meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(rawName));
                    }
                    List<String> rawLore = plugin.getConfig().getStringList("egg-item.lore");
                    if (!rawLore.isEmpty()) {
                        List<Component> loreComps = rawLore.stream()
                            .map(s -> LegacyComponentSerializer.legacyAmpersand().deserialize(s))
                            .collect(Collectors.toList());
                        if (plugin.getConfig().getBoolean("add-player-name", false)) {
                            loreComps.add(Component.text(killer.getName()));
                        }
                        meta.lore(loreComps);
                    }
                    egg.setItemMeta(meta);
                }
                killer.getInventory().addItem(egg);
            } else {
                dropWorld.getBlockAt(dropLoc).setType(Material.DRAGON_EGG);
            }
            String raw = plugin.getConfig().getString("first-kill-message", "");
            if (raw != null && !raw.isEmpty()) {
                Component msg = LegacyComponentSerializer.legacyAmpersand().deserialize(raw);
                killer.sendMessage(msg);
            }
            plugin.getLogger().info((giveEgg ? "Gave" : "Spawned") + " dragon egg for " + killer.getName() + " at " + dropLoc);
        });
        // record that this player has triggered the egg
        plugin.markSpawned(id);
    }
}
