package com.omar.system64.pvp;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class MapPVPCommand implements CommandExecutor, Listener {

    private final JavaPlugin plugin;
    private final Set<Player> playersInRegion = new HashSet<>();
    private final File pvpFile;
    private FileConfiguration pvpConfig;

    private Location point1 = null;
    private Location point2 = null;

    public MapPVPCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.pvpFile = new File(plugin.getDataFolder(), "pvp.yml");
        if (!pvpFile.exists()) {
            try {
                plugin.saveResource("pvp.yml", false);
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("pvp.yml not found in jar, creating blank.");
                try {
                    pvpFile.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        this.pvpConfig = YamlConfiguration.loadConfiguration(pvpFile);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return false;
        }

        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta meta = stick.getItemMeta();
        meta.setDisplayName("§aPVP Selector Stick"); // عصا مميزة
        stick.setItemMeta(meta);

        player.getInventory().addItem(stick);
        player.sendMessage(ChatColor.GREEN + "You received the PVP selector stick! Right-click blocks to set two points.");
        return true;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.STICK) return;
        if (!item.hasItemMeta() || !"§aPVP Selector Stick".equals(item.getItemMeta().getDisplayName())) return;
        if (event.getClickedBlock() == null) return;

        Player player = event.getPlayer();
        Location clicked = event.getClickedBlock().getLocation();

        if (point1 == null) {
            point1 = clicked;
            player.sendMessage(ChatColor.YELLOW + "Point 1 set at: " + formatLocation(clicked));
        } else if (point2 == null) {
            point2 = clicked;
            player.sendMessage(ChatColor.YELLOW + "Point 2 set at: " + formatLocation(clicked));
            saveRegion(player.getWorld(), point1, point2);
            point1 = null;
            point2 = null;
            player.getInventory().remove(item); // إزالة العصا بعد الاستخدام
        }
    }

    private void saveRegion(World world, Location p1, Location p2) {
        List<Map<String, Object>> regions = (List<Map<String, Object>>) pvpConfig.getList("pvp_areas");
        if (regions == null) regions = new ArrayList<>();

        Map<String, Object> region = new HashMap<>();
        region.put("world", world.getName());
        region.put("x1", p1.getX());
        region.put("y1", p1.getY());
        region.put("z1", p1.getZ());
        region.put("x2", p2.getX());
        region.put("y2", p2.getY());
        region.put("z2", p2.getZ());

        regions.add(region);
        pvpConfig.set("pvp_areas", regions);
        savePVPConfig();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();

        if (isInAnyRegion(loc)) {
            if (!playersInRegion.contains(player)) {
                playersInRegion.add(player);
                givePlayerItems(player);
                player.setAllowFlight(false);
                player.setFlying(false);
            }
        } else {
            if (playersInRegion.contains(player)) {
                playersInRegion.remove(player);
                removePlayerItems(player);
                player.sendMessage(ChatColor.RED + "You left the PVP area.");

                if (player.hasPermission("autofly")) {
                    player.setAllowFlight(true);
                }
            }
        }
    }

    private boolean isInAnyRegion(Location loc) {
        List<?> areas = pvpConfig.getList("pvp_areas");
        if (areas == null) return false;

        for (Object obj : areas) {
            if (!(obj instanceof Map)) continue;

            Map<?, ?> region = (Map<?, ?>) obj;

            String world = (String) region.get("world");
            if (!loc.getWorld().getName().equals(world)) continue;

            double x1 = toDouble(region.get("x1"));
            double y1 = toDouble(region.get("y1"));
            double z1 = toDouble(region.get("z1"));
            double x2 = toDouble(region.get("x2"));
            double y2 = toDouble(region.get("y2"));
            double z2 = toDouble(region.get("z2"));

            double minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
            double minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
            double minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);

            double px = loc.getX(), py = loc.getY(), pz = loc.getZ();

            if (px >= minX && px <= maxX &&
                    py >= minY && py <= maxY &&
                    pz >= minZ && pz <= maxZ) {
                return true;
            }
        }

        return false;
    }

    private void givePlayerItems(Player player) {
        player.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD));
        player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
    }

    private void removePlayerItems(Player player) {
        player.getInventory().remove(Material.DIAMOND_SWORD);
        player.getInventory().setArmorContents(null);
    }

    private double toDouble(Object obj) {
        if (obj instanceof Integer) return (double) (Integer) obj;
        if (obj instanceof Double) return (Double) obj;
        return 0.0;
    }

    private void savePVPConfig() {
        try {
            pvpConfig.save(pvpFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatLocation(Location loc) {
        return String.format("(%d, %d, %d)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (isInAnyRegion(player.getLocation())) {
            String msg = event.getMessage().toLowerCase();
            if (msg.equals("/fly") || msg.startsWith("/fly ")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot use /fly in the PVP area.");
            }
        }
    }
}
