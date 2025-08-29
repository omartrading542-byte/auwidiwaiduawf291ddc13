package com.omar.system64;

import org.bukkit.*;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LightningZoneManager implements Listener {

    private final JavaPlugin plugin;
    private final FileConfiguration config;
    private final File file;
    private final Map<UUID, Integer> playerSelections = new HashMap<>();

    public LightningZoneManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "lightningzones.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void saveZones() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addZone(Location loc) {
        List<String> zones = config.getStringList("zones");
        if (zones.size() >= 10) return;

        zones.add(loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
        config.set("zones", zones);
        saveZones();
    }

    public List<Location> getAllZones() {
        List<Location> list = new ArrayList<>();
        List<String> zones = config.getStringList("zones");
        for (String s : zones) {
            String[] parts = s.split(",");
            World world = Bukkit.getWorld(parts[0]);
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            list.add(new Location(world, x, y, z));
        }
        return list;
    }

    public void giveSelector(Player player) {
        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Lightning Wand");
        wand.setItemMeta(meta);
        player.getInventory().addItem(wand);
        player.sendMessage(ChatColor.GREEN + "Use the wand to select a lightning point!");
        playerSelections.put(player.getUniqueId(), 1);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("lightning.area")) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (Location loc : getAllZones()) {
                    loc.getWorld().strikeLightningEffect(loc);
                }
            }, 10L); // بعد ثانيتين
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.BLAZE_ROD) return;
        if (!item.getItemMeta().hasDisplayName()) return;
        if (!item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Lightning Wand")) return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Location loc = event.getClickedBlock().getLocation();
        addZone(loc);
        player.getInventory().remove(item);
        player.sendMessage(ChatColor.YELLOW + "Lightning point set at: " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
    }
}
