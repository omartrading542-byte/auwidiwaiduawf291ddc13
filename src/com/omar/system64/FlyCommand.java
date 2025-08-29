package com.omar.system64;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {

    private final Ranks ranks;

    public FlyCommand(Main plugin) {
        this.ranks = plugin.getRanks();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // التحقق من أن المرسل هو لاعب وأن لديه صلاحيات لتنفيذ الأمر
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
            return true;
        }

        Player player = (Player) sender;

        // إذا لم يتم تحديد أي لاعب، فسنطبق الأمر على اللاعب نفسه
        if (args.length == 0) {
            AutoFly.toggleFlyFor(player, sender, ranks);
        }
        // إذا تم تحديد اسم لاعب
        else if (args.length == 1) {
            // إذا كان اللاعب لا يمتلك صلاحية OP أو لا يكون نفس اللاعب
            if (!player.hasPermission("fly.others") && !player.isOp()) {
                player.sendMessage(ChatColor.RED + "You do not have permission to fly other players.");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }

            AutoFly.toggleFlyFor(target, sender, ranks);
        } else {
            player.sendMessage(ChatColor.RED + "Usage: /fly [player]");
        }

        return true;
    }
}
