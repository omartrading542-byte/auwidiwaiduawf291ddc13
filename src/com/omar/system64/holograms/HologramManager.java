package com.omar.system64.holograms;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.omar.system64.Main;
import com.omar.system64.Ranks;
import com.omar.system64.levelManager.LevelManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class HologramManager {
    private final Main plugin;
    private final LevelManager levelManager;
    private Hologram topLevelHologram;
    private int updateTaskId = -1;

    public HologramManager(Main plugin, LevelManager levelManager) {
        this.plugin = plugin;
        this.levelManager = levelManager;
        loadHologramFromConfig();
    }

    public void updateTopLevelHologram(Location location, Map<String, Integer> topPlayers) {
        if (this.topLevelHologram == null || this.topLevelHologram.isDeleted()) {
            this.topLevelHologram = HologramsAPI.createHologram(this.plugin, location);
        } else {
            try {
                this.topLevelHologram.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.topLevelHologram = HologramsAPI.createHologram(this.plugin, location);
        }

        this.topLevelHologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', "&bTop Levels"));
        this.topLevelHologram.appendTextLine(""); // مسافة بين العنوان والقائمة

        Ranks ranks = plugin.getRanks();
        if (ranks == null) {
            return;
        }

        int rank = 1;
        for (Map.Entry<String, Integer> entry : topPlayers.entrySet()) {
            String uuidString = entry.getKey();

            String name = uuidString;
            String rankColor = ChatColor.GRAY.toString();
            String guildPrefix = "";

            try {
                UUID uuid = UUID.fromString(uuidString);
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                if (offlinePlayer.getName() != null) {
                    name = offlinePlayer.getName();
                    String playerRank = ranks.getPlayerRank(uuid);
                    rankColor = ranks.getRankColor(playerRank);
                }

                String guildName = getGuildName(uuid);
                if (guildName != null) {
                    FileConfiguration guildsConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "guilds.yml"));
                    guildPrefix = ChatColor.translateAlternateColorCodes('&', guildsConfig.getString("guilds." + guildName + ".prefix", ""));
                }
            } catch (IllegalArgumentException e) {
                // UUID غير صالح
            }

            // هنا نجيب المستوى من MySQL عبر levelManager
            UUID playerUUID = UUID.fromString(uuidString);
            int levelFromMySQL = levelManager.getLevelFromMySQL(playerUUID); // لازم تكون هذه الطريقة موجودة في LevelManager

            String levelColoredEmoji = levelManager.getLevelColorAndEmoji(levelFromMySQL);
            String levelText = ChatColor.translateAlternateColorCodes('&', levelColoredEmoji);

            String line = String.format(Locale.ENGLISH, "&e%d. %s%s %s &7- %s %d",
                    rank++, rankColor, name, guildPrefix, levelText, levelFromMySQL);

            this.topLevelHologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', line));
            if (rank > 10) break;
        }

        saveHologramLocation(location);
    }

    public Map<String, Integer> getTopLevels() {
        // هنا عدلنا لإحضار البيانات من MySQL عبر LevelManager بدل ملف levels.yml
        Map<String, Integer> topLevels = levelManager.getTopLevelsFromMySQL(10); // لازم تضيف هذه الطريقة في LevelManager
        return topLevels;
    }

    public void updateTopLevelHologram(Location location) {
        Map<String, Integer> topPlayers = getTopLevels();
        updateTopLevelHologram(location, topPlayers);
    }

    public void removeHologram() {
        if (this.topLevelHologram != null) {
            try {
                this.topLevelHologram.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            removeHologramFromConfig();
        }
    }

    public void startAutoUpdate() {
        if (this.updateTaskId != -1) {
            Bukkit.getScheduler().cancelTask(this.updateTaskId);
        }

        this.updateTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, () -> {
            try {
                if (this.topLevelHologram != null) {
                    updateTopLevelHologram(this.topLevelHologram.getLocation());
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (this.topLevelHologram != null) {
                    this.topLevelHologram.delete();
                }
            }
        }, 0L, 150L);
    }

    public void stopAutoUpdate() {
        if (this.updateTaskId != -1) {
            Bukkit.getScheduler().cancelTask(this.updateTaskId);
            this.updateTaskId = -1;
        }
    }

    public void recreateHologramOnStartup() {
        loadHologramFromConfig();
        if (this.topLevelHologram != null) {
            updateTopLevelHologram(this.topLevelHologram.getLocation());
        }
    }

    public Hologram getTopLevelHologram() {
        return this.topLevelHologram;
    }

    private void saveHologramLocation(Location location) {
        File hologramFile = new File(plugin.getDataFolder(), "hologram.yml");
        FileConfiguration hologramConfig = YamlConfiguration.loadConfiguration(hologramFile);
        hologramConfig.set("hologram.location.world", location.getWorld().getName());
        hologramConfig.set("hologram.location.x", location.getX());
        hologramConfig.set("hologram.location.y", location.getY());
        hologramConfig.set("hologram.location.z", location.getZ());
        try {
            hologramConfig.save(hologramFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadHologramFromConfig() {
        File hologramFile = new File(plugin.getDataFolder(), "hologram.yml");
        if (!hologramFile.exists()) {
            try {
                hologramFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        FileConfiguration hologramConfig = YamlConfiguration.loadConfiguration(hologramFile);
        if (hologramConfig.contains("hologram.location")) {
            String worldName = hologramConfig.getString("hologram.location.world");
            double x = hologramConfig.getDouble("hologram.location.x");
            double y = hologramConfig.getDouble("hologram.location.y");
            double z = hologramConfig.getDouble("hologram.location.z");
            Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
            updateTopLevelHologram(location);
        }
    }

    private void removeHologramFromConfig() {
        File hologramFile = new File(plugin.getDataFolder(), "hologram.yml");
        FileConfiguration hologramConfig = YamlConfiguration.loadConfiguration(hologramFile);
        hologramConfig.set("hologram.location", null);
        try {
            hologramConfig.save(hologramFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getGuildName(UUID uuid) {
        File guildsFile = new File(plugin.getDataFolder(), "guilds.yml");
        FileConfiguration guildsConfig = YamlConfiguration.loadConfiguration(guildsFile);
        if (guildsConfig == null) return null;

        ConfigurationSection guildsSection = guildsConfig.getConfigurationSection("guilds");
        if (guildsSection == null) return null;

        for (String guildName : guildsSection.getKeys(false)) {
            String ownerUUID = guildsConfig.getString("guilds." + guildName + ".owner");
            if (ownerUUID != null && ownerUUID.equals(uuid.toString())) {
                return guildName;
            }
        }

        return null;
    }
}
