package com.omar.system64.coins;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopManager {

    private final Coins coinsManager;

    // Store last purchased item per player UUID (optional, to show in GUI)
    private final Map<UUID, ItemStack> lastPurchasedItem = new HashMap<>();

    // Products and their prices
    private final Map<Material, Long> shopItemsPrices = new HashMap<>();

    public ShopManager(Coins coinsManager) {
        this.coinsManager = coinsManager;

        // Add shop items and prices here
        shopItemsPrices.put(Material.DIAMOND_SWORD, 500L);
        shopItemsPrices.put(Material.IRON_SWORD, 200L);
        shopItemsPrices.put(Material.GOLDEN_APPLE, 150L);
        shopItemsPrices.put(Material.GOLDEN_APPLE, 2000L);
    }

    public boolean buyItem(Player player, Material material) {
        if (!shopItemsPrices.containsKey(material)) {
            player.sendMessage(ChatColor.RED + "This item is not available in the shop.");
            return false;
        }

        long price = shopItemsPrices.get(material);
        long playerCoins = coinsManager.getCoins(player.getUniqueId());

        if (playerCoins < price) {
            player.sendMessage(ChatColor.RED + "You don't have enough coins. You need " + price + " coins to buy this item.");
            return false;
        }

        boolean removed = coinsManager.removeCoins(player.getUniqueId(), price);
        if (!removed) {
            player.sendMessage(ChatColor.RED + "An error occurred while deducting coins.");
            return false;
        }

        ItemStack item = new ItemStack(material, 1);
        player.getInventory().addItem(item);
        player.sendMessage(ChatColor.GREEN + "You have purchased " + material.name() + " for " + price + " coins.");

        // Save last purchased item for GUI display
        lastPurchasedItem.put(player.getUniqueId(), item);

        // Open GUI to show purchased item
        openShopGUI(player, item);

        return true;
    }

    public void openShopGUI(Player player, ItemStack item) {
        ShopGUI.open(player, item);
    }

    public Map<Material, Long> getShopItemsPrices() {
        return shopItemsPrices;
    }
}
