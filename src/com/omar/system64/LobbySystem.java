package com.omar.system64;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class LobbySystem {

    private Main plugin;

    public LobbySystem(Main plugin) {
        this.plugin = plugin;
    }

    public void teleportToLobby(Player player) {
        String worldName = plugin.getConfig().getString("lobby.world");
        double x = plugin.getConfig().getDouble("lobby.x");
        double y = plugin.getConfig().getDouble("lobby.y");
        double z = plugin.getConfig().getDouble("lobby.z");

        Location lobbyLocation = new Location(Bukkit.getWorld(worldName), x, y, z);
        player.teleport(lobbyLocation);
    }
}
