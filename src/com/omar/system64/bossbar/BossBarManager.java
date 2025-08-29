package com.omar.system64.bossbar;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Location;

import java.io.File;
import java.util.*;

public class BossBarManager {

    private final HashMap<UUID, EntityWither> bars = new HashMap<>();
    private final Plugin plugin;
    private List<String> messages = new ArrayList<>();
    private final Map<UUID, Integer> messageIndex = new HashMap<>();

    public BossBarManager(Plugin plugin) {
        this.plugin = plugin;
        loadMessages();

        // تحديث الرسالة والموقع كل ثانية
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateBossBarMessage(player);
                    updateBossBarLocation(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 4L); // كل 1 ثانية (20 ticks)
    }

    // تحميل الرسائل من ملف bossbar.yml
    private void loadMessages() {
        File file = new File(plugin.getDataFolder(), "bossbar.yml");
        if (!file.exists()) {
            plugin.saveResource("bossbar.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        messages = config.getStringList("messages");
        if (messages.isEmpty()) {
            messages.add("&aWelcome to the Server!");
        }
    }

    public void showPermanentBossBar(Player player, String text, float healthPercent) {
        removeBossBar(player);

        WorldServer world = ((CraftWorld) player.getWorld()).getHandle();
        EntityWither wither = new EntityWither(world);
        Location loc = player.getLocation().add(player.getLocation().getDirection().multiply(20));

        wither.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
        wither.setCustomName(ChatColor.translateAlternateColorCodes('&', text));
        wither.setCustomNameVisible(true);
        wither.setInvisible(true);
        wither.setHealth(healthPercent * wither.getMaxHealth());

        PacketPlayOutSpawnEntityLiving spawnPacket = new PacketPlayOutSpawnEntityLiving(wither);
        sendPacket(player, spawnPacket);

        bars.put(player.getUniqueId(), wither);
        messageIndex.put(player.getUniqueId(), 0);
    }

    public void updateBossBarMessage(Player player) {
        EntityWither wither = bars.get(player.getUniqueId());
        if (wither == null || messages.isEmpty()) return;

        int index = messageIndex.getOrDefault(player.getUniqueId(), 0);
        index = (index + 1) % messages.size();

        String message = ChatColor.translateAlternateColorCodes('&', messages.get(index));
        wither.setCustomName(message);

        PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(wither.getId(), wither.getDataWatcher(), true);
        sendPacket(player, metadataPacket);

        messageIndex.put(player.getUniqueId(), index);
    }

    public void updateBossBarLocation(Player player) {
        EntityWither wither = bars.get(player.getUniqueId());
        if (wither == null) return;

        Location loc = player.getLocation().add(player.getLocation().getDirection().normalize().multiply(20));
        wither.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);

        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(wither);
        sendPacket(player, teleportPacket);
    }

    public void removeBossBar(Player player) {
        EntityWither wither = bars.remove(player.getUniqueId());
        if (wither != null) {
            PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(wither.getId());
            sendPacket(player, destroyPacket);
        }
        messageIndex.remove(player.getUniqueId());
    }

    private void sendPacket(Player player, Packet<?> packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}
