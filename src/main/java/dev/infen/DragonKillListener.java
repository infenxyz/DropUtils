package dev.infen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import net.kyori.adventure.text.format.TextDecoration;
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
        // permission-based first-kill check
        if (killer.hasPermission("droputils.dragonegg")) return;

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
                    List<Component> loreComps = new ArrayList<>();
                    // add custom lore lines
                    for (String line : rawLore) {
                        Component comp = LegacyComponentSerializer.legacyAmpersand()
                            .deserialize(line)
                            .decoration(TextDecoration.ITALIC, false);
                        loreComps.add(comp);
                    }
                    // optionally add formatted player line
                    if (plugin.getConfig().getBoolean("add-player-name", false)) {
                        String template = plugin.getConfig().getString("egg-item.player-lore", "");
                        if (template != null && !template.trim().isEmpty()) {
                            String replaced = template.replace("{playername}", killer.getName());
                            Component comp = LegacyComponentSerializer.legacyAmpersand()
                                .deserialize(replaced)
                                .decoration(TextDecoration.ITALIC, false);
                            loreComps.add(comp);
                        }
                    }
                    if (!loreComps.isEmpty()) {
                        meta.lore(loreComps);
                    }
                    egg.setItemMeta(meta);
                }
                // try to add to inventory; if full, drop at head
                Map<Integer, ItemStack> leftover = killer.getInventory().addItem(egg);
                if (!leftover.isEmpty()) {
                    // drop remaining items at player head location
                    Location headLoc = killer.getEyeLocation();
                    for (ItemStack drop : leftover.values()) {
                        killer.getWorld().dropItem(headLoc, drop);
                    }
                }
            } else {
                dropWorld.getBlockAt(dropLoc).setType(Material.DRAGON_EGG);
            }
            String raw = plugin.getConfig().getString("first-kill-message", "");
            if (raw != null && !raw.isEmpty()) {
                Component msg = LegacyComponentSerializer.legacyAmpersand().deserialize(raw);
                killer.sendMessage(msg);
            }
            plugin.getLogger().info((giveEgg ? "Gave" : "Spawned") + " dragon egg for " + killer.getName() + " at " + dropLoc);
            // execute configured permission command (console)
            String template = plugin.getConfig().getString("permission-command", "");
            if (!template.isEmpty()) {
                String cmd = template.replace("{player}", killer.getName());
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd);
            }
        });
    }
}
