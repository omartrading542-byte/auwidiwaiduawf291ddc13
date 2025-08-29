package com.omar.system64.coins;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class CoinsCommand implements CommandExecutor {

    private final Coins coins;

    public CoinsCommand(Coins coins) {
        this.coins = coins;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!sender.hasPermission("coins.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /coins <add|remove|set|info> <player> [amount]");
            return true;
        }

        String action = args[0].toLowerCase();

        if (action.equals("info")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /coins info <player>");
                return true;
            }
            String targetName = args[1];
            // يمكن اللاعب يكون أونلاين أو أوفلاين
            UUID targetUUID = null;
            Player targetPlayer = Bukkit.getPlayer(targetName);
            if (targetPlayer != null) {
                targetUUID = targetPlayer.getUniqueId();
            } else {
                // Offline player
                try {
                    targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
            }

            if (targetUUID == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }

            long balance = coins.getCoins(targetUUID);
            sender.sendMessage(ChatColor.GOLD + "===== Coins Info for " + targetName + " =====");
            sender.sendMessage(ChatColor.YELLOW + "Balance: " + ChatColor.GREEN + balance + " coins");
            sender.sendMessage(ChatColor.YELLOW + "Recent Transactions:");

            List<String> transactions = coins.getRecentTransactions(targetUUID, 10);
            if (transactions.isEmpty()) {
                sender.sendMessage(ChatColor.GRAY + "No transactions found.");
            } else {
                for (String log : transactions) {
                    sender.sendMessage(ChatColor.GRAY + log);
                }
            }
            return true;
        }

        // باقي الأوامر: add, remove, set
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /coins <add|remove|set> <player> <amount>");
            return true;
        }

        String targetName = args[1];
        String amountStr = args[2];

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found or offline.");
            return true;
        }

        long amount;
        try {
            amount = Long.parseLong(amountStr);
            if (amount < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Please enter a valid positive number for amount.");
            return true;
        }

        UUID targetUUID = target.getUniqueId();

        switch (action) {
            case "add":
                coins.addCoins(targetUUID, amount);
                sender.sendMessage(ChatColor.GREEN + "Added " + amount + " coins to " + target.getName());
                target.sendMessage(ChatColor.GREEN + "You have received " + amount + " coins!");
                break;
            case "remove":
                boolean success = coins.removeCoins(targetUUID, amount);
                if (success) {
                    sender.sendMessage(ChatColor.GREEN + "Removed " + amount + " coins from " + target.getName());
                    target.sendMessage(ChatColor.RED + "" + amount + " coins have been deducted from your balance.");
                } else {
                    sender.sendMessage(ChatColor.RED + "Player does not have enough coins.");
                }
                break;
            case "set":
                coins.setCoins(targetUUID, amount);
                sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s coins to " + amount);
                target.sendMessage(ChatColor.GREEN + "Your coins balance has been set to " + amount);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown action. Use add, remove, set or info.");
                break;
        }

        return true;
    }
}
