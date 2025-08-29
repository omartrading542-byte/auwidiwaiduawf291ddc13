package com.omar.system64.coins;

import com.omar.system64.Ranks;
import com.omar.system64.levelManager.LevelManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class BuyCommand implements CommandExecutor, Listener {

    private final Coins coinsManager;
    private final Ranks ranksManager;
    private final LevelManager levelManager;
    private final JavaPlugin plugin;
    private final Connection mysqlConnection;

    private static final int GUI_SIZE = 54;
    private static final String GUI_TITLE = ChatColor.GOLD + "Shop - Buy Items";
    private Inventory shopInventory;

    private final File playerDataFile;
    private final FileConfiguration playerDataConfig;

    // ترتيب الرتب
    private static final Map<String, Integer> rankOrder = new HashMap<>();
    static {
        rankOrder.put("vip", 1);
        rankOrder.put("vip+", 2);
        rankOrder.put("mvp", 3);
        rankOrder.put("mvp+", 4);
        rankOrder.put("mvp++", 5);
    }

    public BuyCommand(JavaPlugin plugin, Coins coinsManager, Ranks ranksManager, LevelManager levelManager, Connection mysqlConnection) {
        this.plugin = plugin;
        this.coinsManager = coinsManager;
        this.ranksManager = ranksManager;
        this.levelManager = levelManager;
        this.mysqlConnection = mysqlConnection;

        Bukkit.getPluginManager().registerEvents(this, plugin);
        createShopInventory();

        playerDataFile = new File(plugin.getDataFolder(), "store.yml");
        if (!playerDataFile.exists()) {
            try {
                playerDataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
    }

    private void createShopInventory() {
        shopInventory = Bukkit.createInventory(null, GUI_SIZE, GUI_TITLE);
        shopInventory.setItem(10, createRankItem("VIP", Material.EMERALD, ChatColor.GREEN, 75000));
        shopInventory.setItem(12, createRankItem("VIP+", Material.DIAMOND, ChatColor.GREEN, 120000));
        shopInventory.setItem(14, createRankItem("MVP", Material.NETHER_STAR, ChatColor.AQUA, 180000));
        shopInventory.setItem(16, createRankItem("MVP+", Material.BEACON, ChatColor.AQUA, 225000));
        shopInventory.setItem(31, createRankItem("MVP++", Material.DRAGON_EGG, ChatColor.GOLD, 375000));
    }

    private ItemStack createRankItem(String rankName, Material icon, ChatColor color, int price) {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color + rankName + " Rank");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Buy the " + rankName + " rank for "+ ChatColor.GOLD + price + ChatColor.GRAY +" coins",
                    ChatColor.GRAY + "Upgrades your current rank",
                    ChatColor.GRAY + "",
                    ChatColor.YELLOW + "Click to Play"

            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            ((Player) sender).openInventory(shopInventory);
            return true;
        }
        sender.sendMessage(ChatColor.RED + "Only players can use this command.");
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String rawName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        String cleanName = rawName.replace(" Rank", "").trim().toLowerCase();

        int cost = getCost(cleanName);
        if (cost <= 0) {
            player.sendMessage(ChatColor.RED + "This item is not available for purchase.");
            return;
        }

        // تحقق من الرتبة الحالية
        String currentRank = ranksManager.getRank(player).toLowerCase();
        int currentRankLevel = rankOrder.getOrDefault(currentRank, 0);
        int newRankLevel = rankOrder.get(cleanName);

        if (newRankLevel <= currentRankLevel) {
            player.sendMessage(ChatColor.RED + "You cannot buy a lower or equal rank than your current one.");
            return;
        }

        long playerCoins = coinsManager.getCoins(player.getUniqueId());
        if (playerCoins < cost) {
            player.sendMessage(ChatColor.RED + "You don't have enough coins for " + cleanName + ".");
            return;
        }

        coinsManager.removeCoins(player.getUniqueId(), cost);

        ranksManager.setRank(player, cleanName);
        savePurchasedRank(player.getUniqueId(), cleanName);

        player.sendMessage(ChatColor.GREEN + "You bought rank " + cleanName.toUpperCase() + "!");
        int xpToAdd = (int) (cost * 0.1);
        levelManager.addXP(player.getUniqueId(), xpToAdd);
        player.sendMessage(ChatColor.AQUA + "You gained " + xpToAdd + " XP for your purchase!");
        player.closeInventory();
    }

    private void savePurchasedRank(UUID uuid, String rankName) {
        if (mysqlConnection != null) {
            try {
                PreparedStatement ps = mysqlConnection.prepareStatement(
                        "INSERT INTO purchased_ranks (uuid, rank) VALUES (?, ?) ON DUPLICATE KEY UPDATE rank=?");
                ps.setString(1, uuid.toString());
                ps.setString(2, rankName);
                ps.setString(3, rankName);
                ps.executeUpdate();
                ps.close();
                return;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        playerDataConfig.set(uuid.toString(), rankName);
        try {
            playerDataConfig.save(playerDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getCost(String name) {
        switch (name.toLowerCase()) {
            case "vip": return 75000;
            case "vip+": return 120000;
            case "mvp": return 180000;
            case "mvp+": return 225000;
            case "mvp++": return 375000;
            default: return 0;
        }
    }
}
