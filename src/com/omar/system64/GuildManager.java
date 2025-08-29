package com.omar.system64;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class GuildManager {

    private final Main plugin;
    private final Ranks ranks;

    private final File guildsFile;
    private YamlConfiguration guildsConfig;

    private final File ranksFile;
    private YamlConfiguration ranksConfig;

    private final File hologramFile;
    private YamlConfiguration hologramConfig;

    public GuildManager(Main plugin, Ranks ranks) {
        this.plugin = plugin;
        this.ranks = ranks;

        this.guildsFile = new File(plugin.getDataFolder(), "guilds.yml");
        if (!guildsFile.exists()) plugin.saveResource("guilds.yml", false);
        this.guildsConfig = YamlConfiguration.loadConfiguration(guildsFile);

        this.ranksFile = new File(plugin.getDataFolder(), "playersrank.yml");
        if (!ranksFile.exists()) plugin.saveResource("playersrank.yml", false);
        this.ranksConfig = YamlConfiguration.loadConfiguration(ranksFile);

        this.hologramFile = new File(plugin.getDataFolder(), "hologramg.yml");
        if (!hologramFile.exists()) {
            try { hologramFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        this.hologramConfig = YamlConfiguration.loadConfiguration(hologramFile);

        createTablesIfNotExists();
    }

    private void createTablesIfNotExists() {
        Connection connection = MySQL.getConnection();
        if (connection == null) return;

        String guildsTable = "CREATE TABLE IF NOT EXISTS guilds (" +
                "guild_name VARCHAR(100) PRIMARY KEY," +
                "owner_uuid VARCHAR(36)," +
                "prefix VARCHAR(100)," +
                "suffix VARCHAR(100)," +
                "color VARCHAR(10)," +
                "level INT," +
                "xp INT," +
                "members TEXT" +
                ");";

        String ranksTable = "CREATE TABLE IF NOT EXISTS players_rank (" +
                "player_uuid VARCHAR(36) PRIMARY KEY," +
                "rank VARCHAR(50)" +
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(guildsTable);
            stmt.executeUpdate(ranksTable);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void createGuild(Player player, String guildName, String colorCode) {
        if (guildExists(guildName)) {
            player.sendMessage(ChatColor.RED + "Guild with this name already exists!");
            return;
        }

        Connection connection = MySQL.getConnection();
        if (connection == null) {
            player.sendMessage(ChatColor.RED + "Database connection is not available.");
            return;
        }

        String formattedColor = ChatColor.translateAlternateColorCodes('&', colorCode);
        List<String> members = new ArrayList<>();
        members.add(player.getUniqueId().toString());

        String prefix = formattedColor + "[" + guildName + "]";

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO guilds (guild_name, owner_uuid, prefix, suffix, color, level, xp, members) VALUES (?, ?, ?, ?, ?, ?, ?, ?);"
        )) {
            ps.setString(1, guildName);
            ps.setString(2, player.getUniqueId().toString());
            ps.setString(3, prefix);
            ps.setString(4, "");
            ps.setString(5, colorCode);
            ps.setInt(6, 1);
            ps.setInt(7, 0);
            ps.setString(8, String.join(",", members));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Error occurred while creating guild.");
            return;
        }

        // ✅ إضافة اللاعب كـ OWNER في جدول players_rank
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO players_rank (player_uuid, rank) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE rank=?"
        )) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, "OWNER");
            ps.setString(3, "OWNER");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // ✅ حفظ نسخة في YAML
        guildsConfig.set("guilds." + guildName + ".owner", player.getUniqueId().toString());
        guildsConfig.set("guilds." + guildName + ".prefix", prefix);
        guildsConfig.set("guilds." + guildName + ".suffix", "");
        guildsConfig.set("guilds." + guildName + ".color", colorCode);
        guildsConfig.set("guilds." + guildName + ".level", 1);
        guildsConfig.set("guilds." + guildName + ".xp", 0);
        guildsConfig.set("guilds." + guildName + ".members", members);
        saveGuilds();

        // ✅ حفظ الرتبة في playersrank.yml
        ranksConfig.set("players." + player.getUniqueId().toString() + ".rank", "OWNER");
        try {
            ranksConfig.save(ranksFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.sendMessage(ChatColor.GREEN + "Guild created successfully: " + prefix);
        player.sendMessage(ChatColor.GOLD + "You are now the OWNER of your guild!");
    }

    public boolean guildExists(String guildName) {
        Connection connection = MySQL.getConnection();
        if (connection == null) return false;

        String query = "SELECT guild_name FROM guilds WHERE guild_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, guildName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    
    

    public Map<String, Object> getGuildData(String guildName) {
        Connection connection = MySQL.getConnection();
        Map<String, Object> data = new HashMap<>();
        if (connection == null) return data;

        String query = "SELECT * FROM guilds WHERE guild_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, guildName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data.put("owner", rs.getString("owner_uuid"));
                    data.put("prefix", rs.getString("prefix"));
                    data.put("suffix", rs.getString("suffix"));
                    data.put("color", rs.getString("color"));
                    data.put("level", rs.getInt("level"));
                    data.put("xp", rs.getInt("xp"));
                    String membersCSV = rs.getString("members");
                    if (membersCSV != null && !membersCSV.isEmpty()) {
                        data.put("members", Arrays.asList(membersCSV.split(",")));
                    } else {
                        data.put("members", new ArrayList<String>());
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return data;
    }
    
    
    

    private String getGuildFieldByUUID(UUID uuid, String field) {
        Connection connection = MySQL.getConnection();
        if (connection == null) return field.equalsIgnoreCase("color") ? "&7" : "";

        String query = "SELECT " + field + ", members FROM guilds";
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String membersCSV = rs.getString("members");
                if (membersCSV != null) {
                    List<String> members = Arrays.asList(membersCSV.split(","));
                    if (members.contains(uuid.toString())) {
                        if (field.equalsIgnoreCase("color")) {
                            return rs.getString(field);
                        } else {
                            return ChatColor.translateAlternateColorCodes('&', rs.getString(field));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (field.equalsIgnoreCase("color")) return "&7";
        return "";
    }

    public String getGuildPrefix(UUID uuid) {
        return getGuildFieldByUUID(uuid, "prefix");
    }

    public String getGuildSuffix(UUID uuid) {
        return getGuildFieldByUUID(uuid, "suffix");
    }

    public String getGuildColor(UUID uuid) {
        return getGuildFieldByUUID(uuid, "color");
    }

    public int getGuildLevel(String guildName) {
        Connection connection = MySQL.getConnection();
        if (connection == null) return 0;

        String query = "SELECT level FROM guilds WHERE guild_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, guildName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("level");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getGuildXP(String guildName) {
        Connection connection = MySQL.getConnection();
        if (connection == null) return 0;

        String query = "SELECT xp FROM guilds WHERE guild_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, guildName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("xp");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<String> getGuildMembers(String guildName) {
        Connection connection = MySQL.getConnection();
        if (connection == null) return new ArrayList<>();

        String query = "SELECT members FROM guilds WHERE guild_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, guildName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String membersCSV = rs.getString("members");
                    if (membersCSV != null && !membersCSV.isEmpty()) {
                        return Arrays.asList(membersCSV.split(","));
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return new ArrayList<>();
    }
    
    public String getPlayerGuild(UUID playerUUID) {
        for (String guildName : getAllGuildNames()) {
            List<String> members = getGuildMembers(guildName);
            if (members.contains(playerUUID.toString())) return guildName;
        }
        return null;
    }
    
    
    public void updateGuildMembers(String guildName, List<String> members) {
        Connection connection = MySQL.getConnection();
        if (connection != null) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE guilds SET members=? WHERE guild_name=?")) {
                ps.setString(1, String.join(",", members));
                ps.setString(2, guildName);
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }

        guildsConfig.set("guilds." + guildName + ".members", members);
        saveGuilds();
    }
    
    
    public List<UUID> getOnlineGuildMembersUUID(String guildName) {
        List<UUID> onlineMembers = new ArrayList<>();
        for (String uuidStr : getGuildMembers(guildName)) {
            Player player = Bukkit.getPlayer(UUID.fromString(uuidStr));
            if (player != null && player.isOnline()) onlineMembers.add(player.getUniqueId());
        }
        return onlineMembers;
    }
    
    
    public String getGuildMemberRank(UUID playerUUID) {
        return getPlayerRank(playerUUID);
    }

    public int getGuildRankValue(String rank) {
        switch (rank.toUpperCase()) {
            case "OWNER": return 5;
            case "ADMIN": return 4;
            case "MOD": return 3;
            case "PRO": return 2;
            case "MEMBER": return 1;
            default: return 0;
        }
    }
    
    
    public boolean leaveGuild(UUID playerUUID) {
        String guildName = getPlayerGuild(playerUUID);
        if (guildName == null) return false;

        Map<String, Object> guildData = getGuildData(guildName);
        String ownerUUID = (String) guildData.get("owner");

        // ✅ إذا كان هو المالك، احذف الجيلد كامل
        if (ownerUUID.equals(playerUUID.toString())) {
            deleteGuild(guildName);
            return true;
        }

        // ✅ إذا كان عضو عادي، فقط يتم إزالته من قائمة الأعضاء
        List<String> members = getGuildMembers(guildName);
        members.remove(playerUUID.toString());
        updateGuildMembers(guildName, members);

        return true;
    }
    
    
    public void deleteGuild(String guildName) {
        Connection connection = MySQL.getConnection();
        if (connection != null) {
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM guilds WHERE guild_name=?")) {
                ps.setString(1, guildName);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // حذف من guilds.yml
        guildsConfig.set("guilds." + guildName, null);
        saveGuilds();

        Bukkit.broadcastMessage(ChatColor.RED + "Guild " + ChatColor.GOLD + guildName + ChatColor.RED + " has been disbanded!");
    }
    
    
    public boolean sendGuildInvite(UUID senderUUID, UUID targetUUID) {
        String senderGuild = getPlayerGuild(senderUUID);
        if (senderGuild == null) return false;

        String senderRank = getGuildMemberRank(senderUUID);
        if (getGuildRankValue(senderRank) < getGuildRankValue("PRO")) return false;

        Player target = Bukkit.getPlayer(targetUUID);
        if (target != null && target.isOnline()) {
            target.sendMessage(ChatColor.GREEN + "You received a guild invite from " +
                    Bukkit.getOfflinePlayer(senderUUID).getName() + "!");
            return true;
        }
        return false;
    }
    
    
    public void sendGuildMessage(UUID senderUUID, String message) {
        String guildName = getPlayerGuild(senderUUID);
        if (guildName == null) return;

        String senderRank = getGuildMemberRank(senderUUID);
        String rankPrefix = ranks.getRankColor(senderRank);

        for (UUID memberUUID : getOnlineGuildMembersUUID(guildName)) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null) {
                member.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        rankPrefix + Bukkit.getOfflinePlayer(senderUUID).getName() + ": &f" + message));
            }
        }
    }
    
    public Map<String, String> getGuildMembersStatus(UUID requesterUUID) {
        String guildName = getPlayerGuild(requesterUUID);
        if (guildName == null) return Collections.emptyMap();

        Map<String, String> membersStatus = new LinkedHashMap<>();
        List<String> members = getGuildMembers(guildName);
        for (String memberUUIDStr : members) {
            UUID memberUUID = UUID.fromString(memberUUIDStr);
            Player player = Bukkit.getPlayer(memberUUID);
            String name = (player != null) ? player.getName() : "Offline";
            boolean isInGuildChat = false;
            
            // تحقق إذا كان الـ GuildChatListener موجود
            if (plugin.getGuildChatListener() != null) {
                isInGuildChat = plugin.getGuildChatListener().isGuildChat(memberUUID);
            }
            
            String status = (player != null && player.isOnline() ? "Online" : "Offline"); 
                         
            membersStatus.put(name, status);
        }
        return membersStatus;
    }
    
    
    public List<String> getAllGuildNames() {
        Connection connection = MySQL.getConnection();
        List<String> guildNames = new ArrayList<>();
        if (connection == null) return guildNames;

        String query = "SELECT guild_name FROM guilds";
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) guildNames.add(rs.getString("guild_name"));
        } catch (SQLException e) { e.printStackTrace(); }
        return guildNames;
    }

    public String getTopGuild() {
        Connection connection = MySQL.getConnection();
        if (connection == null) return null;

        String topGuild = null;
        int topLevel = 0;
        String query = "SELECT guild_name, level FROM guilds";
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int level = rs.getInt("level");
                String name = rs.getString("guild_name");
                if (level > topLevel) {
                    topLevel = level;
                    topGuild = name;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topGuild;
    }

    public void updateTopGuildHologram() {
        if (!hologramConfig.contains("hologram.world")) return;

        String world = hologramConfig.getString("hologram.world");
        double x = hologramConfig.getDouble("hologram.x");
        double y = hologramConfig.getDouble("hologram.y");
        double z = hologramConfig.getDouble("hologram.z");

        if (Bukkit.getWorld(world) == null) {
            plugin.getLogger().warning("World not found: " + world);
            return;
        }

        Location location = new Location(Bukkit.getWorld(world), x, y, z);

        boolean hologramExists = false;
        for (Hologram hologram : HologramsAPI.getHolograms(plugin)) {
            if (hologram.getLocation().distance(location) < 0.5) {
                hologramExists = true;
                updateHologramContent(hologram);
                break;
            }
        }
        if (!hologramExists) {
            createNewHologram(location);
        }
    }

    public YamlConfiguration getGuildsConfig() {
        return guildsConfig;
    }

    public void updateHologramContent(Hologram hologram) {
        hologram.clearLines();
        hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', "&bTop Guilds"));
        hologram.appendTextLine("");

        Connection connection = MySQL.getConnection();
        if (connection == null) return;

        List<Map.Entry<String, Integer>> topGuilds = new ArrayList<>();

        String query = "SELECT guild_name, level FROM guilds";
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                topGuilds.add(new AbstractMap.SimpleEntry<>(rs.getString("guild_name"), rs.getInt("level")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        topGuilds.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        Ranks ranks = plugin.getRanks();
        int max = Math.min(10, topGuilds.size());

        for (int i = 0; i < max; i++) {
            String guild = topGuilds.get(i).getKey();
            int level = topGuilds.get(i).getValue();

            Map<String, Object> data = getGuildData(guild);
            String guildPrefix = ChatColor.translateAlternateColorCodes('&',
                    data.getOrDefault("prefix", guild).toString());

            String ownerUUID = (String) data.get("owner");
            String ownerName = "Unknown";
            String rankPrefix = "";
            String rankColor = ChatColor.GRAY.toString();

            if (ownerUUID != null) {
                try {
                    UUID uuid = UUID.fromString(ownerUUID);
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(uuid);

                    if (owner.getName() != null) {
                        ownerName = owner.getName();

                        if (ranks != null) {
                            String playerRank = ranks.getPlayerRank(uuid); // مثل: OWNER, ADMIN
                            rankColor = ranks.getRankColor(playerRank);    // لون الرتبة
                            String prefix = ranks.getPrefix(playerRank);   // مثل: [OWNER]
                            if (prefix != null) {
                                rankPrefix = ChatColor.translateAlternateColorCodes('&', prefix);
                            }
                        }
                    }
                } catch (IllegalArgumentException ignored) {}
            }

            String line = String.format(Locale.ENGLISH, "&e%d. %s%s %s%s &7- &6%d",
                    (i + 1), rankPrefix, rankColor, ownerName, guildPrefix, level);

            hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', line));
        }
    }





    public void setTopGuildHologram(Location location) {
        hologramConfig.set("hologram.world", location.getWorld().getName());
        hologramConfig.set("hologram.x", location.getX());
        hologramConfig.set("hologram.y", location.getY());
        hologramConfig.set("hologram.z", location.getZ());
        saveHologram();

        createNewHologram(location);
    }

    public void createNewHologram(Location location) {
        Hologram hologram = HologramsAPI.createHologram(plugin, location);
        updateHologramContent(hologram);
    }

    public void loadTopGuildHologram() {
        if (!hologramConfig.contains("hologram.world")) {
            plugin.getLogger().info("[GuildManager] No hologram location found in hologramg.yml");
            return;
        }

        String world = hologramConfig.getString("hologram.world");
        double x = hologramConfig.getDouble("hologram.x");
        double y = hologramConfig.getDouble("hologram.y");
        double z = hologramConfig.getDouble("hologram.z");

        plugin.getLogger().info("[GuildManager] Loading Top Guild Hologram at: " + world + " (" + x + ", " + y + ", " + z + ")");

        Location location = new Location(plugin.getServer().getWorld(world), x, y, z);
        setTopGuildHologram(location);
    }

    public String getPlayerRank(UUID uuid) {
        Connection connection = MySQL.getConnection();
        if (connection == null) return ranksConfig.getString("players." + uuid.toString() + ".rank", "default");

        String query = "SELECT rank FROM players_rank WHERE player_uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("rank");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ranksConfig.getString("players." + uuid.toString() + ".rank", "default");
    }

    public void saveGuilds() {
        try {
            guildsConfig.save(guildsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveHologram() {
        try {
            hologramConfig.save(hologramFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
