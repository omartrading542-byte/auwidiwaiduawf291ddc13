package com.omar.system64;

import com.omar.system64.levelManager.LevelManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class RankPlaceholder extends PlaceholderExpansion {
    private final Main plugin;
    private final Ranks ranks;
    private final GuildManager guildManager;
    private final LevelManager levelManager;

    public RankPlaceholder(Main plugin, Ranks ranks, GuildManager guildManager, LevelManager levelManager) {
        this.plugin = plugin;
        this.ranks = ranks;
        this.guildManager = guildManager;
        this.levelManager = levelManager;
    }

    @Override
    public String getIdentifier() {
        return "system64";
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
    public String onRequest(OfflinePlayer player, String identifier) {
        if (player == null) return null;

        // الرتبة الخاصة باللاعب
        if (identifier.equalsIgnoreCase("rank")) {
            return this.ranks.getPlayerRank(player.getUniqueId());
        }

        // لون الرتبة
        if (identifier.equalsIgnoreCase("color")) {
            String rank = this.ranks.getPlayerRank(player.getUniqueId());
            return this.ranks.getRankColor(rank);
        }

        // الـ prefix للرتبة
        if (identifier.equalsIgnoreCase("prefix")) {
            String rank = this.ranks.getPlayerRank(player.getUniqueId());
            return this.ranks.getPrefix(rank);
        }

        // جماعة - prefix
        if (identifier.equalsIgnoreCase("guild_prefix")) {
            return this.guildManager.getGuildPrefix(player.getUniqueId());
        }

        // جماعة - suffix
        if (identifier.equalsIgnoreCase("guild_suffix")) {
            return this.guildManager.getGuildSuffix(player.getUniqueId());
        }

        // جماعة - chat color
        if (identifier.equalsIgnoreCase("guild_chatcolor")) {
            return this.guildManager.getGuildColor(player.getUniqueId());
        }

        // جماعة - level
        if (identifier.equalsIgnoreCase("guild_level")) {
            for (String guildName : this.guildManager.getGuildsConfig().getConfigurationSection("guilds").getKeys(false)) {
                if (this.guildManager.getGuildMembers(guildName).contains(player.getUniqueId().toString())) {
                    return String.valueOf(this.guildManager.getGuildLevel(guildName));
                }
            }
        }

        // جماعة - xp
        if (identifier.equalsIgnoreCase("guild_xp")) {
            for (String guildName : this.guildManager.getGuildsConfig().getConfigurationSection("guilds").getKeys(false)) {
                if (this.guildManager.getGuildMembers(guildName).contains(player.getUniqueId().toString())) {
                    return String.valueOf(this.guildManager.getGuildXP(guildName));
                }
            }
        }

        // دمج اللون + إيموجي + رقم المستوى في placeholder واحد system64_level
        if (identifier.equalsIgnoreCase("level")) {
            int level = this.levelManager.getLevel(player.getUniqueId());
            String color = this.levelManager.getLevelColor(level);
            String emoji = this.levelManager.getLevelColorAndEmoji(level);

            // النتيجة مثل: &a✦ 15
            return color + emoji + "" + level;
        }
        if (identifier.equalsIgnoreCase("colorr")) {
            int level = this.levelManager.getLevel(player.getUniqueId());
            String color = this.levelManager.getLevelColor(level);
            String emoji = this.levelManager.getLevelColorAndEmoji(level);

            // النتيجة مثل: &a✦ 15
            return color;
        }

        return null;
    }
}