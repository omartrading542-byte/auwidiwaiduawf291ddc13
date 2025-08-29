package com.omar.system64.BungeeMain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BungeeAchievementManager {

    private static final String LEVELS_TABLE = "achievement_levels"; // جدول المستوى و XP

    // =====================
    // المستوى و XP
    // =====================

    public int getLevel(UUID playerUUID) {
        try (Connection conn = MySQL.getConnection()) {
            if (conn == null) return 0;
            PreparedStatement ps = conn.prepareStatement(
                "SELECT level FROM " + LEVELS_TABLE + " WHERE player_uuid = ?");
            ps.setString(1, playerUUID.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("level");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getXP(UUID playerUUID) {
        try (Connection conn = MySQL.getConnection()) {
            if (conn == null) return 0;
            PreparedStatement ps = conn.prepareStatement(
                "SELECT xp FROM " + LEVELS_TABLE + " WHERE player_uuid = ?");
            ps.setString(1, playerUUID.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("xp");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setLevel(UUID playerUUID, int level) {
        try (Connection conn = MySQL.getConnection()) {
            if (conn == null) return;
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + LEVELS_TABLE + " (player_uuid, level) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE level = ?");
            ps.setString(1, playerUUID.toString());
            ps.setInt(2, level);
            ps.setInt(3, level);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setXP(UUID playerUUID, int xp) {
        try (Connection conn = MySQL.getConnection()) {
            if (conn == null) return;
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + LEVELS_TABLE + " (player_uuid, xp) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE xp = ?");
            ps.setString(1, playerUUID.toString());
            ps.setInt(2, xp);
            ps.setInt(3, xp);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
