package com.omar.system64.holograms;

import com.omar.system64.holograms.HologramManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HologramCommand implements CommandExecutor {

    private final HologramManager hologramManager;

    public HologramCommand(HologramManager hologramManager) {
        this.hologramManager = hologramManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (hologramManager == null) {
            sender.sendMessage("HologramManager is not initialized.");
            return false;
        }

        // باقي الكود الخاص بالأمر


        Player player = (Player) sender;

        // تحقق من إذا كانت هناك وسائط للأمر
        if (args.length == 0) {
            player.sendMessage("Usage: /hologram <set|remove>");
            return true;
        }

        // إذا كان المستخدم يريد إنشاء الهولوغرام
        if (args[0].equalsIgnoreCase("set")) {
            Location location = player.getLocation(); // وضع الهولوغرام في موقع اللاعب
            hologramManager.updateTopLevelHologram(location); // تحديث الهولوغرام
            player.sendMessage("Top Level Hologram has been created at your location!");
        }
        // إذا كان المستخدم يريد إزالة الهولوغرام
        else if (args[0].equalsIgnoreCase("remove")) {
            hologramManager.removeHologram();
            player.sendMessage("Hologram has been removed!");
        }
        else {
            player.sendMessage("Usage: /hologram <set|remove>");
        }

        return true;
    }
}
