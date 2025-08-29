package com.omar.system64;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL {

    private static Connection connection;

    public static void connect(File dataFolder) {
        try {
            File configFile = new File(dataFolder, "MySQL.yml");
            if (!configFile.exists()) {
                Bukkit.getConsoleSender().sendMessage(color("&c[MySQL] &fConfig file not found! Creating default one..."));
                createDefaultConfig(configFile);
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

            boolean enabled = config.getBoolean("enable", true);
            if (!enabled) {
                Bukkit.getConsoleSender().sendMessage(color("&e[MySQL] &fMySQL is disabled in config. Running in offline mode."));
                return;
            }

            String host = config.getString("host", "localhost");
            int port = config.getInt("port", 3306);
            String database = config.getString("database", "gg");
            String user = config.getString("username", "root");
            String password = config.getString("password", "");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
            connection = DriverManager.getConnection(url, user, password);

            String createTableQuery = "CREATE TABLE IF NOT EXISTS player_ranks (" +
                    "player_uuid VARCHAR(36) PRIMARY KEY, " +
                    "rank VARCHAR(50))";

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createTableQuery);
            }

            Bukkit.getConsoleSender().sendMessage(color("&a[MySQL] &fConnected successfully!"));

        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(color("&c[MySQL] &fFailed to connect to the database! Plugin will run in offline mode."));
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    private static void createDefaultConfig(File file) {
        try {
            file.getParentFile().mkdirs();
            YamlConfiguration config = new YamlConfiguration();
            config.set("enable", true);
            config.set("host", "localhost");
            config.set("port", 3306);
            config.set("database", "gg");
            config.set("username", "root");
            config.set("password", "");
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
