package com.omar.system64.coins;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.ChatColor;

public class LuckyGameListener implements Listener {

    private final LuckyGame luckyGame;

    public LuckyGameListener(LuckyGame luckyGame) {
        this.luckyGame = luckyGame;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.GOLD + "Lucky Game - Choose your items")) {
            event.setCancelled(true); // يمنع السحب أو تحريك العناصر

            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player) event.getWhoClicked();

            int slot = event.getRawSlot();
            if (slot >= 0 && slot < 27) { // نفس حجم GUI
                luckyGame.handlePlayerChoice(player, slot);
            }
        }
    }

}
