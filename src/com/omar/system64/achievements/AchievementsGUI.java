package com.omar.system64.achievements;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AchievementsGUI implements Listener {
    private final Player player;
    private final AchievementManager manager;

    public AchievementsGUI(Player player, AchievementManager manager) {
        this.player = player;
        this.manager = manager;
        Bukkit.getPluginManager().registerEvents(this, manager.getPlugin());
    }

    // فتح نافذة الإنجازات
    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Achievements");

        // التأكد من أن كل الإنجازات تظهر في الـ Inventory
        for (AchievementManager.Achievement achievement : manager.getAllAchievements()) {
            boolean completed = manager.hasAchievement(player, achievement.getId()); // التحقق من إنجاز اللاعب
            int progress = manager.getProgress(player, achievement.getId()); // التقدم في الإنجاز
            int goal = achievement.getGoal(); // الهدف

            // اختيار نوع المادة بناءً على الإنجاز المكتمل
            ItemStack item = new ItemStack(completed ? Material.DIAMOND : Material.COAL);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName((completed ? ChatColor.GREEN : ChatColor.GRAY) + achievement.getDescription()); // اسم الإنجاز

            // إضافة معلومات إضافية حول الإنجاز
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + "Progress: " + ChatColor.WHITE + progress + "/" + goal);
            lore.add(ChatColor.AQUA + "Points: " + achievement.getPoints());

            // إضافة العلامة التي تدل على الإنجاز المكتمل
            if (completed) {
                lore.add(ChatColor.GREEN + "✔ Completed");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.addItem(item);
        }

        player.openInventory(inv); // فتح الـ Inventory للاعب
    }

    // التعامل مع النقر على العناصر في الـ Inventory
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // التأكد من أن هذا هو الـ Inventory الخاص بالإنجازات
        if (event.getView().getTitle().equals(ChatColor.DARK_PURPLE + "Achievements")) {
            event.setCancelled(true); // منع التفاعل مع العناصر

            // التفاعل مع العناصر عند النقر عليها (اختياري)
            if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta()) {
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String itemName = meta.getDisplayName();

                    // على سبيل المثال، إذا كان اللاعب قد أكمل الإنجاز
                    if (itemName.contains("Completed")) {
                        player.sendMessage(ChatColor.GREEN + "You have already completed this achievement!");
                    } else {
                        player.sendMessage(ChatColor.RED + "You haven't completed this achievement yet!");
                    }
                }
            }
        }
    }
}
