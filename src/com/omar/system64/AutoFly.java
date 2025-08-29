package com.omar.system64;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class AutoFly implements Listener {

    private final Main plugin;
    private final Ranks ranks;

    public AutoFly(Main plugin) {
        this.plugin = plugin;
        this.ranks = plugin.getRanks();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String displayName = ranks.getDisplayName(player); // يعرض الاسم مع الرتبة
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.hasPermission("autofly")) {
                enableAutoFly(player);
            } else {
                disableAutoFly(player);

            }
        }, 20L);
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("autofly")) {
            event.setCancelled(true);
            disableAutoFly(player);

        }
    }

    public void toggleFly(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                toggleFlyFor(player, sender, ranks); // نمرر ranks
            } else {
                sender.sendMessage(ChatColor.RED + "Only players can toggle their own fly mode.");
            }
        } else if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return;
            }

            toggleFlyFor(target, sender, ranks); // نمرر ranks
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /fly [player]");
        }
    }

    // نمرر ranks لهذه الدالة
    public static void toggleFlyFor(Player player, CommandSender sender, Ranks ranks) {
        boolean enable = !player.getAllowFlight();
        String nameWithRank = ranks.getDisplayName(player); // مثال: [VIP] Omar

        if (!player.hasPermission("autofly")) {
            sender.sendMessage(ChatColor.RED +  " does not have permission to use fly.");
            return;
        }

        if (enable) {
            enableAutoFly(player);
            sender.sendMessage(ChatColor.GREEN + "Enabled fly mode");
            if (!player.equals(sender)) {
                player.sendMessage(ChatColor.GREEN + "Your fly mode was enabled.");
            }
        } else {
            disableAutoFly(player);
            sender.sendMessage(ChatColor.RED + "Disabled fly mode");
            if (!player.equals(sender)) {
                player.sendMessage(ChatColor.RED + "Your fly mode was disabled.");
            }
        }
    }

    private static void enableAutoFly(Player player) {
        player.setAllowFlight(true);
        player.setFlying(true);
    }

    private static void disableAutoFly(Player player) {
        player.setFlying(false);
        player.setAllowFlight(false);
    }
}
