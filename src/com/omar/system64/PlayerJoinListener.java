package com.omar.system64;

import com.omar.system64.coins.Coins;
import com.omar.system64.scoreboard.Board;
import com.omar.system64.levelManager.LevelManager;
import com.omar.system64.achievements.AchievementManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.PlayerInventory;

public class PlayerJoinListener implements Listener {

    private final Main plugin;
    private final Ranks ranks;
    private final LevelManager levelManager;
    private final AchievementManager achievementManager;
    private final Coins coins;  
    GuildManager guildManager = Main.getInstance().getGuildManager();
    
    public PlayerJoinListener(Main plugin, Coins coins)  {
        this.plugin = plugin;
        this.ranks = plugin.getRanks();
        this.levelManager = plugin.getLevelManager();
        this.achievementManager = plugin.getAchievementManager();
        this.coins = coins;  
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // إزالة رسالة الانضمام الافتراضية
        event.setJoinMessage(null);

        // استدعاء إعدادات الرتبة والـ Permissions
        ranks.setChatPrefix(player);
        ranks.applyPermissions(player);
        ranks.applyPermissions(player);
        ranks.applyExtraPermissions(player);
        ranks.setChatPrefix(player);

        // تحديد الرتبة للاعب
        String rank = ranks.getRank(player);
        String prefix = ranks.getPrefix(rank);
        String rankColor = ranks.getRankColor(rank);

        // تعيين رسالة الدخول حسب الصلاحيات

        // إعدادات الانتقال إلى الـ Spawn أو الـ Lobby
        boolean setSpawnOnJoin = plugin.getConfig().getBoolean("setSpawnOnJoin", true);
        boolean setLobbyOnJoin = plugin.getConfig().getBoolean("setLobbyOnJoin", false);

        if (setSpawnOnJoin) {
            teleportToSpawn(player);
        } else if (setLobbyOnJoin) {
            teleportToLobby(player);
        }

        // تفعيل الـ Scoreboard - تمرير Coins للكونستركتور
        if (player.getScoreboard() == null || player.getScoreboard() == Bukkit.getScoreboardManager().getMainScoreboard()) {
            Board board = new Board(player, levelManager, ranks, achievementManager, coins, guildManager, null);
            board.update();
        }

        // ضبط الطقس
        setDayWeather(player);

        // رسالة ترحيب للاعب
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aWelcome to the server!"));

        // إزالة الدروع تلقائيًا
        removeArmor(player);
    }


    private void teleportToSpawn(Player player) {
        if (plugin.getConfig().contains("spawn")) {
            String worldName = plugin.getConfig().getString("spawn.world");
            double x = plugin.getConfig().getDouble("spawn.x");
            double y = plugin.getConfig().getDouble("spawn.y");
            double z = plugin.getConfig().getDouble("spawn.z");
            float pitch = (float) plugin.getConfig().getDouble("spawn.pitch");
            float yaw = (float) plugin.getConfig().getDouble("spawn.yaw");

            if (Bukkit.getWorld(worldName) != null) {
                Location spawnLocation = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
                player.teleport(spawnLocation);
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThe world specified for spawn does not exist!"));
            }
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cSpawn location is not defined in the config.yml."));
        }
    }

    private void teleportToLobby(Player player) {
        if (plugin.getConfig().contains("lobby")) {
            String worldName = plugin.getConfig().getString("lobby.world");
            double x = plugin.getConfig().getDouble("lobby.x");
            double y = plugin.getConfig().getDouble("lobby.y");
            double z = plugin.getConfig().getDouble("lobby.z");
            float pitch = (float) plugin.getConfig().getDouble("lobby.pitch");
            float yaw = (float) plugin.getConfig().getDouble("lobby.yaw");

            if (Bukkit.getWorld(worldName) != null) {
                Location lobbyLocation = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
                player.teleport(lobbyLocation);
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThe world specified for lobby does not exist!"));
            }
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cLobby location is not defined in the config.yml."));
        }
    }

    private void setDayWeather(Player player) {
        player.getWorld().setTime(1000);
        player.getWorld().setStorm(false);
    }

    private void removeArmor(Player player) {
        PlayerInventory inventory = player.getInventory();
        inventory.setChestplate(null);
        inventory.setLeggings(null);
        inventory.setBoots(null);
        inventory.setHelmet(null);
    }
}
