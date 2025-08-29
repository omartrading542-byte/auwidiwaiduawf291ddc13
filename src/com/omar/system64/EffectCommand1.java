package com.omar.system64;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class EffectCommand1 implements CommandExecutor {

    private final JavaPlugin plugin;
    private final Map<Player, BukkitTask> playerEffects = new HashMap<>();

    public EffectCommand1(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command is only for players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§eUsage: /effect <effect_name>");
            return true;
        }

        String effectName = args[0].toUpperCase();

        try {
            Effect effect = Effect.valueOf(effectName);

            // Cancel previous effect task if exists
            if (playerEffects.containsKey(player)) {
                playerEffects.get(player).cancel();
            }

            player.sendMessage("§aEffect applied continuously: " + effectName);

            // Start new repeating task to keep effect around the player
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) {
                        cancel(); // stop task if player leaves
                        playerEffects.remove(player);
                        return;
                    }

                    Location baseLocation = player.getLocation().clone().subtract(0, 1, 0);

                    // Effect below player
                    player.getWorld().playEffect(baseLocation, effect, 1);

                    // Effect in circle around player
                    double radius = 1.0;
                    int points = 8;
                    for (int i = 0; i < points; i++) {
                        double angle = 2 * Math.PI * i / points;
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        Location circleLocation = baseLocation.clone().add(x, 0, z);
                        player.getWorld().playEffect(circleLocation, effect, 1);
                    }
                }
            }.runTaskTimer(plugin, 0L, 10L); // repeat every 10 ticks (~0.5 sec)

            // Store the task for the player
            playerEffects.put(player, task);

        } catch (IllegalArgumentException e) {
            player.sendMessage("§cEffect not found!");
        }

        return true;
    }
}
