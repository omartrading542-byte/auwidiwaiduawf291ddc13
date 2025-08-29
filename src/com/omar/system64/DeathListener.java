package com.omar.system64;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.entity.Player;

public class DeathListener implements Listener {

    private final Main plugin;

    public DeathListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // يمنع دروب الأدوات
        event.setKeepInventory(true);

        // إلغاء رسالة الموت
        event.setDeathMessage(null);

        // إرجاع الصحة بعد الموت بدون عرض شاشة "You Died"
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player player = event.getEntity();
            player.spigot().respawn(); // يجبره يرجع تلقائي بدون انتظار
        }, 2L); // تأخير بسيط حتى يتمكن من العودة
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // مكان السبون من ملف config أو مكان ثابت
        Location spawn = plugin.getSpawnLocation(); // تأكد أن عندك هذه الدالة في Main
        if (spawn != null) {
            event.setRespawnLocation(spawn);
        }
    }
}
