package com.omar.system64.coins;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class CoinsGUIListener implements Listener {
	
	 private final Coins coins;
	 
	 
    public CoinsGUIListener(Coins coins) {
        this.coins = coins;
    }
	
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();

        if (inv != null && inv.getTitle().equalsIgnoreCase("§6Coins Information")) {
            event.setCancelled(true);  // منع التعديل أو السحب
        }
    }
}
