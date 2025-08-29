package com.omar.system64;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import com.omar.system64.levelManager.LevelManager;

import java.util.*;

public class GuildChatListener implements Listener {

    private final GuildManager guildManager;
    private final Ranks ranks;
    private final LevelManager levelManager;

    private final Map<UUID, Boolean> guildChatToggle = new HashMap<>();

    public GuildChatListener(GuildManager guildManager, Ranks ranks, LevelManager levelManager) {
        this.guildManager = guildManager;
        this.ranks = ranks;
        this.levelManager = levelManager;
    }

    public boolean toggleGuildChat(UUID uuid) {
        boolean newValue = !guildChatToggle.getOrDefault(uuid, false);
        guildChatToggle.put(uuid, newValue);
        return newValue;
    }

    public boolean isGuildChat(UUID uuid) {
        return guildChatToggle.getOrDefault(uuid, false);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        UUID senderUUID = sender.getUniqueId();

        // تجاهل إذا toggle مش مفعل
        if (!isGuildChat(senderUUID)) return;

        String guildName = guildManager.getPlayerGuild(senderUUID);

        if (guildName == null || guildName.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "❌ You are not in a guild!");
            return; // لا نلغي الرسالة للعالم كله
        }

        event.setCancelled(true); // إلغاء ظهور الرسالة للعالم كله فقط إذا هو عضو بالجيلد

        List<String> members = guildManager.getGuildMembers(guildName);
        if (members == null || members.isEmpty()) return;

        String rank = ranks.getRank(sender);
        String prefix = ranks.getPrefix(rank);
        String rankColor = ranks.getRankColor(rank);

        int level = levelManager.getLevel(senderUUID);
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
        String color = guildManager.getGuildColor(senderUUID);
        
        String message = ChatColor.translateAlternateColorCodes('&',
                color + "[" + guildName + "] " + levelString + formattedPrefix + " " 
                + formattedRankColor + sender.getName() + "&7: &f" + event.getMessage());

        for (String memberUUID : members) {
            Player member = Bukkit.getPlayer(UUID.fromString(memberUUID));
            if (member != null && member.isOnline()) {
                member.sendMessage(message);
            }
        }
    }
}
