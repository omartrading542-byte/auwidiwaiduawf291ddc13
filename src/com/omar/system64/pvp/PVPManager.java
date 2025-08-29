package com.omar.system64.pvp;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import org.bukkit.World;
import org.bukkit.Location;

public class PVPManager implements Listener {

    private final JavaPlugin plugin;
    private final World world;
    private final Location regionCenter;
    private final double regionRadius;
    private final Location pvpPos1;
    private final Location pvpPos2;
    private final World pvpWorld;

    public PVPManager(JavaPlugin plugin, World world, Location regionCenter, double regionRadius) {
        this.plugin = plugin;
        this.world = world;
        this.regionCenter = regionCenter;
        this.regionRadius = regionRadius;
        this.pvpWorld = Bukkit.getWorld("world"); // غيّر اسم العالم إذا كان مختلف
        this.pvpPos1 = new Location(pvpWorld, 100, 60, 100);
        this.pvpPos2 = new Location(pvpWorld, 200, 80, 200);

    }

    @EventHandler
    public void onPlayerEnterRegion(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // التحقق إذا كان اللاعب في المنطقة المحددة
        if (isInRegion(player.getLocation())) {
            givePVPItems(player);
        } else {
            removePVPItems(player);
        }
    }

    // تحقق ما إذا كان اللاعب في المنطقة المحددة
    private boolean isInRegion(Location location) {
        if (location.getWorld() != world) return false;
        return location.distance(regionCenter) <= regionRadius;
    }
    public boolean isInPvpZone(Location location) {
        if (!location.getWorld().getName().equalsIgnoreCase(pvpWorld.getName())) return false;

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        int x1 = Math.min(pvpPos1.getBlockX(), pvpPos2.getBlockX());
        int y1 = Math.min(pvpPos1.getBlockY(), pvpPos2.getBlockY());
        int z1 = Math.min(pvpPos1.getBlockZ(), pvpPos2.getBlockZ());

        int x2 = Math.max(pvpPos1.getBlockX(), pvpPos2.getBlockX());
        int y2 = Math.max(pvpPos1.getBlockY(), pvpPos2.getBlockY());
        int z2 = Math.max(pvpPos1.getBlockZ(), pvpPos2.getBlockZ());

        return x >= x1 && x <= x2
                && y >= y1 && y <= y2
                && z >= z1 && z <= z2;
    }

    // إعطاء اللاعب العناصر والدروع
    private void givePVPItems(Player player) {
        PlayerInventory inventory = player.getInventory();

        // إعطاء الدروع
        inventory.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        inventory.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        inventory.setBoots(new ItemStack(Material.IRON_BOOTS));
        inventory.setHelmet(new ItemStack(Material.IRON_HELMET));

        // إعطاء السيف
        inventory.addItem(new ItemStack(Material.IRON_SWORD));
    }

    // إزالة العناصر التي حصل عليها اللاعب
    private void removePVPItems(Player player) {
        PlayerInventory inventory = player.getInventory();

        // إزالة العناصر
        inventory.setChestplate(null);
        inventory.setLeggings(null);
        inventory.setBoots(null);
        inventory.setHelmet(null);

        // إزالة السيف
        inventory.remove(Material.IRON_SWORD);
    }
}
