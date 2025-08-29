package com.omar.system64;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class GuildCommand implements CommandExecutor {

    private final GuildManager guildManager;
    private final GuildChatListener guildChatListener;

    public GuildCommand(GuildManager guildManager, GuildChatListener guildChatListener) {
        this.guildManager = guildManager;
        this.guildChatListener = guildChatListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "❌ Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "create":
                handleCreateCommand(player, args);
                break;

            case "settopguild":
                handleSetTopGuildCommand(player);
                break;

            case "leave":
                if (guildManager.leaveGuild(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "🏃 You left the guild.");
                } else {
                    player.sendMessage(ChatColor.RED + "❌ You cannot leave your guild as the owner!");
                }
                break;

            case "invite":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "❌ Usage: /guild invite <player>");
                    break;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "❌ Player not found.");
                    break;
                }
                if (guildManager.sendGuildInvite(player.getUniqueId(), target.getUniqueId())) {
                    player.sendMessage(ChatColor.GREEN + "📩 Invite sent to " + target.getName());
                } else {
                    player.sendMessage(ChatColor.RED + "❌ You don't have permission to invite.");
                }
                break;

            case "chat":
                boolean enabled = guildChatListener.toggleGuildChat(player.getUniqueId());
                if (enabled) {
                    player.sendMessage(ChatColor.GREEN + "✅ Guild Chat mode enabled. All your messages will go to guild chat.");
                } else {
                    player.sendMessage(ChatColor.RED + "❌ Guild Chat mode disabled. Your messages will go to public chat.");
                }
                break;

            case "kick":
                player.sendMessage(ChatColor.RED + "🚪 This feature will remove a player from your guild (coming soon).");
                break;

            case "list":
                handleGuildList(player);
                break;

            default:
                player.sendMessage(ChatColor.RED + "❌ Unknown subcommand. Use /guild for help.");
                break;
        }

        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.YELLOW + "---------------- " + ChatColor.AQUA + "Guild Commands" + ChatColor.YELLOW + " ----------------");
        player.sendMessage(ChatColor.GOLD + "/guild create <name> <color>" + ChatColor.AQUA + " - Create a new guild");
        player.sendMessage(ChatColor.GOLD + "/guild settopguild" + ChatColor.AQUA + " - Display top guild hologram");
        player.sendMessage(ChatColor.GOLD + "/guild leave" + ChatColor.AQUA + " - Leave your current guild");
        player.sendMessage(ChatColor.GOLD + "/guild invite <player>" + ChatColor.AQUA + " - Invite a player to your guild");
        player.sendMessage(ChatColor.GOLD + "/guild chat" + ChatColor.AQUA + " - Toggle guild chat mode");
        player.sendMessage(ChatColor.GOLD + "/guild list" + ChatColor.AQUA + " - Show all guild members and their status");
        player.sendMessage(ChatColor.YELLOW + "---------------------------------------------------");
    }

    private void handleCreateCommand(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "❌ Usage: /guild create <name> <color>");
            return;
        }
        String name = args[1];
        String color = args[2];
        guildManager.createGuild(player, name, color);
    }

    private void handleSetTopGuildCommand(Player player) {
        Location location = player.getLocation().add(0, 2, 0);
        guildManager.setTopGuildHologram(location);
        player.sendMessage(ChatColor.GREEN + "✅ Displaying top 10 guilds in a hologram.");
    }

    private void handleGuildList(Player player) {
        Map<String, String> membersStatus = guildManager.getGuildMembersStatus(player.getUniqueId());
        if (membersStatus.isEmpty()) {
            player.sendMessage(ChatColor.RED + "❌ You are not in a guild.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "=== Guild Members ===");
        membersStatus.forEach((name, status) ->
                player.sendMessage(ChatColor.AQUA + name + ChatColor.GRAY + ": " + status));
    }
}
