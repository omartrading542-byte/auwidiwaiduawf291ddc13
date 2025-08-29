package com.omar.system64;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class WorldRegionManager implements Listener, CommandExecutor {

    private final JavaPlugin plugin;
    private FileConfiguration regionConfig;
    private File regionFile;

    private double x1, y1, z1, x2, y2, z2;
    private World world;

    private Location firstPoint = null;
    private Location secondPoint = null;

    public WorldRegionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadRegionFile();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void loadRegionFile() {
        regionFile = new File(plugin.getDataFolder(), "worldregion.yml");
        if (!regionFile.exists()) {
            try {
                regionFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        regionConfig = YamlConfiguration.loadConfiguration(regionFile);
        loadRegion();
    }

    private void loadRegion() {
        String worldName = regionConfig.getString("world");
        if (worldName == null) return;
        world = Bukkit.getWorld(worldName);

        x1 = regionConfig.getDouble("x1");
        y1 = regionConfig.getDouble("y1");
        z1 = regionConfig.getDouble("z1");
        x2 = regionConfig.getDouble("x2");
        y2 = regionConfig.getDouble("y2");
        z2 = regionConfig.getDouble("z2");
    }

    public void setRegion(Location loc1, Location loc2) {
        world = loc1.getWorld();
        regionConfig.set("world", world.getName());

        x1 = Math.min(loc1.getX(), loc2.getX());
        y1 = Math.min(loc1.getY(), loc2.getY());
        z1 = Math.min(loc1.getZ(), loc2.getZ());

        x2 = Math.max(loc1.getX(), loc2.getX());
        y2 = Math.max(loc1.getY(), loc2.getY());
        z2 = Math.max(loc1.getZ(), loc2.getZ());

        regionConfig.set("x1", x1);
        regionConfig.set("y1", y1);
        regionConfig.set("z1", z1);
        regionConfig.set("x2", x2);
        regionConfig.set("y2", y2);
        regionConfig.set("z2", z2);

        try {
            regionConfig.save(regionFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isInRegion(Location loc) {
        if (loc.getWorld() != world) return false;

        double px = loc.getX(), py = loc.getY(), pz = loc.getZ();
        return px >= x1 && px <= x2
                && py >= y1 && py <= y2
                && pz >= z1 && pz <= z2;
    }
    
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (world == null) return;

        ItemStack hand = player.getItemInHand();
        // تجاهل اللاعب أثناء اختيار النقاط بالفأس المخصص فقط
        if (hand != null && hand.getType() == Material.WOOD_AXE
            && hand.hasItemMeta() && "§aWorld Selector Axe".equals(hand.getItemMeta().getDisplayName()))
            return;

        if (!isInRegion(player.getLocation())) {
            Location spawn = world.getSpawnLocation();
            Location loc = player.getLocation();

            // حساب الاتجاه نحو الـ spawn
            double dx = spawn.getX() - loc.getX();
            double dz = spawn.getZ() - loc.getZ();

            // تطبيع الاتجاه ليصبح خطوة واحدة
            double length = Math.sqrt(dx*dx + dz*dz);
            if (length != 0) {
                dx = dx / length;
                dz = dz / length;
            }

            // خطوة واحدة نحو الـ spawn
            Location newLoc = loc.clone().add(dx, 0, dz);
            newLoc.setPitch(loc.getPitch());
            newLoc.setYaw(loc.getYaw());

            player.teleport(newLoc);
            player.sendMessage("§cYou are not allowed to leave this area!");
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack hand = player.getItemInHand();

        if (hand == null || hand.getType() != Material.WOOD_AXE) return;
        if (!hand.hasItemMeta() || !"§aWorld Selector Axe".equals(hand.getItemMeta().getDisplayName())) return;

        Action action = event.getAction();
        Location clickedLoc = event.getClickedBlock() != null ? event.getClickedBlock().getLocation() : player.getLocation();

        if (action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR) {
            firstPoint = clickedLoc;
            player.sendMessage("§aFirst point set at: " + formatLocation(firstPoint) + ". Now right-click for the second point.");
        } else if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
            if (firstPoint == null) {
                player.sendMessage("§cPlease select the first point with left click first!");
                return;
            }
            secondPoint = clickedLoc;
            setRegion(firstPoint, secondPoint);
            player.sendMessage("§aWorld region set successfully! Points saved in worldregion.yml");
        }
    }

    private String formatLocation(Location loc) {
        return String.format("(%d, %d, %d)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (command.getName().equalsIgnoreCase("setworld")) {
            ItemStack axe = new ItemStack(Material.WOOD_AXE);
            ItemMeta meta = axe.getItemMeta();
            meta.setDisplayName("§aWorld Selector Axe"); // فأس مميز
            axe.setItemMeta(meta);

            player.getInventory().addItem(axe);
            player.sendMessage("§aUse this wooden axe to select two points in the world! Left click = first point, Right click = second point.");
            return true;
        }
        return false;
    }
}
