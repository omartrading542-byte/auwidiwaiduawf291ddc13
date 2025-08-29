package com.omar.system64.coins;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopGUI implements Listener {

    private static final String GUI_TITLE = ChatColor.GOLD + "Purchased Item";

    public static void open(Player player, ItemStack item) {
        Inventory inv = Bukkit.createInventory(null, 9, GUI_TITLE);

        ItemStack itemCopy = item.clone();

        // Add custom lore to explain it's non-removable
        ItemMeta meta = itemCopy.getItemMeta();
        if (meta != null) {
            meta.setLore(java.util.Arrays.asList(ChatColor.RED + ""));
            itemCopy.setItemMeta(meta);
        }

        inv.setItem(4, itemCopy); // place item in middle slot

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(GUI_TITLE)) {
            event.setCancelled(true); // prevent any clicks in GUI
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().equals(GUI_TITLE)) {
            event.setCancelled(true); // prevent drag in GUI
        }
    }
}
