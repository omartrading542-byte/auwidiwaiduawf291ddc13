package com.omar.system64.coins;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CoinsGUICommand implements CommandExecutor {

    private final Coins coins;

    public CoinsGUICommand(Coins coins) {
        this.coins = coins;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        openCoinsGUI(player);
        return true;
    }

    private void openCoinsGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Coins Information");

        // الرصيد الحالي مع التنسيق
        long coinsAmount = coins.getCoins(player.getUniqueId());
        String formattedCoins = formatNumberWithDots(coinsAmount);

        ItemStack balanceItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta balanceMeta = balanceItem.getItemMeta();
        balanceMeta.setDisplayName(ChatColor.YELLOW + "Your Coins: " + ChatColor.GREEN + formattedCoins);
        balanceItem.setItemMeta(balanceMeta);

        // سجل العمليات
        ItemStack historyItem = new ItemStack(Material.PAPER);
        ItemMeta historyMeta = historyItem.getItemMeta();
        historyMeta.setDisplayName(ChatColor.AQUA + "Transaction History");

        List<String> logs = coins.getRecentTransactions(player.getUniqueId(), 10); // آخر 10 عمليات
        if (logs.isEmpty()) {
            logs.add(ChatColor.GRAY + "No transactions found.");
        }
        historyMeta.setLore(logs);
        historyItem.setItemMeta(historyMeta);

        inv.setItem(11, balanceItem);
        inv.setItem(15, historyItem);

        player.openInventory(inv);
    }

    // دالة تنسيق الأرقام بفاصل آلاف نقطة (.)
    private String formatNumberWithDots(long number) {
        // نستخدم Locale.GERMANY حيث يفصل الآلاف بنقطة
        NumberFormat nf = NumberFormat.getInstance(Locale.GERMANY);
        return nf.format(number);
    }
}
