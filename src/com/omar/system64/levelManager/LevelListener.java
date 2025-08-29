package com.omar.system64.levelManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class LevelListener implements Listener {

    private final LevelManager levelManager;

    public LevelListener(LevelManager levelManager) {
        this.levelManager = levelManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!levelManager.isEnabled()) return;

        updateLevelBar(p);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (!levelManager.isEnabled()) return;

        // نحتاج نستخدم delay بسيط لأن البار ينعاد تلقائيًا وقت الريسباون
        Bukkit.getScheduler().runTaskLater(levelManager.getPlugin(), () -> {
            updateLevelBar(p);
        }, 2L); // بعد 2 tick
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (p.getOpenInventory().getTitle().equals("§aYour Level Info")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (p.getOpenInventory().getTitle().equals("§aYour Level Info")) {
            e.setCancelled(true);
        }
    }

    private void updateLevelBar(Player p) {
        int level = levelManager.getLevel(p.getUniqueId());
        int xp = levelManager.getXP(p.getUniqueId());
        long next = levelManager.getXPNeededForLevel(level);  

        p.setLevel(level);

        // حساب النسبة بين XP الحالي و XP المطلوب للوصول للمستوى التالي
        float progress = next > 0 ? Math.min(1f, (float) xp / next) : 0f;

        // عرض التقدم في شريط الـ XP بناءً على النسبة المحسوبة
        p.setExp(progress);
    }


}
