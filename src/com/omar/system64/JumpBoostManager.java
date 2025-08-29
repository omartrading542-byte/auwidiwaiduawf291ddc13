package com.omar.system64;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class JumpBoostManager implements Listener {

    private final JavaPlugin plugin;
    private final Set<Player> canDoubleJump = new HashSet<>();

    public JumpBoostManager(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
            return;

        // إذا اللاعب على الأرض → نسمح له بتفعيل الطيران/دبل جمب
        if (player.isOnGround()) {
            player.setAllowFlight(true);
            canDoubleJump.add(player);
        }
    }

    @EventHandler
    public void onDoubleJump(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
            return;

        // إذا معه صلاحية autofly
        if (player.hasPermission("autofly")) {
            // إذا واقف (بدون حركة) → نخليه يطير طبيعي
            if (isStandingStill(player)) {
                return; // ما نلغي الحدث → يطير
            }
        }

        // إذا ما عنده دبل جمب جاهز → تجاهل
        if (!canDoubleJump.contains(player)) return;

        // نلغي الطيران ونطبق دبل جمب
        event.setCancelled(true);

        Vector direction = player.getLocation().getDirection().normalize().multiply(1.5);
        direction.setY(0.7);
        player.setVelocity(direction);

        player.setAllowFlight(false);
        canDoubleJump.remove(player);
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (!event.isSneaking()) return; // فقط عند الضغط للأسفل
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
            return;

        // إذا اللاعب بالهوا → نزلة سريعة
        if (!player.isOnGround()) {
            Vector down = player.getVelocity();
            down.setY(-2.0); // سرعة نزول قوية
            player.setVelocity(down);
        }
    }

    private boolean isStandingStill(Player player) {
        // التحقق إذا اللاعب ما عم يتحرك (سرعة تقريباً صفر)
        return player.getVelocity().lengthSquared() < 0.01;
    }
}
