package com.omar.system64.BungeeMain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public class LevelManager {

    // جلب مستوى اللاعب من MySQL
    public int getLevel(UUID playerUUID) {
        try (Connection conn = MySQL.getConnection()) {
            if (conn == null) return 0;
            PreparedStatement ps = conn.prepareStatement("SELECT level FROM player_levels WHERE player_uuid=?");
            ps.setString(1, playerUUID.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("level");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // جلب XP اللاعب من MySQL
    public int getXP(UUID playerUUID) {
        try (Connection conn = MySQL.getConnection()) {
            if (conn == null) return 0;
            PreparedStatement ps = conn.prepareStatement("SELECT xp FROM player_levels WHERE player_uuid=?");
            ps.setString(1, playerUUID.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("xp");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // تحديث مستوى اللاعب
    public void setLevel(UUID playerUUID, int level) {
        try (Connection conn = MySQL.getConnection()) {
            if (conn == null) return;
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO player_levels (player_uuid, level) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE level=?"
            );
            ps.setString(1, playerUUID.toString());
            ps.setInt(2, level);
            ps.setInt(3, level);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // تحديث XP اللاعب
    public void setXP(UUID playerUUID, int xp) {
        try (Connection conn = MySQL.getConnection()) {
            if (conn == null) return;
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO player_levels (player_uuid, xp) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE xp=?"
            );
            ps.setString(1, playerUUID.toString());
            ps.setInt(2, xp);
            ps.setInt(3, xp);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // إضافة مستوى معين للاعب
    public void addLevel(ProxiedPlayer player, int amount) {
        int currentLevel = getLevel(player.getUniqueId());
        setLevel(player.getUniqueId(), currentLevel + amount);
    }

    // إضافة XP معين للاعب
    public void addXP(ProxiedPlayer player, int amount) {
        int currentXP = getXP(player.getUniqueId());
        setXP(player.getUniqueId(), currentXP + amount);
    }

    // الحصول على جميع اللاعبين وترتيبهم حسب المستوى
    public List<Map.Entry<UUID, Integer>> getTopPlayers() {
        List<Map.Entry<UUID, Integer>> topPlayers = new ArrayList<>();
        try (Connection conn = MySQL.getConnection()) {
            if (conn == null) return topPlayers;
            PreparedStatement ps = conn.prepareStatement("SELECT player_uuid, level FROM player_levels ORDER BY level DESC");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("player_uuid"));
                int level = rs.getInt("level");
                topPlayers.add(new AbstractMap.SimpleEntry<>(uuid, level));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topPlayers;
    }
}
