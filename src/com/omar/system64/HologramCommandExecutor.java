package com.omar.system64;

import com.omar.system64.achievements.AchievementManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HologramCommandExecutor implements CommandExecutor {

    private final Main plugin;

    public HologramCommandExecutor(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player.");
            return false;
        }

        Player player = (Player) sender;

        // تحقق من الصلاحية
        if (!player.hasPermission("system64.pholoset")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        // اجلب موقع اللاعب مباشرة (من غير ما ينزل على الأرض)
        Location loc = player.getLocation();

        plugin.getAchievementManager().setHologramLocation(player, loc);

        player.sendMessage(ChatColor.GREEN + "Hologram location set to: "
                + loc.getBlockX() + ", "
                + loc.getBlockY() + ", "
                + loc.getBlockZ());

        return true;
    }
}
