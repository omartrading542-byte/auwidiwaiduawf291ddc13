package com.omar.system64.achievements;

import com.omar.system64.MySQL;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.NumberFormat;
import java.util.*;

public class AchievementManager {

    private final Plugin plugin;
    private final Map<String, Achievement> achievements = new HashMap<>();
    private Hologram hologram;
    private int updateTaskId = -1;
    private FileConfiguration achConfig;
    private File achFile;

    public AchievementManager(Plugin plugin) {
        this.plugin = plugin;
        this.achFile = new File(plugin.getDataFolder(), "ach.yml");
        if (!achFile.exists()) {
            try { achFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        this.achConfig = YamlConfiguration.loadConfiguration(achFile);
        loadAchievements();
        loadHologramFromConfig();
        createTablesIfNotExist();
    }

    private void createTablesIfNotExist() {
        Connection conn = MySQL.getConnection();
        if (conn == null) return;

        try (PreparedStatement ps1 = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS achievement_levels (" +
                        "player_uuid VARCHAR(36) PRIMARY KEY," +
                        "level INT NOT NULL DEFAULT 1," +
                        "xp INT NOT NULL DEFAULT 0)")) {
            ps1.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }

        try (PreparedStatement ps2 = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS player_achievements (" +
                        "player_uuid VARCHAR(36)," +
                        "achievement_id VARCHAR(50)," +
                        "progress INT NOT NULL DEFAULT 0," +
                        "PRIMARY KEY(player_uuid, achievement_id))")) {
            ps2.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void loadAchievements() {
        achievements.put("firstkill", new Achievement("firstkill", "Get your first kill!", 1, 10));
        achievements.put("tenkills", new Achievement("tenkills", "Kill 10 players!", 10, 25));
        achievements.put("hundredkills", new Achievement("hundredkills", "Kill 100 players!", 100, 100));
    }

    public Collection<Achievement> getAllAchievements() { return achievements.values(); }

    public boolean hasAchievement(Player player, String achievementId) {
        Achievement achievement = achievements.get(achievementId);
        if (achievement == null) return false;
        return getProgress(player, achievementId) >= achievement.getGoal();
    }

    public int getProgress(Player player, String achievementId) {
        Achievement achievement = achievements.get(achievementId);
        if (achievement == null) return 0;

        Connection conn = MySQL.getConnection();
        if (conn == null) return 0;

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT progress FROM player_achievements WHERE player_uuid=? AND achievement_id=?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, achievementId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("progress");
        } catch (SQLException e) { e.printStackTrace(); }

        return 0;
    }

    public void setAchievementProgress(Player player, String achievementId, int progress) {
        Connection conn = MySQL.getConnection();
        if (conn == null) return;

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO player_achievements (player_uuid, achievement_id, progress) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE progress=?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, achievementId);
            ps.setInt(3, progress);
            ps.setInt(4, progress);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public Map<String, Integer> getPlayerAchievements(UUID playerUUID) {
        Map<String, Integer> achievementsMap = new HashMap<>();
        Connection conn = MySQL.getConnection();
        if (conn == null) return achievementsMap;

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT achievement_id, progress FROM player_achievements WHERE player_uuid=?")) {
            ps.setString(1, playerUUID.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String id = rs.getString("achievement_id");
                int progress = rs.getInt("progress");
                if (id != null) achievementsMap.put(id, progress);
            }
        } catch (SQLException e) { e.printStackTrace(); }

        return achievementsMap;
    }

    // ================= Level / XP Methods =================
    public void addLevel(Player player, int level) {
        int currentLevel = getPlayerLevel(player);
        setPlayerLevel(player, currentLevel + level);
    }

    public void setPlayerLevel(Player player, int level) {
        achConfig.set("players." + player.getUniqueId() + ".level", level);
        saveAchConfig();
        saveLevelToMySQL(player, level);
    }

    public void setPlayerXP(Player player, int xp) {
        achConfig.set("players." + player.getUniqueId() + ".xp", xp);
        saveAchConfig();
        saveXPToMySQL(player, xp);
    }

    private void saveLevelToMySQL(Player player, int level) {
        int xp = getPlayerXP(player);
        Connection conn = MySQL.getConnection();
        if (conn == null) return;

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO achievement_levels (player_uuid, level, xp) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE level=?, xp=?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setInt(2, level);
            ps.setInt(3, xp);
            ps.setInt(4, level);
            ps.setInt(5, xp);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void saveXPToMySQL(Player player, int xp) {
        int level = getPlayerLevel(player);
        Connection conn = MySQL.getConnection();
        if (conn == null) return;

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO achievement_levels (player_uuid, level, xp) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE xp=?, level=?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setInt(2, level);
            ps.setInt(3, xp);
            ps.setInt(4, xp);
            ps.setInt(5, level);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ================= Hologram Methods =================
    public void startAutoUpdateHologram() {
        if (this.updateTaskId != -1) Bukkit.getScheduler().cancelTask(this.updateTaskId);

        this.updateTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, () -> {
            try { if (this.hologram != null) updateHologramContent(); } 
            catch (Exception e) { e.printStackTrace(); if (this.hologram != null) this.hologram.delete(); }
        }, 0L, 100L);
    }

    public void removeHologram() {
        if (this.hologram != null) { this.hologram.delete(); this.hologram = null; }
    }

    public Plugin getPlugin() { return this.plugin; }

    public void setHologramLocation(Player player, Location location) {
        if (this.hologram != null) this.hologram.delete();
        this.hologram = HologramsAPI.createHologram(this.plugin, location);
        updateHologramContent();
        player.sendMessage(ChatColor.GREEN + "Hologram location set!");
    }

    public void loadHologramFromConfig() {
        File hologramFile = new File(plugin.getDataFolder(), "phologram.yml");
        if (!hologramFile.exists()) { try { hologramFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); } return; }

        FileConfiguration hologramConfig = YamlConfiguration.loadConfiguration(hologramFile);
        if (hologramConfig.contains("hologram.location")) {
            if (this.hologram != null) this.hologram.delete();
            String worldName = hologramConfig.getString("hologram.location.world");
            double x = hologramConfig.getDouble("hologram.location.x");
            double y = hologramConfig.getDouble("hologram.location.y");
            double z = hologramConfig.getDouble("hologram.location.z");
            Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
            this.hologram = HologramsAPI.createHologram(this.plugin, location);
            updateHologramContent();
        }
    }

    public void updateHologramContent() {
        if (hologram == null) return;
        hologram.clearLines();
        hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', "&bTop Achievements"));
        hologram.appendTextLine("");

        List<Map.Entry<UUID, Integer>> topPlayers = getTopPlayers();
        int rank = 1;
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        for (Map.Entry<UUID, Integer> entry : topPlayers) {
            UUID uuid = entry.getKey();
            int level = entry.getValue();
            String name = Bukkit.getOfflinePlayer(uuid).getName();
            String line = String.format(Locale.ENGLISH, "&e%d. %s &7- %s", rank++, name, nf.format(level));
            hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', line));
            if (rank > 10) break;
        }
    }

    public List<Map.Entry<UUID, Integer>> getTopPlayers() {
        List<Map.Entry<UUID, Integer>> topPlayers = new ArrayList<>();
        if (achConfig.contains("players")) {
            for (String uuidStr : achConfig.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    int level = achConfig.getInt("players." + uuidStr + ".level", 0);
                    topPlayers.add(new AbstractMap.SimpleEntry<>(uuid, level));
                } catch (IllegalArgumentException e) { continue; }
            }
        }
        topPlayers.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        return topPlayers;
    }

    public int getPlayerLevel(Player player) { return achConfig.getInt("players." + player.getUniqueId() + ".level", 0); }
    public int getPlayerXP(Player player) { return achConfig.getInt("players." + player.getUniqueId() + ".xp", 0); }
    public void saveAchConfig() { try { achConfig.save(achFile); } catch (IOException e) { e.printStackTrace(); } }

    public static class Achievement {
        private final String id;
        private final String description;
        private final int goal;
        private final int points;
        public Achievement(String id, String description, int goal, int points) {
            this.id = id; this.description = description; this.goal = goal; this.points = points;
        }
        public String getId() { return id; }
        public String getDescription() { return description; }
        public int getGoal() { return goal; }
        public int getPoints() { return points; }
    }
}
