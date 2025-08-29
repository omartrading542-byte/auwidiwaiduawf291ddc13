package com.omar.system64;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

public class JoinListener implements Listener {

    private Main plugin;
    private final Ranks ranks;

    public JoinListener(Main plugin,Ranks ranks) {
        this.plugin = plugin;
        this.ranks = ranks;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // رسالة ترحيب

        event.setJoinMessage(null);

        // التحقق من صلاحيات اللاعب
        if (player.hasPermission("mvp++")) {
            // إذا كان لدى اللاعب صلاحية mvp++
            event.setJoinMessage(ChatColor.GOLD + player.getName() + " joined the lobby!");
        } else if (player.hasPermission("mvp+")) {
            // إذا كان لدى اللاعب صلاحية vipmsg
            event.setJoinMessage(ChatColor.AQUA + player.getName() + " joined the lobby!");
        }
        // قلب واحد فقط
        player.setMaxHealth(20);
        player.setHealth(20);
        player.setHealthScale(20);
        player.setHealthScaled(true);
        ranks.applyPermissions(player);
        ranks.applyExtraPermissions(player);
        ranks.setChatPrefix(player);

        // الجوع دائماً كامل
        player.setFoodLevel(20);
        player.setSaturation(20f);
    }




    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        // نمنع تغيير مستوى الجوع
        event.setCancelled(true);

        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            player.setFoodLevel(20);
            player.setSaturation(20f);
        }
    }
}
