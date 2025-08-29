package com.omar.system64;

import com.omar.system64.achievements.AchievementManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AchievementCommandExecutor implements CommandExecutor {

    private final AchievementManager achievementManager;

    public AchievementCommandExecutor(Main plugin) {
        this.achievementManager = plugin.getAchievementManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /ach <subcommand>");
            return false;
        }

        if (args[0].equalsIgnoreCase("addlevel")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /ach addlevel <player> <level>");
                return false;
            }

            String playerName = args[1];
            int level;
            try {
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid level number.");
                return false;
            }

            // استدعاء اللاعب عبر الاسم
            Player target = sender.getServer().getPlayerExact(playerName);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player " + playerName + " is not online.");
                return false;
            }

            // إضافة المستوى عبر AchievementManager
            achievementManager.addLevel(target, level);

            sender.sendMessage(ChatColor.GREEN + "Level added successfully to " + playerName);
            target.sendMessage(ChatColor.GREEN + "Your level has been increased by " + level);
            return true;
        }

        return false;
    }
}
