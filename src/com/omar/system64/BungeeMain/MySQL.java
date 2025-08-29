package com.omar.system64.BungeeMain;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL {

    private static Connection connection;

    // نسخة connect بدون معاملات، تستخدم مجلد plugin تلقائيًا
    public static void connect() {
        File dataFolder = new File("plugins/System64");
        connect(dataFolder);
    }

    // النسخة الأصلية مع ملف
    public static void connect(File dataFolder) {
        try {
            File configFile = new File(dataFolder, "MySQL.yml");
            if (!configFile.exists()) {
                ProxyServer.getInstance().getConsole().sendMessage(color("&c[MySQL] &fConfig file not found! Creating default one..."));
                createDefaultConfig(configFile);
            }

            Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);

            boolean enabled = config.getBoolean("enable", true);
            if (!enabled) {
                ProxyServer.getInstance().getConsole().sendMessage(color("&e[MySQL] &fMySQL is disabled in config. Running in offline mode."));
                return;
            }

            String host = config.getString("host", "localhost");
            int port = config.getInt("port", 3306);
            String database = config.getString("database", "gg");
            String user = config.getString("username", "root");
            String password = config.getString("password", "");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
            connection = DriverManager.getConnection(url, user, password);

            // إنشاء جدول أساسي للاعبين + الرتبة
            String createTableQuery = "CREATE TABLE IF NOT EXISTS player_ranks (" +
                    "player_uuid VARCHAR(36) PRIMARY KEY, " +
                    "rankprefix VARCHAR(50), " +
                    "rankcolor VARCHAR(20))";

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createTableQuery);
            }

            ProxyServer.getInstance().getConsole().sendMessage(color("&a[MySQL] &fConnected successfully!"));

        } catch (SQLException | IOException e) {
            ProxyServer.getInstance().getConsole().sendMessage(color("&c[MySQL] &fFailed to connect to the database! Plugin will run in offline mode."));
            e.printStackTrace();
        }
    }

    public static boolean isConnected() {
        try {
            return (connection != null && !connection.isClosed());
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void disconnect() {
        if (isConnected()) {
            try {
                connection.close();
                ProxyServer.getInstance().getConsole().sendMessage(color("&e[MySQL] &fConnection closed."));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // إعادة الاتصال تلقائيًا إذا كان مغلقًا أو null
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect(); // إعادة الاتصال
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    private static void createDefaultConfig(File file) {
        try {
            file.getParentFile().mkdirs();
            Configuration config = new Configuration();
            config.set("enable", true);
            config.set("host", "localhost");
            config.set("port", 3306);
            config.set("database", "gg");
            config.set("username", "root");
            config.set("password", "");
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
