package com.omar.system64.commands;

import com.omar.system64.LightningZoneManager;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class SetGGCommand implements CommandExecutor {

    private final LightningZoneManager manager;

    public SetGGCommand(LightningZoneManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        manager.giveSelector(player);
        return true;
    }
}

