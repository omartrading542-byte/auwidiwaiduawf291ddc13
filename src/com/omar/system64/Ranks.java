package com.omar.system64;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Ranks implements CommandExecutor {

    private final Main plugin;
    private final File ranksFile;
    private org.bukkit.configuration.file.YamlConfiguration ranksConfig;
    private final HashMap<UUID, PermissionAttachment> attachments = new HashMap<>();
    private final boolean useMySQL;

    public Ranks(Main plugin, File customRanksFile) {
        this.plugin = plugin;
        this.ranksFile = customRanksFile;
        if (!ranksFile.exists()) {
            plugin.saveResource("playersrank.yml", false);
        }
        ranksConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(ranksFile);

        MySQL.connect(plugin.getDataFolder());

        useMySQL = MySQL.getConnection() != null;

        if (useMySQL) {
            setupTables();
        }
    }

    private void setupTables() {
        try {
            Connection conn = MySQL.getConnection();
            Statement stmt = conn.createStatement();

            // إنشاء جدول player_ranks إذا لم يكن موجود
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS player_ranks (" +
                "player_uuid VARCHAR(36) NOT NULL PRIMARY KEY," +
                "rank VARCHAR(50) NOT NULL" +
                ")"
            );

            // التأكد من وجود الأعمدة الإضافية، وإذا لم تكن موجودة إضافتها
            ResultSet rs = conn.getMetaData().getColumns(null, null, "player_ranks", "prefix");
            if (!rs.next()) stmt.executeUpdate("ALTER TABLE player_ranks ADD COLUMN prefix VARCHAR(100) DEFAULT ''");
            rs.close();

            rs = conn.getMetaData().getColumns(null, null, "player_ranks", "suffix");
            if (!rs.next()) stmt.executeUpdate("ALTER TABLE player_ranks ADD COLUMN suffix VARCHAR(100) DEFAULT ''");
            rs.close();

            rs = conn.getMetaData().getColumns(null, null, "player_ranks", "rankcolor");
            if (!rs.next()) stmt.executeUpdate("ALTER TABLE player_ranks ADD COLUMN rankcolor VARCHAR(10) DEFAULT '&7'");
            rs.close();

            // جدول player_permissions
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS player_permissions (" +
                "player_uuid VARCHAR(36) NOT NULL," +
                "permission VARCHAR(255) NOT NULL," +
                "PRIMARY KEY(player_uuid, permission)" +
                ")"
            );

            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= Ranks =================

    private String getMatchingRankName(String inputRank) {
        Set<String> keys = ranksConfig.getConfigurationSection("ranks").getKeys(false);
        for (String key : keys) {
            if (key.equalsIgnoreCase(inputRank)) return key;
        }
        return "default";
    }

    public String getRank(Player player) {
        return getPlayerRank(player.getUniqueId());
    }

    public String getPlayerRank(UUID uuid) {
        String rawRank;
        if (useMySQL) {
            rawRank = getPlayerRankFromDB(uuid);
        } else {
            rawRank = ranksConfig.getString("players." + uuid + ".rank", "default");
        }
        return getMatchingRankName(rawRank);
    }

    private String getPlayerRankFromDB(UUID uuid) {
        try {
            PreparedStatement ps = MySQL.getConnection().prepareStatement(
                "SELECT rank FROM player_ranks WHERE player_uuid = ?"
            );
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("rank");
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "default";
    }

    public void setRank(Player player, String rank) {
        PermissionAttachment oldAttachment = attachments.remove(player.getUniqueId());
        if (oldAttachment != null) {
            try { player.removeAttachment(oldAttachment); } catch (IllegalArgumentException ignored) {}
        }

        if (useMySQL) {
            savePlayerRankToDB(player.getUniqueId(), rank, getPrefix(rank), getSuffix(rank), getRankColor(rank));
        } else {
            ranksConfig.set("players." + player.getUniqueId() + ".rank", rank);
            saveRanks();
        }

        applyPermissions(player);
        applyExtraPermissions(player);
        setChatPrefix(player);
    }

    private void savePlayerRankToDB(UUID uuid, String rank, String prefix, String suffix, String rankColor) {
        try {
            PreparedStatement ps = MySQL.getConnection().prepareStatement(
                "INSERT INTO player_ranks (player_uuid, rank, prefix, suffix, rankcolor) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE rank=?, prefix=?, suffix=?, rankcolor=?"
            );
            ps.setString(1, uuid.toString());
            ps.setString(2, rank);
            ps.setString(3, prefix);
            ps.setString(4, suffix);
            ps.setString(5, rankColor);

            ps.setString(6, rank);
            ps.setString(7, prefix);
            ps.setString(8, suffix);
            ps.setString(9, rankColor);

            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveRanks() {
        try { ranksConfig.save(ranksFile); } catch (IOException e) { e.printStackTrace(); }
    }
    

    
    
    public String getDisplayName(Player player) {
        String rank = getRank(player);
        String prefix = getPrefix(rank);
        String rankColor = getRankColor(rank);
        return ChatColor.translateAlternateColorCodes('&', prefix + " " + rankColor + player.getName());
    }

    // ================= Permissions =================

    public void applyPermissions(Player player) {
        String rank = getRank(player);
        List<String> permissions = ranksConfig.getStringList("ranks." + rank + ".permissions");

        // إزالة الـ attachment القديم فقط إذا كان موجودًا
        PermissionAttachment oldAttachment = attachments.get(player.getUniqueId());
        if (oldAttachment != null) {
            try {
                player.removeAttachment(oldAttachment);
            } catch (IllegalArgumentException e) {
                // إذا حدث خطأ في الإزالة تجاهله، لأنه يعني أن الـ attachment غير مضاف
                // ويمكن طباعة سجل لهذا الخطأ إن أردت
            }
            attachments.remove(player.getUniqueId());
        }

        // إضافة attachment جديد وتعيين الصلاحيات
        PermissionAttachment attachment = player.addAttachment(plugin);
        if (permissions != null && !permissions.isEmpty()) {
            for (String perm : permissions) {
                attachment.setPermission(perm, true);
            }
        }

        attachments.put(player.getUniqueId(), attachment);
    }

    public void applyExtraPermissions(Player player) {
        if (!useMySQL) return;
        try {
            PreparedStatement ps = MySQL.getConnection().prepareStatement(
                "SELECT permission FROM player_permissions WHERE player_uuid = ?"
            );
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();

            PermissionAttachment attachment = attachments.get(player.getUniqueId());
            if (attachment == null) {
                attachment = player.addAttachment(plugin);
                attachments.put(player.getUniqueId(), attachment);
            }

            while (rs.next()) {
                attachment.setPermission(rs.getString("permission"), true);
            }

            rs.close();
            ps.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public boolean addPermission(UUID uuid, String permission) {
        if (!useMySQL) return false;
        try {
            PreparedStatement ps = MySQL.getConnection().prepareStatement(
                "INSERT IGNORE INTO player_permissions (player_uuid, permission) VALUES (?, ?)"
            );
            ps.setString(1, uuid.toString());
            ps.setString(2, permission);
            int updated = ps.executeUpdate();
            ps.close();
            return updated > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean removePermission(UUID uuid, String permission) {
        if (!useMySQL) return false;
        try {
            PreparedStatement ps = MySQL.getConnection().prepareStatement(
                "DELETE FROM player_permissions WHERE player_uuid=? AND permission=?"
            );
            ps.setString(1, uuid.toString());
            ps.setString(2, permission);
            int updated = ps.executeUpdate();
            ps.close();
            return updated > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ================= Chat / Prefix / Suffix =================

    public String getPrefix(String rank) {
        if (useMySQL) {
            try {
                PreparedStatement ps = MySQL.getConnection().prepareStatement(
                    "SELECT prefix FROM player_ranks WHERE rank=? LIMIT 1"
                );
                ps.setString(1, rank);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getString("prefix");
                rs.close();
                ps.close();
            } catch (SQLException e) { e.printStackTrace(); }
        }
        return ranksConfig.getString("ranks." + rank + ".prefix", "&7[Player]");
    }

    public String getSuffix(String rank) {
        if (useMySQL) {
            try {
                PreparedStatement ps = MySQL.getConnection().prepareStatement(
                    "SELECT suffix FROM player_ranks WHERE rank=? LIMIT 1"
                );
                ps.setString(1, rank);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getString("suffix");
                rs.close();
                ps.close();
            } catch (SQLException e) { e.printStackTrace(); }
        }
        return ranksConfig.getString("ranks." + rank + ".suffix", "");
    }

    public String getRankColor(String rank) {
        if (useMySQL) {
            try {
                PreparedStatement ps = MySQL.getConnection().prepareStatement(
                    "SELECT rankcolor FROM player_ranks WHERE rank=? LIMIT 1"
                );
                ps.setString(1, rank);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getString("rankcolor");
                rs.close();
                ps.close();
            } catch (SQLException e) { e.printStackTrace(); }
        }
        return ranksConfig.getString("ranks." + rank + ".rankcolor", "&7");
    }

    public void setChatPrefix(Player player) {
        String rank = getRank(player);
        String displayName = ChatColor.translateAlternateColorCodes('&', getPrefix(rank) + " " + getRankColor(rank) + player.getName());
        player.setDisplayName(displayName);
        player.setPlayerListName(displayName);
        updateNameTag(player, rank);
    }

    private void updateNameTag(Player player, String rank) {
        ScoreboardManager manager = plugin.getServer().getScoreboardManager();
        if (manager == null) return;
        Scoreboard board = manager.getMainScoreboard();

        Team team = board.getTeam(rank);
        if (team == null) {
            team = board.registerNewTeam(rank);
            team.setPrefix(ChatColor.translateAlternateColorCodes('&', getRankColor(rank)) + " ");
            team.setSuffix(ChatColor.translateAlternateColorCodes('&', getSuffix(rank)) + " ");
        }

        team.addEntry(player.getName());
    }

    // ================= CommandExecutor =================

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(ChatColor.RED + "Only players or console can run this command.");
            return true;
        }

        if (!sender.hasPermission("system64.setpermission")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("setpermission")) {
            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /setPermission <player> <permission>");
                return true;
            }

            Player target = plugin.getServer().getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found or offline.");
                return true;
            }

            String perm = args[1];
            PermissionAttachment attachment = attachments.get(target.getUniqueId());
            if (attachment == null) {
                attachment = target.addAttachment(plugin);
                attachments.put(target.getUniqueId(), attachment);
            }

            attachment.setPermission(perm, true);

            sender.sendMessage(ChatColor.GREEN + "Permission " + perm + " has been granted to " + target.getName());
            target.sendMessage(ChatColor.GREEN + "You have been granted permission: " + perm);
            return true;
        }

        else if (cmd.getName().equalsIgnoreCase("addpermission")) {
            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /addPermission <player> <permission>");
                return true;
            }

            Player target = plugin.getServer().getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found or offline.");
                return true;
            }

            String perm = args[1];
            boolean success = addPermission(target.getUniqueId(), perm);
            if (success) {
                PermissionAttachment attachment = attachments.get(target.getUniqueId());
                if (attachment == null) {
                    attachment = target.addAttachment(plugin);
                    attachments.put(target.getUniqueId(), attachment);
                }
                attachment.setPermission(perm, true);

                sender.sendMessage(ChatColor.GREEN + "Permission " + perm + " added to " + target.getName());
                target.sendMessage(ChatColor.GREEN + "You have been granted permission: " + perm);
            } else {
                sender.sendMessage(ChatColor.RED + "Failed to add permission. MySQL might be disabled.");
            }
            return true;
        }

        else if (cmd.getName().equalsIgnoreCase("removepermission")) {
            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /removePermission <player> <permission>");
                return true;
            }

            Player target = plugin.getServer().getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found or offline.");
                return true;
            }

            String perm = args[1];
            boolean success = removePermission(target.getUniqueId(), perm);
            if (success) {
                PermissionAttachment attachment = attachments.get(target.getUniqueId());
                if (attachment != null) attachment.unsetPermission(perm);

                sender.sendMessage(ChatColor.GREEN + "Permission " + perm + " removed from " + target.getName());
                target.sendMessage(ChatColor.RED + "Your permission " + perm + " has been removed.");
            } else {
                sender.sendMessage(ChatColor.RED + "Failed to remove permission. MySQL might be disabled or permission does not exist.");
            }
            return true;
        }

        return false;
    }
}
