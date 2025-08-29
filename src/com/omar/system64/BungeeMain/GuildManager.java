package com.omar.system64.BungeeMain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

// كلاس لإدارة الجيلد من MySQL
public class GuildManager {

    // كلاس داخلي يمثل بيانات الجيلد
    public static class GuildData {
        private final String ownerUUID;
        private final String prefix;
        private final String suffix;
        private final String color;
        private final int level;
        private final int xp;
        private final List<String> members;

        public GuildData(String ownerUUID, String prefix, String suffix, String color, int level, int xp, List<String> members) {
            this.ownerUUID = ownerUUID;
            this.prefix = prefix;
            this.suffix = suffix;
            this.color = color;
            this.level = level;
            this.xp = xp;
            this.members = members;
        }

        public String getOwnerUUID() { return ownerUUID; }
        public String getPrefix() { return prefix; }
        public String getSuffix() { return suffix; }
        public String getColor() { return color; }
        public int getLevel() { return level; }
        public int getXP() { return xp; }
        public List<String> getMembers() { return members; }
    }

    // جلب اسم الجيلد للاعب حسب UUID
    public String getPlayerGuild(UUID playerUUID) {
        try (Connection connection = MySQL.getConnection()) {
            if (connection == null) return null;

            String query = "SELECT guild_name, members FROM guilds";
            try (PreparedStatement ps = connection.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String guildName = rs.getString("guild_name");
                    String membersCSV = rs.getString("members");
                    if (membersCSV != null && !membersCSV.isEmpty()) {
                        List<String> members = Arrays.asList(membersCSV.split(","));
                        if (members.contains(playerUUID.toString())) {
                            return guildName;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // لا يوجد جيلد
    }

    // جلب بيانات الجيلد كاملة
    public GuildData getGuildData(String guildName) {
        try (Connection connection = MySQL.getConnection()) {
            if (connection == null) return null;

            String query = "SELECT * FROM guilds WHERE guild_name = ?";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, guildName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String owner = rs.getString("owner_uuid");
                        String prefix = rs.getString("prefix");
                        String suffix = rs.getString("suffix");
                        String color = rs.getString("color");
                        int level = rs.getInt("level");
                        int xp = rs.getInt("xp");

                        String membersCSV = rs.getString("members");
                        List<String> members = (membersCSV != null && !membersCSV.isEmpty())
                                ? Arrays.asList(membersCSV.split(","))
                                : new ArrayList<>();

                        return new GuildData(owner, prefix, suffix, color, level, xp, members);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
