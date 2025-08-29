package com.omar.system64;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.omar.system64.coins.Coins;
import com.omar.system64.levelManager.LevelManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GiftSystem implements Listener, CommandExecutor {

    private final JavaPlugin plugin;
    private final Random random = new Random();

    private final Set<UUID> wandMode = new HashSet<>();
    private final Map<UUID, Integer> radiusMap = new HashMap<>();

    private final Map<UUID, Map<String, Long>> playerCooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 24 * 60 * 60 * 1000; // 24 ساعة

    private final Set<UUID> receivingGift = new HashSet<>();
    private final Map<UUID, Map<String, Boolean>> warnedMap = new HashMap<>();

    private final File giftsFile;
    private final YamlConfiguration giftsConfig;
    private final File playersFile;
    private final YamlConfiguration playersConfig;

    private final Coins coins;
    private final LevelManager levelManager;

    private static final int[] COINS_VALUES = {3, 5, 7, 10, 15, 18, 30};

    public GiftSystem(JavaPlugin plugin, Coins coins, LevelManager levelManager) {
        this.plugin = plugin;
        this.coins = coins;
        this.levelManager = levelManager;

        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getCommand("setgift").setExecutor(this);

        giftsFile = new File(plugin.getDataFolder(), "gift.yml");
        if (!giftsFile.exists()) try { giftsFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        giftsConfig = YamlConfiguration.loadConfiguration(giftsFile);

        playersFile = new File(plugin.getDataFolder(), "players.yml");
        if (!playersFile.exists()) try { playersFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        playersConfig = YamlConfiguration.loadConfiguration(playersFile);

        loadPlayerCooldowns();
    }

    private void loadPlayerCooldowns() {
        for (String uuidStr : playersConfig.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            Map<String, Long> cooldowns = new HashMap<>();
            for (String key : playersConfig.getConfigurationSection(uuidStr).getKeys(false)) {
                cooldowns.put(key, playersConfig.getLong(uuidStr + "." + key));
            }
            playerCooldowns.put(uuid, cooldowns);
        }
    }
    
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Item item = event.getItem();
        if (item.hasMetadata("NO_PICKUP")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getClickedInventory() == null) return;
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        // منع أخذ الرأس من الـ ArmorStand
        if (clicked.getType() == Material.SKULL_ITEM) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand) {
            ArmorStand stand = (ArmorStand) event.getRightClicked();
            if (stand.hasMetadata("NO_PICKUP")) {
                event.setCancelled(true); // يمنع اللاعب من أخذ helmet أو أي شيء من ArmorStand
            }
        }
    }

    private void savePlayerCooldowns() {
        try {
            for (UUID uuid : playerCooldowns.keySet()) {
                Map<String, Long> map = playerCooldowns.get(uuid);
                for (String key : map.keySet()) {
                    playersConfig.set(uuid.toString() + "." + key, map.get(key));
                }
            }
            playersConfig.save(playersFile);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private String getNextGiftKey() {
        int index = 1;
        while (giftsConfig.contains("gift" + index)) index++;
        return "gift" + index;
    }

    @EventHandler
    public void onSetGift(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();
        if (item == null || item.getType() != Material.STICK) return;
        if (!player.hasPermission("gift.set") || !player.isSneaking() || event.getClickedBlock() == null) return;

        if (wandMode.contains(player.getUniqueId()) && radiusMap.containsKey(player.getUniqueId())) {
            if (player.hasMetadata("SETTING_GIFT")) return;
        }

        player.setMetadata("SETTING_GIFT", new FixedMetadataValue(plugin, true));

        Location loc = event.getClickedBlock().getLocation().add(0, 1, 0);
        String key = getNextGiftKey();

        giftsConfig.set(key + ".world", loc.getWorld().getName());
        giftsConfig.set(key + ".x", loc.getX());
        giftsConfig.set(key + ".y", loc.getY());
        giftsConfig.set(key + ".z", loc.getZ());
        giftsConfig.set(key + ".radius", 2);
        giftsConfig.set(key + ".heads", Arrays.asList(getDefaultTexture()));

        try { giftsConfig.save(giftsFile); } catch (IOException e) { e.printStackTrace(); }

        player.sendMessage(ChatColor.GREEN + "Gift location added with key: " + key);

        new BukkitRunnable() {
            @Override
            public void run() {
                player.removeMetadata("SETTING_GIFT", plugin);
            }
        }.runTaskLater(plugin, 5L);
    }

    public int getTotalGifts() {
        return giftsConfig.getKeys(false).size();
    }

    public int getPlayerClaimedGifts(UUID playerUUID) {
        Map<String, Long> cooldowns = playerCooldowns.get(playerUUID);
        if (cooldowns == null) return 0;
        return cooldowns.size();
    }

    public String getGiftScoreboardLine(Player player, String template) {
        int claimed = getPlayerClaimedGifts(player.getUniqueId());
        int total = giftsConfig.getKeys(false).size();
        return template.replace("{gift}", String.valueOf(claimed))
                       .replace("{totalGifts}", String.valueOf(total));
    }

    @EventHandler
    public void onPlayerNear(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (receivingGift.contains(player.getUniqueId())) return;

        Map<String, Boolean> warned = warnedMap.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());

        for (String key : giftsConfig.getKeys(false)) {
            Location loc = new Location(
                    Bukkit.getWorld(giftsConfig.getString(key + ".world")),
                    giftsConfig.getDouble(key + ".x"),
                    giftsConfig.getDouble(key + ".y"),
                    giftsConfig.getDouble(key + ".z")
            );
            int radius = giftsConfig.getInt(key + ".radius", 2);
            if (player.getLocation().distance(loc) > radius) continue;

            Map<String, Long> cooldowns = playerCooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
            Long lastTime = cooldowns.get(key);
            long now = System.currentTimeMillis();

            if (lastTime != null && now - lastTime < COOLDOWN_MS) {
                long timeLeft = COOLDOWN_MS - (now - lastTime);

                long hours = timeLeft / 3600000;
                long minutes = (timeLeft % 3600000) / 60000;
                long seconds = (timeLeft % 60000) / 1000;

                if (!warned.getOrDefault(key, false)) {
                    player.sendMessage(ChatColor.RED + "Please wait " 
                        + hours + "h " + minutes + "m " + seconds + "s before claiming this gift again!");
                    warned.put(key, true);
                    new BukkitRunnable() {
                        @Override
                        public void run() { warned.put(key, false); }
                    }.runTaskLater(plugin, 20L);
                }
                return;
            }

            // حفظ الوقت عند أخذ الهدية مباشرة
            cooldowns.put(key, now);
            playerCooldowns.put(player.getUniqueId(), cooldowns);
            savePlayerCooldowns(); // حفظ تلقائي في players.yml

            receivingGift.add(player.getUniqueId());
            triggerGift(player, loc, giftsConfig.getStringList(key + ".heads"));
            break;
        }
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        Item item = event.getItem();
        if (item.hasMetadata("NO_PICKUP")) event.setCancelled(true);
    }

    private void triggerGift(Player player, Location loc, List<String> heads) {
        if (loc == null) return;
        player.getWorld().strikeLightningEffect(loc);

        ArmorStand stand = loc.getWorld().spawn(loc.clone().subtract(0, 1, 0), ArmorStand.class);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setMetadata("NO_PICKUP", new FixedMetadataValue(plugin, true));

        String texture = getDefaultTexture();
        if (heads != null && !heads.isEmpty()) texture = heads.get(random.nextInt(heads.size()));

        stand.setHelmet(getCustomSkull(texture));
        stand.setCustomNameVisible(false);

        Hologram hologram = HologramsAPI.createHologram(plugin, loc.clone().add(0, 2, 0));
        hologram.appendTextLine(ChatColor.BLUE + "???");

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (player == null || !player.isOnline()) {
                    hologram.delete();
                    stand.remove();
                    receivingGift.remove(player.getUniqueId());
                    cancel();
                    return;
                }
                ticks++;
                stand.teleport(stand.getLocation().add(0, 0.1, 0));
                hologram.teleport(hologram.getLocation().add(0, 0.1, 0));
                if (ticks >= 20) {
                    cancel();
                    startRandomReward(hologram, player, stand);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void startRandomReward(Hologram hologram, Player player, ArmorStand stand) {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (player == null || !player.isOnline()) {
                    hologram.delete();
                    stand.remove();
                    receivingGift.remove(player.getUniqueId());
                    cancel();
                    return;
                }
                ticks++;
                hologram.clearLines();
                hologram.appendTextLine(ChatColor.LIGHT_PURPLE + randomString());

                if (ticks >= 20) {
                    cancel();
                    hologram.clearLines();
                    hologram.appendTextLine(ChatColor.GOLD + "Congratulations! You received the gift 🎁");

                    int rareChance = 1 + random.nextInt(8);
                    int normalChance = random.nextInt(100);

                    if (rareChance == 1) {
                        int coinsAmount = 10 + random.nextInt(21);
                        int xpAmount = 10 + random.nextInt(41);
                        coins.addCoins(player.getUniqueId(), coinsAmount);
                        levelManager.addXP(player.getUniqueId(), xpAmount);
                        player.sendMessage(ChatColor.GOLD + "Lucky gift! You received " + coinsAmount + " coins & " + ChatColor.AQUA + xpAmount + " XP!");
                    } else if (normalChance < 50) {
                        int coinsAmount = 10 + random.nextInt(21);
                        coins.addCoins(player.getUniqueId(), coinsAmount);
                        player.sendMessage(ChatColor.GOLD + "You received " + coinsAmount + " coins!");
                    } else {
                        int xpAmount = 10 + random.nextInt(41);
                        levelManager.addXP(player.getUniqueId(), xpAmount);
                        player.sendMessage(ChatColor.AQUA + "You received " + xpAmount + " XP!");
                    }

                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1f);
                    hologram.delete();
                    stand.remove();
                    receivingGift.remove(player.getUniqueId());
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private String randomString() {
        String chars = "0123456789abcdef";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }

    public String getDefaultTexture() {
        return "48aa2f9bffd8853646c1375d3de71b8187bd426332d9f9d712216f7e92a930d";
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
        } catch (Exception e) { e.printStackTrace(); }
        head.setItemMeta(skullMeta);
        return head;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is for players only.");
            return true;
        }
        Player p = (Player) sender;
        if (!p.hasPermission("system64.setgift")) {
            p.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("cancel")) {
            wandMode.remove(p.getUniqueId());
            radiusMap.remove(p.getUniqueId());
            p.sendMessage(ChatColor.YELLOW + "Gift selection mode canceled.");
            return true;
        }

        int radius = 4;
        if (args.length > 0) {
            try { radius = Math.max(1, Integer.parseInt(args[0])); } catch (NumberFormatException ignored) {}
        }

        wandMode.add(p.getUniqueId());
        radiusMap.put(p.getUniqueId(), radius);

        ItemStack wand = new ItemStack(Material.STICK, 1);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Gift Wand");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Right-click a block to set gift trigger");
        lore.add(ChatColor.GRAY + "Radius: " + radius);
        meta.setLore(lore);
        wand.setItemMeta(meta);
        p.getInventory().addItem(wand);
        p.sendMessage(ChatColor.GREEN + "Gift selection mode enabled. Right-click a block to set activation point (radius=" + radius + ")");

        return true;
    }
}
