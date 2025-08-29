package com.omar.system64;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.File;

public class SetRankCommand implements CommandExecutor {

    private Main plugin;
    private Ranks ranks;

    public SetRankCommand(Main plugin) {
        this.plugin = plugin;

        File ranksFile = new File(plugin.getDataFolder(), "playersrank.yml");
        this.ranks = new Ranks(plugin, ranksFile);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(color("&cUsage: /setrank <player> <rank>"));
            return false;
        }

        String playerName = args[0];
        String rank = args[1];

        // التحقق من أن المرسل هو OP أو الـ Console
        if (sender instanceof Player) {
            if (!sender.hasPermission("system64.setrank")) {
                sender.sendMessage(color("&cYou do not have permission to use this command."));
                return false;
            }
        } else if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(color("&cThis command can only be used by the console."));
            return false;
        }

        Player player = plugin.getServer().getPlayer(playerName);
        if (player != null) {
            // التحقق من صحة الرتبة
            if (rank != null && !rank.isEmpty()) {
                ranks.setRank(player, rank);
                String suffix = ranks.getSuffix(rank);
                player.sendMessage(color("&7Your rank has been set to: &f" + suffix));
                sender.sendMessage(color("&7You have set the rank of &f" + playerName + " &ato &f" + suffix));
            } else {
                sender.sendMessage(color("&cInvalid rank specified."));
            }
        } else {
            sender.sendMessage(color("&cPlayer " + playerName + " not found."));
        }

        return true;
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
