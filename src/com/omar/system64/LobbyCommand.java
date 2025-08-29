package com.omar.system64;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LobbyCommand implements CommandExecutor {

    private Main plugin;

    public LobbyCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player!");
            return false;
        }

        Player player = (Player) sender;


        if (cmd.getName().equalsIgnoreCase("setspawn")) {
            if (player.hasPermission("system64.setspawn")) {
                Location spawnLocation = player.getLocation();
                plugin.getConfig().set("spawn.world", spawnLocation.getWorld().getName());
                plugin.getConfig().set("spawn.x", spawnLocation.getX());
                plugin.getConfig().set("spawn.y", spawnLocation.getY());
                plugin.getConfig().set("spawn.z", spawnLocation.getZ());
                plugin.getConfig().set("spawn.pitch", spawnLocation.getPitch());
                plugin.getConfig().set("spawn.yaw", spawnLocation.getYaw());
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "Spawn location set!");
            } else {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("setlobby")) {
            if (player.hasPermission("system64.setlobby")) {
                Location lobbyLocation = player.getLocation();
                plugin.getConfig().set("lobby.world", lobbyLocation.getWorld().getName());
                plugin.getConfig().set("lobby.x", lobbyLocation.getX());
                plugin.getConfig().set("lobby.y", lobbyLocation.getY());
                plugin.getConfig().set("lobby.z", lobbyLocation.getZ());
                plugin.getConfig().set("lobby.pitch", lobbyLocation.getPitch());
                plugin.getConfig().set("lobby.yaw", lobbyLocation.getYaw());
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "Lobby location set!");
            } else {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            }
            return true;
        }

        return false;
    }
}
