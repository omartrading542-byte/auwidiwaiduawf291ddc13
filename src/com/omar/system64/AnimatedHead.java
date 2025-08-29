package com.omar.system64;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.omar.system64.bungee.BungeeListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AnimatedHead implements Listener {

    private final Main plugin;
    private final File headsFile;
    private final FileConfiguration headsConfig;
    private final Set<HeadData> heads = new HashSet<>();
    private final Map<String, Integer> serverCounts = new HashMap<>();

    public AnimatedHead(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord",
                new BungeeListener(serverCounts));

        headsFile = new File(plugin.getDataFolder(), "heads.yml");
        if (!headsFile.exists()) {
            headsFile.getParentFile().mkdirs();
            try { headsFile.createNewFile(); } catch (Exception e) { e.printStackTrace(); }
        }
        headsConfig = YamlConfiguration.loadConfiguration(headsFile);

        loadHeads();

        new BukkitRunnable() {
            @Override
            public void run() {
                requestPlayerCount("ALL");
                for (String server : new HashSet<>(serverCounts.keySet())) {
                    requestPlayerCount(server);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L * 5);
    }

    public String getDefaultTexture() {
        return "62c17c70e6acaa5399ae986696185b849adbde33fc14e2e3a84081278cf66377";
    }

    public List<String> getDefaultHologram(String serverName) {
        return Arrays.asList(
                "&e&lCLICK TO JOIN",
                "",
                "&b&lPlayer: &f&l{server_" + serverName + "}",
                "",
                "&c&lSoon!"
        );
    }

    public void createHeadWithHologram(Player player, String textureId, List<String> hologramText, String effectName, String serverName, String command) {
        createHeadWithHologram(player, textureId, hologramText, effectName, serverName, command, player.getLocation().add(0,1,0));
    }

    public void createHeadWithHologram(Player player, String textureId, List<String> hologramText, String effectName, String serverName, String command, Location loc) {
        int id = headsConfig.getKeys(false).size() + 1;
        headsConfig.set(id + ".world", loc.getWorld().getName());
        headsConfig.set(id + ".x", loc.getX());
        headsConfig.set(id + ".y", loc.getY());
        headsConfig.set(id + ".z", loc.getZ());
        headsConfig.set(id + ".textureId", textureId);
        headsConfig.set(id + ".hologramLines", hologramText);
        headsConfig.set(id + ".effectName", effectName);
        headsConfig.set(id + ".serverName", serverName);
        headsConfig.set(id + ".command", command);
        saveHeads();

        HeadData headData = createHeadAtLocation(String.valueOf(id), loc, textureId, hologramText, serverName, command);
        heads.add(headData);

        if (serverName != null && !serverName.isEmpty()) {
            serverCounts.putIfAbsent(serverName, 0);
            requestPlayerCount(serverName);
        }
        requestPlayerCount("ALL");
    }

    private HeadData createHeadAtLocation(String headKey, Location loc, String textureId, List<String> hologramLines, String serverName, String command) {
        ItemStack headItem = getCustomSkull(textureId);

        ArmorStand headStand = loc.getWorld().spawn(loc, ArmorStand.class);
        headStand.setVisible(false);
        headStand.setGravity(false);
        headStand.setBasePlate(false);
        headStand.setRemoveWhenFarAway(false);
        headStand.setHelmet(headItem);

        List<ArmorStand> holoStands = new ArrayList<>();
        double yOffset = 0.5;
        for (String line : hologramLines) {
            ArmorStand holoStand = loc.getWorld().spawn(loc.clone().add(0, yOffset, 0), ArmorStand.class);
            holoStand.setVisible(false);
            holoStand.setGravity(false);
            holoStand.setBasePlate(false);
            holoStand.setRemoveWhenFarAway(false);
            holoStands.add(holoStand);
            yOffset += 0.25;
        }

        HeadData headData = new HeadData(headKey, headStand, holoStands, hologramLines, serverName, command);
        headData.startAnimation(plugin);
        return headData;
    }

    public void removeAllHeads() {
        for (HeadData head : new HashSet<>(heads)) {
            if (head.headStand != null && !head.headStand.isDead()) {
                head.headStand.remove();
            }
            for (ArmorStand holo : head.holoStands) {
                if (holo != null && !holo.isDead()) {
                    holo.remove();
                }
            }
        }
        heads.clear();
    }

    private int getPlayersInServer(String serverName) {
        return BungeeListener.GLOBAL_COUNTS.getOrDefault(serverName, 0);
    }

    public void reloadAllHeads() {
        removeAllHeads();
        headsConfig.options().copyDefaults(true);
        try { headsConfig.load(headsFile); } catch (Exception e) { e.printStackTrace(); }

        for (String key : headsConfig.getKeys(false)) {
            String worldName = headsConfig.getString(key + ".world");
            double x = headsConfig.getDouble(key + ".x");
            double y = headsConfig.getDouble(key + ".y");
            double z = headsConfig.getDouble(key + ".z");
            String textureId = headsConfig.getString(key + ".textureId");
            List<String> hologramLines = headsConfig.getStringList(key + ".hologramLines");
            String serverName = headsConfig.getString(key + ".serverName");
            String command = headsConfig.getString(key + ".command");

            Location loc = new Location(Bukkit.getWorld(worldName), x, y, z);
            HeadData headData = createHeadAtLocation(key, loc, textureId, hologramLines, serverName, command);
            heads.add(headData);

            if (serverName != null && !serverName.isEmpty()) {
                serverCounts.putIfAbsent(serverName, 0);
                requestPlayerCount(serverName);
            }
        }
        requestPlayerCount("ALL");
    }

    private void requestPlayerCount(String serverName) {
        if (Bukkit.getOnlinePlayers().isEmpty()) return;
        Player player = Bukkit.getOnlinePlayers().iterator().next();
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("PlayerCount");
            out.writeUTF(serverName);
            player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ItemStack getCustomSkull(String textureId) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        if (textureId == null || textureId.isEmpty()) return head;

        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);

        String base64 = Base64.getEncoder().encodeToString(
                ("{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/" + textureId + "\"}}}")
                        .getBytes(StandardCharsets.UTF_8)
        );
        profile.getProperties().put("textures", new Property("textures", base64));

        try {
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, profile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        head.setItemMeta(skullMeta);
        return head;
    }

    public void loadHeads() {
        for (String key : headsConfig.getKeys(false)) {
            String worldName = headsConfig.getString(key + ".world");
            double x = headsConfig.getDouble(key + ".x");
            double y = headsConfig.getDouble(key + ".y");
            double z = headsConfig.getDouble(key + ".z");
            String textureId = headsConfig.getString(key + ".textureId");
            List<String> hologramLines = headsConfig.getStringList(key + ".hologramLines");
            String serverName = headsConfig.getString(key + ".serverName");
            String command = headsConfig.getString(key + ".command");

            Location loc = new Location(Bukkit.getWorld(worldName), x, y, z);
            HeadData headData = createHeadAtLocation(key, loc, textureId, hologramLines, serverName, command);
            heads.add(headData);

            if (serverName != null && !serverName.isEmpty()) {
                serverCounts.putIfAbsent(serverName, 0);
                requestPlayerCount(serverName);
            }
        }
        requestPlayerCount("ALL");
    }

    private void saveHeads() {
        try { headsConfig.save(headsFile); } catch (Exception e) { e.printStackTrace(); }
    }

    @EventHandler
    public void onPlayerDamageHead(EntityDamageEvent event) {
        for (HeadData head : heads) {
            if (head.headStand.equals(event.getEntity())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerInteractHead(PlayerInteractAtEntityEvent event) {
        for (HeadData head : heads) {
            if (head.headStand.equals(event.getRightClicked())) {
                event.setCancelled(true);
                Player player = event.getPlayer();

                if (head.command != null && !head.command.isEmpty()) {
                    Bukkit.dispatchCommand(player, head.command.replace("{player}", player.getName()));
                    return;
                }

                if (head.serverName != null && !head.serverName.isEmpty()) {
                    sendPlayerToServer(player, head.serverName);
                }
            }
        }
    }

    public String getDefaultEffect() {
        return "HEART"; // القيمة الافتراضية عند عدم تحديد أي effect في الملف
    }
    
    private void sendPlayerToServer(Player player, String serverName) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("Connect");
            out.writeUTF(serverName);
            player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
            player.sendMessage(ChatColor.WHITE + "You are now queued for " + serverName + "...");
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "That queue is not found");
            e.printStackTrace();
        }
    }

    private class HeadData {
        final String headKey;
        final ArmorStand headStand;
        final List<ArmorStand> holoStands;
        final List<String> hologramLines;
        final String serverName;
        final String command;

        double yOffset = 0;
        boolean goingUp = true;
        float rotation = 0;

        HeadData(String headKey, ArmorStand headStand, List<ArmorStand> holoStands, List<String> hologramLines, String serverName, String command) {
            this.headKey = headKey;
            this.headStand = headStand;
            this.holoStands = holoStands;
            this.hologramLines = hologramLines;
            this.serverName = serverName;
            this.command = command;
        }

        void startAnimation(Main plugin) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (headStand == null || headStand.isDead()) {
                        cancel();
                        return;
                    }

                    if (goingUp) {
                        yOffset += 0.03;
                        if (yOffset >= 0.5) goingUp = false;
                    } else {
                        yOffset -= 0.03;
                        if (yOffset <= -0.5) goingUp = true;
                    }

                    rotation += 5;
                    if (rotation >= 360) rotation = 0;

                    Location headLoc = headStand.getLocation();
                    headLoc.setY(headLoc.getY() + (goingUp ? 0.03 : -0.03));
                    headStand.teleport(headLoc);
                    headStand.setHeadPose(headStand.getHeadPose().add(0, (float) Math.toRadians(5), 0));

                    double holoYOffset = 0.5;
                    for (int i = 0; i < holoStands.size(); i++) {
                        ArmorStand holo = holoStands.get(i);
                        holo.teleport(headStand.getLocation().clone().add(0, holoYOffset, 0));

                        String line = hologramLines.get(i);
                        if (line != null && !line.trim().isEmpty()) {
                            String processedLine = line;
                            if (serverName != null && !serverName.isEmpty()) {
                                int serverPlayers = getPlayersInServer(serverName);
                                processedLine = processedLine.replace("{server_" + serverName + "}", String.valueOf(serverPlayers));
                            }
                            processedLine = processedLine.replace("{network}", String.valueOf(BungeeListener.bungeeCount));

                            holo.setCustomName(ChatColor.translateAlternateColorCodes('&', processedLine));
                            holo.setCustomNameVisible(true);
                        } else {
                            holo.setCustomNameVisible(false);
                        }

                        holoYOffset += 0.25;
                    }

                    Location effectLoc = headStand.getLocation().clone().add(0, 0.1, 0);
                    String effectNameFromFile = headsConfig.getString(headKey + ".effectName", getDefaultEffect());
                    try {
                        Effect effect = Effect.valueOf(effectNameFromFile.toUpperCase());
                        headStand.getWorld().playEffect(effectLoc, effect, 0);
                    } catch (Exception e) {
                        headStand.getWorld().playEffect(effectLoc, Effect.SMOKE, 0);
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }
}
