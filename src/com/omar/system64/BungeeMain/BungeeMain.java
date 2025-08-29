package com.omar.system64.BungeeMain;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BungeeMain extends Plugin implements Listener {

    private GuildManager guildManager;
    private BungeeAchievementManager achievementManager;
    private LevelManager levelManager;

    @Override
    public void onEnable() {
        getLogger().info("System64 BungeeCord Plugin enabled!");
        getProxy().getPluginManager().registerListener(this, this);
        MySQL.connect();

        this.guildManager = new GuildManager();
        this.achievementManager = new BungeeAchievementManager();
        this.levelManager = new LevelManager();
    }

    @Override
    public void onDisable() {
        getLogger().info("System64 BungeeCord Plugin disabled!");
        MySQL.disconnect();
    }

    // رسالة دخول
    @EventHandler
    public void onServerConnect(ServerConnectedEvent event) {
        ProxiedPlayer player = event.getPlayer();
        sendJoinMessage(player);
    }

    // صياغة رسائل المحادثة
    @EventHandler
    public void onChat(ChatEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer)) return;
        if (event.isCommand()) return; // لا نعدل على الأوامر

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        String message = event.getMessage();
        event.setCancelled(true);

        // Rank
        RankData data = getPlayerRankData(player.getUniqueId());
        String prefix = data.prefix != null ? ChatColor.translateAlternateColorCodes('&', data.prefix) : ChatColor.GRAY + "[Player]";
        ChatColor rankColor = data.rankColor != null ? parseChatColor(data.rankColor) : ChatColor.WHITE;

        // Level, XP, Achievements
        int level = levelManager.getLevel(player.getUniqueId());
        int xp = levelManager.getXP(player.getUniqueId());
        int achievementLevel = achievementManager.getLevel(player.getUniqueId());
        int achievementXP = achievementManager.getXP(player.getUniqueId());

        // Guild
        String guildName = "None";
        ChatColor guildColor = ChatColor.WHITE;
        String playerGuildName = guildManager.getPlayerGuild(player.getUniqueId());
        if (playerGuildName != null) {
            GuildManager.GuildData guildData = guildManager.getGuildData(playerGuildName);
            if (guildData != null) {
                guildName = guildData.getPrefix() != null ? guildData.getPrefix() : guildData.getColor();
                if (guildData.getColor() != null && !guildData.getColor().isEmpty()) {
                    guildColor = parseChatColor(guildData.getColor());
                }
            }
        }

        // رسالة الشات
        TextComponent chatMessage = new TextComponent(ChatColor.translateAlternateColorCodes('&',
                prefix + " " + rankColor + player.getName() + ChatColor.WHITE + ": " + ChatColor.GRAY + message
        ));

        ComponentBuilder hover = new ComponentBuilder(prefix + " " + rankColor + player.getName())
                .append("\n" + ChatColor.GRAY + "HurrayaMC Level " + ChatColor.AQUA + level)
                .append("\n" + ChatColor.GRAY + "Achievement " + ChatColor.AQUA + achievementLevel)
                .append("\n" + ChatColor.GRAY + "Guild " + guildColor + guildName)
                .append("\n\n" + ChatColor.YELLOW + "Click to view " + rankColor + player.getName() + ChatColor.YELLOW + "'s profile");

        chatMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover.create()));
        chatMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewprofile " + player.getUniqueId()));

        getProxy().broadcast(chatMessage);
    }

    private void sendJoinMessage(ProxiedPlayer player) {
        RankData data = getPlayerRankData(player.getUniqueId());
        String prefix = data.prefix != null ? ChatColor.translateAlternateColorCodes('&', data.prefix) : ChatColor.GRAY + "[Player]";
        ChatColor rankColor = data.rankColor != null ? parseChatColor(data.rankColor) : ChatColor.WHITE;

        int level = levelManager.getLevel(player.getUniqueId());
        int xp = levelManager.getXP(player.getUniqueId());
        int achievementLevel = achievementManager.getLevel(player.getUniqueId());

        String guildName = "None";
        ChatColor guildColor = ChatColor.WHITE;
        String playerGuildName = guildManager.getPlayerGuild(player.getUniqueId());
        if (playerGuildName != null) {
            GuildManager.GuildData guildData = guildManager.getGuildData(playerGuildName);
            if (guildData != null) {
                guildName = guildData.getPrefix() != null ? guildData.getPrefix() : guildData.getColor();
                if (guildData.getColor() != null && !guildData.getColor().isEmpty()) {
                    guildColor = parseChatColor(guildData.getColor());
                }
            }
        }

        TextComponent joinMessage = new TextComponent(ChatColor.translateAlternateColorCodes('&',
                prefix + " " + rankColor + player.getName() + " &6joined the lobby!"
        ));

        ComponentBuilder hover = new ComponentBuilder(prefix + " " + rankColor + player.getName())
                .append("\n" + ChatColor.GRAY + "HurrayaMC Level " + ChatColor.AQUA + level)
                .append("\n" + ChatColor.GRAY + "Achievement " + ChatColor.AQUA + achievementLevel)
                .append("\n" + ChatColor.GRAY + "Guild " + guildColor + guildName)
                .append("\n\n" + ChatColor.YELLOW + "Click to view " + rankColor + player.getName() + ChatColor.YELLOW + "'s profile");

        joinMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover.create()));
        joinMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewprofile " + player.getUniqueId()));

        getProxy().broadcast(joinMessage);
    }

    private RankData getPlayerRankData(UUID uuid) {
        try (Connection conn = MySQL.getConnection()) {
            if (conn == null) return new RankData();
            PreparedStatement ps = conn.prepareStatement("SELECT prefix, rankcolor FROM player_ranks WHERE player_uuid=?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                RankData data = new RankData();
                data.prefix = rs.getString("prefix");
                data.rankColor = rs.getString("rankcolor");
                return data;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new RankData();
    }

    private static class RankData {
        String prefix;
        String rankColor;
    }

    private ChatColor parseChatColor(String colorCode) {
        if (colorCode == null) return ChatColor.WHITE;
        colorCode = colorCode.replace("§", "&").toUpperCase();
        switch (colorCode) {
            case "&0": return ChatColor.BLACK;
            case "&1": return ChatColor.DARK_BLUE;
            case "&2": return ChatColor.DARK_GREEN;
            case "&3": return ChatColor.DARK_AQUA;
            case "&4": return ChatColor.DARK_RED;
            case "&5": return ChatColor.DARK_PURPLE;
            case "&6": return ChatColor.GOLD;
            case "&7": return ChatColor.GRAY;
            case "&8": return ChatColor.DARK_GRAY;
            case "&9": return ChatColor.BLUE;
            case "&A": return ChatColor.GREEN;
            case "&B": return ChatColor.AQUA;
            case "&C": return ChatColor.RED;
            case "&D": return ChatColor.LIGHT_PURPLE;
            case "&E": return ChatColor.YELLOW;
            case "&F": return ChatColor.WHITE;
            default:  return ChatColor.WHITE;
        }
    }
}
