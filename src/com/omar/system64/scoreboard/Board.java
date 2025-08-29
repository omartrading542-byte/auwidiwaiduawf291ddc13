package com.omar.system64.scoreboard;

import com.omar.system64.Main;
import com.omar.system64.Ranks;
import com.omar.system64.achievements.AchievementManager;
import com.omar.system64.coins.Coins;
import com.omar.system64.levelManager.LevelManager;
import com.omar.system64.GuildManager;
import com.omar.system64.GiftSystem;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.*;

import com.omar.system64.bungee.BungeeListener;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class Board {

    private final Player player;
    private final ScoreboardManager manager;
    private final Scoreboard board;
    private final Objective objective;
    private final AchievementManager achievementManager;
    private final List<String> animatedTitles;
    private final int animationInterval;
    private int animationIndex = 0;
    private final Plugin plugin;
    private final LevelManager levelManager;
    private final Ranks ranks;
    private final Coins coins;  
    private final GuildManager guildManager; 
    private final GiftSystem giftSystem;

    public Board(Player player, LevelManager levelManager, Ranks ranks, AchievementManager achievementManager, Coins coins, GuildManager guildManager, GiftSystem giftSystem) {
        this.player = player;
        this.levelManager = levelManager;
        this.ranks = ranks;
        this.achievementManager = achievementManager;
        this.coins = coins;
        this.guildManager = guildManager;
        this.giftSystem = giftSystem;
        this.manager = Bukkit.getScoreboardManager();
        this.plugin = Main.getInstance();

        if (player.getScoreboard() == null || player.getScoreboard() == manager.getMainScoreboard()) {
            this.board = manager.getNewScoreboard();
        } else {
            this.board = player.getScoreboard();
        }

        this.objective = board.getObjective("system64") != null ?
                board.getObjective("system64") :
                board.registerNewObjective("system64", "dummy");

        this.animatedTitles = ScoreboardConfig.getTitleFrames();
        this.animationInterval = ScoreboardConfig.getTitleAnimationInterval();

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        updateBoard();
        update();
        startBoardUpdater();
        startTitleAnimation();
    }

    private void startBoardUpdater() {
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), this::reloadScoreboard, 0L, 20L);
    }

    private void reloadScoreboard() {
        updateBoard();
        update();
    }

    private void startTitleAnimation() {
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
            if (!animatedTitles.isEmpty()) {
                String animatedTitle = animatedTitles.get(animationIndex);
                objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', animatedTitle));
                animationIndex = (animationIndex + 1) % animatedTitles.size();
            }
        }, 0L, animationInterval);
    }

    public Board updateBoard() {
        List<String> lines = ScoreboardConfig.getLines();
        int score = lines.size();
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
        String currentTime = getCurrentTime();

        int achievementsCount = achievementManager.getPlayerAchievements(player.getUniqueId()) != null ?
                achievementManager.getPlayerAchievements(player.getUniqueId()).size() : 0;

        File achFile = new File(plugin.getDataFolder(), "ach.yml");
        FileConfiguration achConfig = YamlConfiguration.loadConfiguration(achFile);
        String playerUUID = player.getUniqueId().toString();
        int achievementLevel = achConfig.contains("players." + playerUUID + ".level") ?
                achConfig.getInt("players." + playerUUID + ".level") : 0;

        // قراءة totalGifts من gift.yml
        int totalGifts = 0;
        File giftFile = new File(plugin.getDataFolder(), "gift.yml");
        if (giftFile.exists()) {
            YamlConfiguration giftConfig = YamlConfiguration.loadConfiguration(giftFile);
            totalGifts = giftConfig.getKeys(false).size(); // عدد المفاتيح = عدد الهدايا
        }

        int index = 0;
        for (String raw : lines) {
            int level = levelManager.getLevel(player.getUniqueId());
            int xp = levelManager.getXP(player.getUniqueId());
            String rank = ranks.getRank(player);
            String prefix = ranks.getPrefix(rank);
            String rankColor = ranks.getRankColor(rank);
            String suffix = ranks.getSuffix(rank);

            String levelColorEmojiCode = levelManager.getLevelColorAndEmoji(level);
            String colorCode = "&f", emoji = "❓";

            if (levelColorEmojiCode.length() >= 3) {
                colorCode = levelColorEmojiCode.substring(0, 2);
                emoji = levelColorEmojiCode.substring(2);
            }

            String levelWithEmoji = ChatColor.translateAlternateColorCodes('&', colorCode) + emoji + numberFormat.format(level) + ChatColor.RESET;

            String replaced = raw
                    .replace("{level}", levelWithEmoji)
                    .replace("{xp}", numberFormat.format(xp))
                    .replace("{health}", String.valueOf((int) player.getHealth()))
                    .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                    .replace("{BUNGEECOUNT}", numberFormat.format(BungeeListener.bungeeCount))
                    .replace("{time}", currentTime)
                    .replace("{rank}", suffix)
                    .replace("{rankcolor}", rankColor)
                    .replace("{achievements}", numberFormat.format(achievementsCount))
                    .replace("{achievementlevel}", numberFormat.format(achievementLevel))
                    .replace("{data}", getCurrentDate());

            // Guild placeholders
            if (guildManager != null) {
                String guildPrefix = guildManager.getGuildPrefix(player.getUniqueId());
                String guildSuffix = guildManager.getGuildSuffix(player.getUniqueId());
                if (guildPrefix == null || guildPrefix.isEmpty()) guildPrefix = ChatColor.RED + "No Guild";
                if (guildSuffix == null || guildSuffix.isEmpty()) guildSuffix = ChatColor.RED + "No Guild";
                replaced = replaced.replace("{guildprefix}", guildPrefix);
                replaced = replaced.replace("{guildsuffix}", guildSuffix);
            } else {
                replaced = replaced.replace("{guildprefix}", ChatColor.RED + "No Guild");
                replaced = replaced.replace("{guildsuffix}", ChatColor.RED + "No Guild");
            }

            // Coins placeholder
            if (coins != null) {
                replaced = replaced.replace("{coins}", numberFormat.format(coins.getCoins(player.getUniqueId())));
            } else {
                replaced = replaced.replace("{coins}", "0");
            }

            // Gift placeholders
            int claimedGifts = 0;
            if (giftSystem != null) {
                claimedGifts = giftSystem.getPlayerClaimedGifts(player.getUniqueId());
            } else {
                // fallback: اقرأ من players.yml
                File playersFile = new File(plugin.getDataFolder(), "players.yml");
                if (playersFile.exists()) {
                    YamlConfiguration playersConfig = YamlConfiguration.loadConfiguration(playersFile);
                    if (playersConfig.contains(playerUUID)) {
                        Map<String, Object> map = playersConfig.getConfigurationSection(playerUUID).getValues(false);
                        claimedGifts = map.size();
                    }
                }
            }
            replaced = replaced.replace("{gift}", String.valueOf(claimedGifts));
            replaced = replaced.replace("{totalGifts}", String.valueOf(totalGifts));

            replaced = PlaceholderAPI.setPlaceholders(player, replaced);
            setLine("line" + index, ChatColor.translateAlternateColorCodes('&', replaced), score--);
            index++;
        }

        return this;
    }

    private void setLine(String key, String text, int score) {
        Team team = board.getTeam(key);
        if (team == null) {
            team = board.registerNewTeam(key);
            String entry = ChatColor.values()[score].toString();
            team.addEntry(entry);
            objective.getScore(entry).setScore(score);
        }

        if (text.length() <= 16) {
            team.setPrefix(text);
            team.setSuffix("");
        } else if (text.length() <= 32) {
            String prefix = text.substring(0, 16);
            String suffix = text.substring(16);
            String lastColorCode = ChatColor.getLastColors(prefix);
            suffix = lastColorCode + suffix;
            team.setPrefix(prefix);
            team.setSuffix(suffix);
        } else {
            String prefix = text.substring(0, 16);
            String suffix = text.substring(16, 32);
            String lastColorCode = ChatColor.getLastColors(prefix);
            suffix = lastColorCode + suffix;
            team.setPrefix(prefix);
            team.setSuffix(suffix);
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss", Locale.US);
        String time = sdfTime.format(new Date());
        String timeColor = ScoreboardConfig.getTimeColor();
        return ChatColor.translateAlternateColorCodes('&', timeColor) + time;
    }

    private String getCurrentDate() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yy", Locale.US);
        String date = sdfDate.format(new Date());
        String dateColor = ScoreboardConfig.getDateColor();
        return ChatColor.translateAlternateColorCodes('&', dateColor) + date;
    }

    public void update() {
        player.setScoreboard(board);
    }
}
