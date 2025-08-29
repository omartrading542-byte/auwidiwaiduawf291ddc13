package com.omar.system64;

import com.omar.system64.levelManager.LevelManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {

    private final Ranks ranks;
    private final LevelManager levelManager;
    private final GuildChatListener guildChatListener;

    public PlayerChatListener(Main plugin) {
        this.ranks = plugin.getRanks();
        this.levelManager = plugin.getLevelManager();
        this.guildChatListener = plugin.getGuildChatListener();
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // إذا toggle مفعل للـguild → تجاهل هذه الرسالة في PlayerChatListener
        if (Main.getInstance().getGuildChatListener().isGuildChat(player.getUniqueId())) {
            return;
        }

        String rank = ranks.getRank(player);
        String prefix = ranks.getPrefix(rank);
        String rankColor = ranks.getRankColor(rank);
        int level = levelManager.getLevel(player.getUniqueId());
        String levelColorEmojiCode = levelManager.getLevelColorAndEmoji(level);

        String colorCode = "&f";
        String emoji = "❓";
        if (levelColorEmojiCode.length() >= 3) {
            colorCode = levelColorEmojiCode.substring(0, 2);
            emoji = levelColorEmojiCode.substring(2);
        }

        String levelString = ChatColor.translateAlternateColorCodes('&', colorCode)
                           + "[" + emoji + level + "] " + ChatColor.RESET;

        String formattedPrefix = ChatColor.translateAlternateColorCodes('&', prefix);
        String formattedRankColor = ChatColor.translateAlternateColorCodes('&', rankColor);

        String format = levelString + formattedPrefix + " " + formattedRankColor + player.getName()
                        + ChatColor.GRAY + ": " + ChatColor.WHITE + event.getMessage();

        event.setCancelled(true);

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendMessage(format);
        }
    }
}
