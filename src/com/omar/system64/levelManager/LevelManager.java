package com.omar.system64.levelManager;

import com.omar.system64.GuildManager;
import com.omar.system64.Main;
import com.omar.system64.Ranks;
import com.omar.system64.achievements.AchievementManager;
import com.omar.system64.scoreboard.Board;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class LevelManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration data;
    private File configFile;
    private File dataFile;
    private final Ranks ranks;
    private FileConfiguration levelsConfig;
    private File levelsFile;
    GuildManager guildManager = Main.getInstance().getGuildManager();

    public LevelManager(JavaPlugin plugin, Ranks ranks) {
        this.plugin = plugin;
        this.ranks = ranks;
        loadConfig();
        loadData();
        createMySQLTableIfNotExists();
    }

    public LevelManager(JavaPlugin plugin) {
        this(plugin, null);
    }

    public JavaPlugin getPlugin() {
        return this.plugin;
    }

    public void loadConfig() {
        this.configFile = new File(this.plugin.getDataFolder(), "levelconfig.yml");
        if (!this.configFile.exists())
            this.plugin.saveResource("levelconfig.yml", false);
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
    }

    public void loadData() {
        this.dataFile = new File(this.plugin.getDataFolder(), "levels.yml");
        if (!this.dataFile.exists())
            try {
                this.dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        this.data = YamlConfiguration.loadConfiguration(this.dataFile);
    }

    public void loadLevelsFile() {
        levelsFile = new File(plugin.getDataFolder(), "levels.yml");
        if (!levelsFile.exists()) {
            plugin.saveResource("levels.yml", false);
        }
        levelsConfig = YamlConfiguration.loadConfiguration(levelsFile);
    }

    public void saveData() {
        try {
            this.data.save(this.dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // إنشاء جدول MySQL إذا لم يكن موجود
    private void createMySQLTableIfNotExists() {
        Connection conn = com.omar.system64.MySQL.getConnection();
        if (conn == null) return;

        String sql = "CREATE TABLE IF NOT EXISTS player_levels (" +
                "player_uuid VARCHAR(36) PRIMARY KEY, " +
                "level INT NOT NULL, " +
                "xp INT NOT NULL)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<UUID> getAllPlayers() {
        List<UUID> players = new ArrayList<>();
        if (this.data.getConfigurationSection("players") == null) return players;
        Set<String> playerKeys = this.data.getConfigurationSection("players").getKeys(false);
        for (String playerKey : playerKeys)
            players.add(UUID.fromString(playerKey));
        return players;
    }

    public boolean isEnabled() {
        return this.config.getBoolean("enabled", true);
    }

    public void reloadLevelsConfig() {
        File file = new File(plugin.getDataFolder(), "levels.yml");
        this.levelsConfig = YamlConfiguration.loadConfiguration(file);
    }

    public long getXPNeededForLevel(int level) {
        // (نفس طريقة حساب XP كما في كودك الأصلي)
        if (level <= 10) {
            return (long) level * 1000;
        } else if (level <= 20) {
            return (long) level * 1000 * 10;
        } else if (level <= 30) {
            return (long) level * 1000 * 100;
        } else if (level <= 40) {
            return (long) level * 1000 * 1000;
        } else if (level <= 50) {
            return (long) level * 1000 * 10000;
        } else if (level <= 60) {
            return (long) level * 1000 * 100000;
        } else if (level <= 70) {
            return (long) level * 1000 * 1000000;
        } else if (level <= 80) {
            return (long) level * 1000 * 10000000;
        } else if (level <= 90) {
            return (long) level * 1000 * 100000000;
        } else if (level <= 100) {
            return (long) level * 1000 * 1000000000;
        } else if (level <= 110) {
            return (long) level * 1000 * 10000000000L;
        } else if (level <= 120) {
            return (long) level * 1000 * 100000000000L;
        } else if (level <= 130) {
            return (long) level * 1000 * 1000000000000L;
        } else if (level <= 140) {
            return (long) level * 1000 * 10000000000000L;
        } else if (level <= 150) {
            return (long) level * 1000 * 100000000000000L;
        } else {
            return (long) level * 1000 * 100000000000000L;
        }
    }

    // ** جلب مستوى لاعب من MySQL **
    public int getLevelFromMySQL(UUID uuid) {
        Connection conn = com.omar.system64.MySQL.getConnection();
        if (conn != null) {
            try {
                PreparedStatement ps = conn.prepareStatement("SELECT level FROM player_levels WHERE player_uuid = ?");
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int level = rs.getInt("level");
                    rs.close();
                    ps.close();
                    return level;
                }
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        // إذا لم يوجد في MySQL يرجع من ملف كنسخة احتياطية
        return this.data.getInt("players." + uuid + ".level", 1);
    }

    // ** جلب أفضل اللاعبين حسب المستوى من MySQL **
    public Map<String, Integer> getTopLevelsFromMySQL(int limit) {
        Map<String, Integer> topPlayers = new LinkedHashMap<>();
        Connection conn = com.omar.system64.MySQL.getConnection();
        if (conn != null) {
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT player_uuid, level FROM player_levels ORDER BY level DESC LIMIT ?");
                ps.setInt(1, limit);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String uuid = rs.getString("player_uuid");
                    int level = rs.getInt("level");
                    topPlayers.put(uuid, level);
                }
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return topPlayers;
    }

    public int getLevel(UUID uuid) {
        return getLevelFromMySQL(uuid);
    }

    public int getXP(UUID uuid) {
        Connection conn = com.omar.system64.MySQL.getConnection();
        if (conn != null) {
            try {
                PreparedStatement ps = conn.prepareStatement("SELECT xp FROM player_levels WHERE player_uuid = ?");
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int xp = rs.getInt("xp");
                    rs.close();
                    ps.close();
                    return xp;
                }
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return this.data.getInt("players." + uuid + ".xp", 0);
    }

    public String parsePlaceholders(Player player, String text) {
        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI") && player != null && text != null) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    public void saveLevelAndXP(UUID playerUUID, int level, int xp) {
        Connection conn = com.omar.system64.MySQL.getConnection();
        boolean savedInMySQL = false;

        if (conn != null) {
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO player_levels (player_uuid, level, xp) VALUES (?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE level = ?, xp = ?");
                ps.setString(1, playerUUID.toString());
                ps.setInt(2, level);
                ps.setInt(3, xp);
                ps.setInt(4, level);
                ps.setInt(5, xp);

                ps.executeUpdate();
                ps.close();
                savedInMySQL = true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (!savedInMySQL) {
            this.data.set("players." + playerUUID + ".level", level);
            this.data.set("players." + playerUUID + ".xp", xp);
            saveData();
        }
    }

    public void setLevel(UUID uuid, int level) {
        saveLevelAndXP(uuid, level, getXP(uuid));
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && Main.getInstance().getScoreboardConfig().getBoolean("enabled")) {
            AchievementManager achievementManager = Main.getInstance().getAchievementManager();
            new Board(player, this, this.ranks, achievementManager, null, guildManager, null).updateBoard();
        }
    }

    public void setXP(UUID uuid, int xp) {
        saveLevelAndXP(uuid, getLevel(uuid), xp);
        checkLevelUp(uuid);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            if (Main.getInstance().getScoreboardConfig().getBoolean("enabled")) {
                AchievementManager achievementManager = Main.getInstance().getAchievementManager();
                new Board(player, this, this.ranks, achievementManager, null, guildManager, null).updateBoard();
            }
            float percentage = xp / (float) getXPNeededForLevel(getLevel(uuid));
            player.setExp(percentage);
        }
    }

    public void setXP(String playerName, int xp) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            Bukkit.getLogger().warning("[system64] Player '" + playerName + "' not found or offline.");
            return;
        }
        setXP(player.getUniqueId(), xp);
    }

    public void addXP(UUID uuid, int amount) {
        setXP(uuid, getXP(uuid) + amount);
    }

    public void addXP(String playerName, int amount) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            Bukkit.getLogger().warning("[system64] Player '" + playerName + "' not found or offline.");
            return;
        }
        addXP(player.getUniqueId(), amount);
    }

    public void addLevel(UUID uuid, int amount) {
        setLevel(uuid, getLevel(uuid) + amount);
    }

    public void addLevel(String playerName, int amount) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            Bukkit.getLogger().warning("[system64] Player '" + playerName + "' not found or offline.");
            return;
        }
        addLevel(player.getUniqueId(), amount);
    }

    public void checkLevelUp(UUID uuid) {
        long xp = getXP(uuid);
        int level = getLevel(uuid);

        long neededXP = getXPNeededForLevel(level);

        while (xp >= neededXP) {
            xp -= neededXP;
            level++;
            neededXP = getXPNeededForLevel(level);
        }

        saveLevelAndXP(uuid, level, (int) xp);
    }

    public Map<UUID, Integer> getTopLevels(int topN) {
        if (this.data == null) {
            Bukkit.getLogger().warning("Data not loaded properly.");
            return new LinkedHashMap<>();
        }

        Map<String, Object> playersData = this.data.getConfigurationSection("players").getValues(false);
        List<Map.Entry<UUID, Integer>> sortedList = new ArrayList<>();
        for (String key : playersData.keySet()) {
            int level = this.data.getInt("players." + key + ".level", 1);
            sortedList.add(new AbstractMap.SimpleEntry<>(UUID.fromString(key), level));
        }
        sortedList.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        Map<UUID, Integer> topPlayers = new LinkedHashMap<>();
        int count = 0;
        for (Map.Entry<UUID, Integer> entry : sortedList) {
            if (count++ >= topN) break;
            topPlayers.put(entry.getKey(), entry.getValue());
        }
        return topPlayers;
    }

    public FileConfiguration getLevelsConfig() {
        return levelsConfig;
    }

    // لون + إيموجي حسب مستوى اللاعب مع & بدل ChatColor
    public String getLevelColorAndEmoji(int level) {
        if (level < 1) level = 1;

        int range = (level - 1) / 10;

        switch (range) {
            case 0:  return "&7★";   // 1-10
            case 1:  return "&a✦";   // 11-20
            case 2:  return "&6✔";   // 21-30
            case 3:  return "&c✪";   // 31-40
            case 4:  return "&5❖";   // 41-50
            case 5:  return "&b✿";   // 51-60
            case 6:  return "&2✸";   // 61-70
            case 7:  return "&4✹";   // 71-80
            case 8:  return "&3❂";   // 81-90
            case 9:  return "&d❁";   // 91-100
            case 10: return "&7❃";   // 101-110
            case 11: return "&8❈";   // 111-120
            case 12: return "&f✰";   // 121-130
            case 13: return "&6✯";   // 131-140
            case 14: return "&c✺";   // 141-150
            case 15: return "&4☯";   // 151-155
            default: return "&f?";   // فوق 155 أو غير معروف
        }
    }

    // لون فقط حسب المستوى مع & بدل ChatColor
    public String getLevelColor(int level) {
        if (level >= 1 && level <= 10) {
            return "&7";
        } else if (level >= 11 && level <= 20) {
            return "&a";
        } else if (level >= 21 && level <= 30) {
            return "&6";
        } else if (level >= 31 && level <= 40) {
            return "&c";
        } else if (level >= 41 && level <= 50) {
            return "&5";
        } else if (level >= 51 && level <= 60) {
            return "&b";
        } else if (level >= 61 && level <= 70) {
            return "&2";
        } else if (level >= 71 && level <= 80) {
            return "&4";
        } else if (level >= 81 && level <= 90) {
            return "&3";
        } else if (level >= 91 && level <= 100) {
            return "&d";
        } else if (level >= 101 && level <= 110) {
            return "&7";
        } else if (level >= 111 && level <= 120) {
            return "&8";
        } else if (level >= 121 && level <= 130) {
            return "&f";
        } else if (level >= 131 && level <= 140) {
            return "&6";
        } else if (level >= 141 && level <= 150) {
            return "&c";
        } else if (level >= 151 && level <= 155) {
            return "&4";
        }
        return "&f";
    }
}
