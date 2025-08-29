package com.omar.system64;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class CommandOnJoinListener implements Listener {
    private Main plugin;
    private FileConfiguration levelConfig;

    public CommandOnJoinListener(Main plugin) {
        this.plugin = plugin;
        this.levelConfig = plugin.getConfig();  // هنا يمكنك استخدام getConfig مباشرة إذا كنت بحاجة للقراءة من ملف config.yml
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // التحقق مما إذا كانت الأوامر التلقائية مفعلة في leveledit.yml
        if (levelConfig.getBoolean("leveledit.autocommand.enable", false)) {
            List<String> commands = levelConfig.getStringList("leveledit.autocommand.commands");

            for (String command : commands) {
                // استبدال %player% باسم اللاعب
                command = command.replace("%player%", player.getName());

                // تنفيذ الأمر من الـ Console
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        }
    }
}
