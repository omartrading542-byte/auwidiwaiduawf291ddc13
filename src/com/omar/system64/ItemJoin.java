package com.omar.system64;

import com.omar.system64.achievements.AchievementManager;
import com.omar.system64.levelManager.LevelManager;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.*;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.text.NumberFormat;
import java.util.*;

import static java.awt.SystemColor.menu;

public class ItemJoin implements Listener {

    private FileConfiguration config;
    private final Map<String, Inventory> menus = new HashMap<>();
    private final Map<String, String> commandMap = new HashMap<>();
    private final Map<UUID, List<ItemStack>> playerItems = new HashMap<>();
    private File pvpFile;
    private FileConfiguration pvpConfig;
    private final Ranks ranks;
    private final AchievementManager achievementManager;
    private final LevelManager levelManager;
    private final Plugin plugin;
    private final GuildManager guildManager;

    public ItemJoin(LevelManager levelManager, Ranks ranks, AchievementManager achievementManager, GuildManager guildManager) {
        loadConfig();
        loadPvpConfig();
        this.ranks = ranks;
        this.achievementManager = achievementManager;
        this.levelManager = levelManager;
        this.plugin = Main.getInstance();
        this.guildManager = guildManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();  // تهيئة اللاعب
        loadMenus(player);
        giveItems(event.getPlayer());
    }
    // التأكد من وجود ملف "pvp.yml"
    private void loadPvpConfig() {
        pvpFile = new File(Bukkit.getPluginManager().getPlugin("system64").getDataFolder(), "pvp.yml");
        pvpConfig = YamlConfiguration.loadConfiguration(pvpFile);
        if (!pvpFile.exists()) {
            try {
                Bukkit.getPluginManager().getPlugin("system64").saveResource("pvp.yml", false);
            } catch (IllegalArgumentException ignored) {
                Bukkit.getPluginManager().getPlugin("system64").getLogger().warning("pvp.yml not found in jar, creating blank.");
                try {
                    pvpFile.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadConfig() {
        File file = new File(Bukkit.getPluginManager().getPlugin("system64").getDataFolder(), "itemjoin.yml");
        if (!file.exists()) {
            Bukkit.getPluginManager().getPlugin("system64").saveResource("itemjoin.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();

        if (isInAnyRegion(loc)) {
            if (!playerItems.containsKey(player.getUniqueId())) {
                storePlayerItems(player);
                removePlayerItems(player);

            }
        } else {
            if (playerItems.containsKey(player.getUniqueId())) {
                restorePlayerItems(player);
                playerItems.remove(player.getUniqueId());
                giveItems(player); // إعطاء العناصر عند الخروج من المنطقة
            }
        }
    }

    private boolean isInAnyRegion(Location loc) {
        List<?> areasRaw = pvpConfig.getList("pvp_areas");
        if (areasRaw == null) return false;

        List<Map<String, Object>> areas = new ArrayList<>();
        for (Object obj : areasRaw) {
            if (obj instanceof Map) {
                areas.add((Map<String, Object>) obj);
            }
        }

        for (Map<String, Object> region : areas) {
            String world = (String) region.get("world");
            if (!loc.getWorld().getName().equals(world)) continue;

            double x1 = toDouble(region.get("x1"));
            double y1 = toDouble(region.get("y1"));
            double z1 = toDouble(region.get("z1"));
            double x2 = toDouble(region.get("x2"));
            double y2 = toDouble(region.get("y2"));
            double z2 = toDouble(region.get("z2"));

            double minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
            double minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
            double minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);

            double px = loc.getX(), py = loc.getY(), pz = loc.getZ();

            if (px >= minX && px <= maxX && py >= minY && py <= maxY && pz >= minZ && pz <= maxZ) {
                return true;
            }
        }
        return false;
    }

    private void storePlayerItems(Player player) {
        List<ItemStack> items = new ArrayList<>(Arrays.asList(player.getInventory().getContents()));
        items.addAll(Arrays.asList(player.getInventory().getArmorContents()));
        playerItems.put(player.getUniqueId(), items);
    }

    private void removePlayerItems(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
    }

    private void restorePlayerItems(Player player) {
        List<ItemStack> items = playerItems.get(player.getUniqueId());
        if (items != null) {
            for (ItemStack item : items) {
                if (item != null) {
                    player.getInventory().addItem(item);
                }
            }
        }
    }

    private double toDouble(Object obj) {
        if (obj instanceof Integer) return (double) (Integer) obj;
        if (obj instanceof Double) return (Double) obj;
        return 0.0;
    }

    // دالة لتنسيق الأرقام بالفواصل
    private String formatNumber(int number) {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        return nf.format(number);
    }

    private void loadMenus(Player player) {
        ConfigurationSection menusSection = config.getConfigurationSection("menus");
        if (menusSection == null) return;

        int achievementsCount = achievementManager.getPlayerAchievements(player.getUniqueId()).size();
        File achFile = new File(plugin.getDataFolder(), "ach.yml");
        FileConfiguration achConfig = YamlConfiguration.loadConfiguration(achFile);
        String playerUUID = player.getUniqueId().toString();
        int achievementLevel = achConfig.contains("players." + playerUUID + ".level") ?
                achConfig.getInt("players." + playerUUID + ".level") : 0;

        String rank = ranks.getRank(player);
        String prefix = ranks.getPrefix(rank);
        int level = levelManager.getLevel(player.getUniqueId());
        int xp = levelManager.getXP(player.getUniqueId());

        String guildPrefix = guildManager.getGuildPrefix(player.getUniqueId());
        String guildColor = guildManager.getGuildColor(player.getUniqueId());
        String guildSuffix = guildManager.getGuildSuffix(player.getUniqueId());

        // إضافة رسالة افتراضية إذا ما عنده Guild
        if (guildPrefix == null || guildPrefix.isEmpty()) guildPrefix = ChatColor.RED + "No Guild";
        if (guildSuffix == null || guildSuffix.isEmpty()) guildSuffix = "";

        for (String menuKey : menusSection.getKeys(false)) {
            ConfigurationSection menu = menusSection.getConfigurationSection(menuKey);

            String rawTitle = ChatColor.translateAlternateColorCodes('&', menu.getString("title")
                    .replace("(player)", player.getName())
                    .replace("(prefix)", prefix)
                    .replace("(level)", formatNumber(level))
                    .replace("(achievementlevel)", formatNumber(achievementLevel))
                    .replace("(guild_prefix)", guildPrefix)
                    .replace("(guildsuffix)", guildSuffix)
            );

            Inventory inv = Bukkit.createInventory(null, menu.getInt("size"), rawTitle);
            ConfigurationSection items = menu.getConfigurationSection("items");
            if (items == null) continue;

            for (String itemKey : items.getKeys(false)) {
                ConfigurationSection itemSection = items.getConfigurationSection(itemKey);
                Material material = Material.getMaterial(itemSection.getString("material"));
                String name = ChatColor.translateAlternateColorCodes('&', itemSection.getString("name"));

                name = ChatColor.translateAlternateColorCodes('&', name
                        .replace("(player)", player.getName())
                        .replace("(prefix)", prefix)
                        .replace("(level)", formatNumber(level))
                        .replace("(achievementlevel)", formatNumber(achievementLevel))
                        .replace("(guild_prefix)", guildPrefix)
                        .replace("(guildsuffix)", guildSuffix)
                );

                int slot = itemSection.getInt("slot");
                int data = itemSection.contains("data") ? itemSection.getInt("data") : 0;
                ItemStack item = new ItemStack(material, 1, (short) data);

                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(name);
                    if (itemSection.contains("lore")) {
                        List<String> rawLore = itemSection.getStringList("lore");
                        List<String> lore = new ArrayList<>();
                        for (String line : rawLore) {
                            line = ChatColor.translateAlternateColorCodes('&', line
                                    .replace("(level)", formatNumber(level))
                                    .replace("(achievements)", formatNumber(achievementsCount))
                                    .replace("(achievementlevel)", formatNumber(achievementLevel))
                                    .replace("(guild_prefix)", guildPrefix)
                                    .replace("(guildsuffix)", guildSuffix)
                            );
                            lore.add(line);
                        }
                        meta.setLore(lore);
                    }
                    if (material == Material.SKULL_ITEM && data == 3) {
                        SkullMeta skullMeta = (SkullMeta) meta;
                        if (skullMeta != null) {
                            skullMeta.setDisplayName(name);
                            skullMeta.setOwner(player.getName());
                            item.setItemMeta(skullMeta);
                        }
                    } else {
                        item.setItemMeta(meta);
                    }
                }
                inv.setItem(slot, item);
                commandMap.put(name, itemSection.getString("command"));
            }

            menus.put(menuKey, inv);
        }
    }

    private void giveItems(Player player) {
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection == null) return;

        player.getInventory().clear();

        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            if (itemSection == null) continue;

            Material material = Material.getMaterial(itemSection.getString("material"));
            if (material == null) continue;

            String name = ChatColor.translateAlternateColorCodes('&', itemSection.getString("name"));
            String playerName = player.getName();
            String prefix = ranks.getPrefix(ranks.getRank(player));
            name = name.replace("(player)", playerName).replace("(prefix)", prefix);

            int slot = itemSection.getInt("slot");
            int data = itemSection.contains("data") ? itemSection.getInt("data") : 0;

            ItemStack item = new ItemStack(material, 1, (short) data);

            if (material == Material.SKULL_ITEM && data == 3) {
                SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
                if (skullMeta != null) {
                    skullMeta.setDisplayName(name);
                    skullMeta.setOwner(player.getName());

                    if (itemSection.contains("lore")) {
                        List<String> rawLore = itemSection.getStringList("lore");
                        List<String> lore = new ArrayList<>();
                        for (String line : rawLore) {
                            line = ChatColor.translateAlternateColorCodes('&', line.replace("(player)", player.getName()));
                            lore.add(line);
                        }
                        skullMeta.setLore(lore);
                    }

                    item.setItemMeta(skullMeta);
                }
            } else {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(name);
                    if (itemSection.contains("lore")) {
                        List<String> rawLore = itemSection.getStringList("lore");
                        List<String> lore = new ArrayList<>();
                        for (String line : rawLore) {
                            line = ChatColor.translateAlternateColorCodes('&', line.replace("(player)", player.getName()));
                            lore.add(line);
                        }
                        meta.setLore(lore);
                    }
                    item.setItemMeta(meta);
                }
            }

            player.getInventory().setItem(slot, item);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("system64"), () -> giveItems(player), 2L);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() == null || !event.getItem().hasItemMeta()) return;

        switch (event.getAction()) {
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                break;
            default:
                return;
        }

        String displayName = event.getItem().getItemMeta().getDisplayName();
        Player player = event.getPlayer();
        String playerName = player.getName();

        String rank = ranks.getRank(player);
        String prefix = ranks.getPrefix(rank);
        String guildPrefix = guildManager.getGuildPrefix(player.getUniqueId());
        String guildColor = guildManager.getGuildColor(player.getUniqueId());
        String guildSuffix = guildManager.getGuildSuffix(player.getUniqueId());

        File achFile = new File(plugin.getDataFolder(), "ach.yml");
        FileConfiguration achConfig = YamlConfiguration.loadConfiguration(achFile);
        String playerUUID = player.getUniqueId().toString();
        int achievementLevel = achConfig.contains("players." + playerUUID + ".level") ?
                achConfig.getInt("players." + playerUUID + ".level") : 0;

        int level = levelManager.getLevel(player.getUniqueId());
        int achievementsCount = achievementManager.getPlayerAchievements(player.getUniqueId()).size();

        String modifiedDisplayName = displayName.replace("(player)", playerName)
                .replace("(prefix)", prefix)
                .replace("(rank)", rank)
                .replace("(level)", formatNumber(level))
                .replace("(achievementlevel)", formatNumber(achievementLevel))
                .replace("(guild_prefix)", guildPrefix)
                .replace("(guildsuffix)", guildSuffix);

        for (String menuKey : menus.keySet()) {
            String configName = ChatColor.translateAlternateColorCodes('&', config.getString("items." + menuKey + ".name"));
            if (modifiedDisplayName.equals(configName)) {
                player.openInventory(menus.get(menuKey));
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        // إذا الإنفنتوري هو مينو خاص بالبلغن
        String title = ChatColor.stripColor(event.getView().getTitle());
        for (String menuKey : menus.keySet()) {
            String configTitle = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                    config.getString("menus." + menuKey + ".title")));
            if (title.equals(configTitle)) {
                event.setCancelled(true);

                ItemStack clicked = event.getCurrentItem();
                if (clicked == null || !clicked.hasItemMeta()) return;

                String displayName = clicked.getItemMeta().getDisplayName();
                String playerName = player.getName();
                String rank = ranks.getRank(player);
                String prefix = ranks.getPrefix(rank);
                int level = levelManager.getLevel(player.getUniqueId());
                int achievementsCount = achievementManager.getPlayerAchievements(player.getUniqueId()).size();

                File achFile = new File(plugin.getDataFolder(), "ach.yml");
                FileConfiguration achConfig = YamlConfiguration.loadConfiguration(achFile);
                String playerUUID = player.getUniqueId().toString();
                String guildSuffix = guildManager.getGuildSuffix(player.getUniqueId());
                int achievementLevel = achConfig.contains("players." + playerUUID + ".level")
                        ? achConfig.getInt("players." + playerUUID + ".level") : 0;

                String modifiedDisplayName = displayName.replace("(player)", playerName)
                        .replace("(prefix)", prefix)
                        .replace("(rank)", rank)
                        .replace("(level)", formatNumber(level))
                        .replace("(achievements)", formatNumber(achievementsCount))
                        .replace("(achievementlevel)", formatNumber(achievementLevel))
                        .replace("(guildsuffix)", guildSuffix);

                List<String> lore = clicked.getItemMeta().getLore();
                if (lore != null) {
                    List<String> modifiedLore = new ArrayList<>();
                    for (String line : lore) {
                        modifiedLore.add(line.replace("(player)", playerName)
                                .replace("(prefix)", prefix)
                                .replace("(rank)", rank)
                                .replace("(level)", formatNumber(level))
                                .replace("(achievements)", formatNumber(achievementsCount))
                                .replace("(achievementlevel)", formatNumber(achievementLevel)));
                    }
                    ItemMeta meta = clicked.getItemMeta();
                    meta.setLore(modifiedLore);
                    clicked.setItemMeta(meta);
                }

                if (clicked.getType() == Material.SKULL_ITEM && clicked.getData().getData() == 3) {
                    SkullMeta skullMeta = (SkullMeta) clicked.getItemMeta();
                    if (skullMeta != null) {
                        skullMeta.setDisplayName(modifiedDisplayName);
                        skullMeta.setOwner(player.getName());
                        clicked.setItemMeta(skullMeta);
                    }
                }

                if (commandMap.containsKey(modifiedDisplayName)) {
                    String command = commandMap.get(modifiedDisplayName);
                    player.performCommand(command);
                }
                return;
            }
        }

        if (!player.isOp() && event.getInventory().equals(player.getInventory())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot modify your inventory!");
        }
    }
}
