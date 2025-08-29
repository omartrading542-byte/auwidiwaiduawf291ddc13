package com.omar.system64.levelManager;

import com.omar.system64.Main;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class LevelPlaceholders extends PlaceholderExpansion {

    private final Main plugin;
    private final LevelManager levelManager;

    public LevelPlaceholders(Main plugin, LevelManager levelManager) {
        this.plugin = plugin;
        this.levelManager = levelManager;
    }

    @Override
    public String getIdentifier() {
        return "system64";  // هذا مهم بدون %
    }

    @Override
    public String getAuthor() {
        return "Omar";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) return "";

        if (params == null) return null;

        switch (params.toLowerCase()) {
            case "level":
                return String.valueOf(levelManager.getLevel(player.getUniqueId()));

            case "xp":
                return String.valueOf(levelManager.getXP(player.getUniqueId()));

            case "level_color_emoji":
                int level = levelManager.getLevel(player.getUniqueId());
                String code = levelManager.getLevelColorAndEmoji(level);
                return (code != null ? code : "") + level;

            case "level_color":
                return levelManager.getLevelColor(levelManager.getLevel(player.getUniqueId()));

            default:
                return null;
        }
    }
}
