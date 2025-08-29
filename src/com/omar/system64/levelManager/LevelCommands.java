package com.omar.system64.levelManager;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LevelCommands implements CommandExecutor {
    private final LevelManager levelManager;

    public LevelCommands(LevelManager levelManager) {
        this.levelManager = levelManager;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!this.levelManager.isEnabled()) {
            sender.sendMessage("system is disabled.");
            return true;
        }
        if (args.length == 0 && sender instanceof Player) {
            Player player = (Player)sender;
            LevelGUI.open(player, this.levelManager);
            return true;
        }
        if (args.length >= 2) {
            if (!sender.isOp() && !(sender instanceof org.bukkit.command.ConsoleCommandSender)) {
                sender.sendMessage("don't have permission.");
                return true;
            }
            String sub = command.getName().toLowerCase();
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("not found.");
                return true;
            }
            try {
                int value = Integer.parseInt(args[1]);
                switch (sub) {
                    case "setlevel":
                        this.levelManager.setLevel(target.getUniqueId(), value);
                        sender.sendMessage("level of " + target.getName() + " to " + value);
                        break;
                    case "addlevel":
                        this.levelManager.addLevel(target.getUniqueId(), value);
                        sender.sendMessage("" + value + " levels to " + target.getName());
                        break;
                    case "setxp":
                        this.levelManager.setXP(target.getUniqueId(), value);
                        sender.sendMessage("XP of " + target.getName() + " to " + value);
                        break;
                    case "addxp":
                        this.levelManager.addXP(target.getUniqueId(), value);
                        sender.sendMessage("" + value + " XP to " + target.getName());
                        break;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("number.");
            }
            return true;
        }
        sender.sendMessage("");
                sender.sendMessage("<player> <level>");
        sender.sendMessage("<player> <amount>");
        sender.sendMessage("<player> <amount>");
        sender.sendMessage("<player> <amount>");
        sender.sendMessage("list");
        return true;
    }
}
