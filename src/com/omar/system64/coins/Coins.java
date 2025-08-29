package com.omar.system64.coins;

import com.omar.system64.MySQL;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Coins {

    private final JavaPlugin plugin;
    private FileConfiguration coinsConfig;
    private File coinsFile;

    public Coins(JavaPlugin plugin) {
        this.plugin = plugin;
        loadCoinsFile();
        createMySQLTablesIfNotExists();
    }

    private void loadCoinsFile() {
        coinsFile = new File(plugin.getDataFolder(), "coins.yml");

        if (!coinsFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                coinsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        coinsConfig = YamlConfiguration.loadConfiguration(coinsFile);
    }

    private void saveCoinsFile() {
        try {
            coinsConfig.save(coinsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createMySQLTablesIfNotExists() {
        Connection conn = MySQL.getConnection();
        if (conn == null) return;

        String coinsTable = "CREATE TABLE IF NOT EXISTS player_coins (" +
                "player_uuid VARCHAR(36) PRIMARY KEY, " +
                "coins BIGINT NOT NULL)";

        String transactionsTable = "CREATE TABLE IF NOT EXISTS coin_transactions (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "player_uuid VARCHAR(36), " +
                "action VARCHAR(50), " +
                "amount BIGINT, " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(coinsTable);
            stmt.execute(transactionsTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // تحويل العدد إلى صيغة مختصرة (1k, 10k, 1m, 1b)
    public static String formatCoins(long amount) {
        if (amount >= 1_000_000_000) {
            return (amount / 1_000_000_000) + "b";
        } else if (amount >= 1_000_000) {
            return (amount / 1_000_000) + "m";
        } else if (amount >= 1_000) {
            return (amount / 1_000) + "k";
        } else {
            return String.valueOf(amount);
        }
    }

    // تسجيل المعاملة في MySQL و YAML مع استخدام الصيغة المختصرة
    public void logTransaction(UUID playerUUID, String action, long amount) {
        // سجل في MySQL
        Connection conn = MySQL.getConnection();
        if (conn != null) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO coin_transactions (player_uuid, action, amount) VALUES (?, ?, ?)")) {
                ps.setString(1, playerUUID.toString());
                ps.setString(2, action);
                ps.setLong(3, amount);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // سجل في YAML لسهولة العرض مع تنسيق المبلغ
        String path = "players." + playerUUID.toString() + ".transactions";
        List<String> logs = coinsConfig.getStringList(path);
        logs.add("[" + java.time.LocalDateTime.now() + "] " + action + ": " + formatCoins(amount) + " coins");
        coinsConfig.set(path, logs);
        saveCoinsFile();
    }

    public long getCoins(UUID playerUUID) {
        Connection conn = MySQL.getConnection();
        if (conn != null) {
            try {
                PreparedStatement ps = conn.prepareStatement("SELECT coins FROM player_coins WHERE player_uuid = ?");
                ps.setString(1, playerUUID.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    long coins = rs.getLong("coins");
                    rs.close();
                    ps.close();
                    return coins;
                }
                rs.close();
                ps.close();

                setCoins(playerUUID, 0);
                return 0;

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return coinsConfig.getLong("players." + playerUUID.toString() + ".coins", 0);
    }

    public void setCoins(UUID playerUUID, long amount) {
        Connection conn = MySQL.getConnection();
        if (conn != null) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO player_coins (player_uuid, coins) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE coins = ?")) {
                ps.setString(1, playerUUID.toString());
                ps.setLong(2, amount);
                ps.setLong(3, amount);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        coinsConfig.set("players." + playerUUID.toString() + ".coins", amount);
        saveCoinsFile();
    }

    public void addCoins(UUID playerUUID, long amount) {
        long current = getCoins(playerUUID);
        setCoins(playerUUID, current + amount);
        logTransaction(playerUUID, "Added", amount);
    }

    public boolean removeCoins(UUID playerUUID, long amount) {
        long current = getCoins(playerUUID);
        if (current < amount) return false;
        setCoins(playerUUID, current - amount);
        logTransaction(playerUUID, "Removed", amount);
        return true;
    }

    public FileConfiguration getCoinsConfig() {
        return coinsConfig;
    }

    public void initializePlayer(UUID playerUUID) {
        if (!coinsConfig.contains("players." + playerUUID.toString())) {
            setCoins(playerUUID, 0);
        }
    }

    // دالة لقراءة آخر n معاملات من MySQL مع تنسيق المبلغ
    public List<String> getRecentTransactions(UUID playerUUID, int limit) {
        List<String> transactions = new ArrayList<>();
        Connection conn = MySQL.getConnection();
        if (conn != null) {
            String sql = "SELECT action, amount, timestamp FROM coin_transactions WHERE player_uuid = ? ORDER BY timestamp DESC LIMIT ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, playerUUID.toString());
                ps.setInt(2, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String action = rs.getString("action");
                        long amount = rs.getLong("amount");
                        Timestamp timestamp = rs.getTimestamp("timestamp");
                        transactions.add("[" + timestamp.toLocalDateTime() + "] " + action + ": " + formatCoins(amount) + " coins");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (transactions.isEmpty()) {
            transactions.add("No transactions found.");
        }
        return transactions;
    }
}
