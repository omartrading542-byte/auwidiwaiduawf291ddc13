package com.omar.system64.levelManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;

import java.util.Arrays;

public class LevelGUI {

	public static void open(Player player, LevelManager levelManager) {
	    int level = levelManager.getLevel(player.getUniqueId());
	    int xp = levelManager.getXP(player.getUniqueId());
	    long currentLevelXP = levelManager.getXPNeededForLevel(level);
	    long nextLevelXP = levelManager.getXPNeededForLevel(level + 1);
	    long remainingForNextLevel = nextLevelXP - xp;

	    // جلب اللون + الإيموجي (مع رموز الألوان &)
	    String levelColorEmojiCode = levelManager.getLevelColorAndEmoji(level);

	    // ترجمة رموز الألوان & إلى ألوان حقيقية مع ترك الإيموجي كنص
	    String levelColorEmoji = ChatColor.translateAlternateColorCodes('&', levelColorEmojiCode);

	    // بناء اسم العنصر مع الإيموجي واللون والمستوى
	    String levelDisplayName = levelColorEmoji + " Level: " + ChatColor.WHITE + level;

	    Inventory gui = Bukkit.createInventory(null, 27, "§aYour Level Info");

	    // Level item
	    ItemStack levelItem = new ItemStack(Material.EXP_BOTTLE);
	    ItemMeta levelMeta = levelItem.getItemMeta();
	    levelMeta.setDisplayName(levelDisplayName);
	    levelMeta.setLore(Arrays.asList(
	        "§7Current Level: " + levelColorEmoji + level + ChatColor.RESET,
	        "§7Next Level: §a" + (level + 1)
	    ));
	    levelItem.setItemMeta(levelMeta);

	    // XP item
	    ItemStack xpItem = new ItemStack(Material.EMERALD);
	    ItemMeta xpMeta = xpItem.getItemMeta();
	    xpMeta.setDisplayName("§aXP: §f" + xp + " / " + nextLevelXP);
	    xpMeta.setLore(Arrays.asList("§7XP to next level: §f" + remainingForNextLevel));
	    xpItem.setItemMeta(xpMeta);

	    gui.setItem(11, levelItem);
	    gui.setItem(15, xpItem);

	    player.openInventory(gui);
	}
}