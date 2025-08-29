package com.omar.system64.BungeeMain;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Ranks {

    // جلب رتبة اللاعب من MySQL
    public static String getPlayerRank(UUID uuid) {
        Connection conn = MySQL.getConnection();
        if (conn == null) return "default";

        String rank = "default";
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT rank FROM player_ranks WHERE player_uuid = ?"
        )) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    rank = rs.getString("rank");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rank != null ? rank : "default";
    }

    // ضبط رتبة اللاعب في MySQL
    public static void setPlayerRank(UUID uuid, String rank) {
        Connection conn = MySQL.getConnection();
        if (conn == null) return;

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO player_ranks (player_uuid, rank) VALUES (?, ?) ON DUPLICATE KEY UPDATE rank = ?"
        )) {
            ps.setString(1, uuid.toString());
            ps.setString(2, rank);
            ps.setString(3, rank);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // إرجاع Prefix حسب الرتبة
    public static String getPrefix(String rank) {
        if (rank == null) return ChatColor.GRAY + "[Player] ";

        switch (rank.toLowerCase()) {
            case "owner":    return ChatColor.RED + "[Owner] ";
            case "admin":    return ChatColor.DARK_RED + "[Admin] ";
            case "mod":      return ChatColor.BLUE + "[Mod] ";
            case "helper":   return ChatColor.AQUA + "[Helper] ";
            case "yt":       return ChatColor.LIGHT_PURPLE + "[YT] ";
            case "king":     return ChatColor.GOLD + "[King] ";
            case "mvp++":    return ChatColor.DARK_PURPLE + "[MVP++] ";
            case "mvp+":     return ChatColor.LIGHT_PURPLE + "[MVP+] ";
            case "mvp":      return ChatColor.GOLD + "[MVP] ";
            case "vip+":     return ChatColor.GREEN + "[VIP+] ";
            case "vip":      return ChatColor.DARK_GREEN + "[VIP] ";
            default:         return ChatColor.GRAY + "[Player] ";
        }
    }

    // الاسم الملون للاعب
    public static String getColoredName(ProxiedPlayer player) {
        String rank = getPlayerRank(player.getUniqueId());
        return getPrefix(rank) + ChatColor.WHITE + player.getName();
    }
}
