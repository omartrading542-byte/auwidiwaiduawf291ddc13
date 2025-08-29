package com.omar.system64.achievements;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AchievementsCommand implements CommandExecutor {

    private final AchievementManager manager;

    public AchievementsCommand(AchievementManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "❌ Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        AchievementsGUI gui = new AchievementsGUI(player, manager);
        gui.open();
        return true;
    }
}
