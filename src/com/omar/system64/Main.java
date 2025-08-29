package com.omar.system64;

import com.omar.system64.achievements.AchievementManager;
import com.omar.system64.achievements.AchievementsCommand;
import com.omar.system64.bossbar.BossBarManager;
import com.omar.system64.bungee.BungeeListener;
import com.omar.system64.coins.BuyCommand;
import com.omar.system64.coins.Coins;
import com.omar.system64.coins.CoinsCommand;
import com.omar.system64.coins.CoinsGUICommand;
import com.omar.system64.coins.CoinsGUIListener;
import com.omar.system64.coins.LuckyGame;
import com.omar.system64.coins.LuckyGameCommand;
import com.omar.system64.coins.LuckyGameListener;
import com.omar.system64.coins.ShopGUI;
import com.omar.system64.coins.ShopManager;
import com.omar.system64.commands.RankColor;
import com.omar.system64.commands.SetGGCommand;
import com.omar.system64.holograms.HologramCommand;
import com.omar.system64.holograms.HologramManager;
import com.omar.system64.levelManager.LevelCommands;
import com.omar.system64.levelManager.LevelListener;
import com.omar.system64.levelManager.LevelManager;
import com.omar.system64.levelManager.LevelPlaceholders;
import com.omar.system64.pvp.MapPVPCommand;
import com.omar.system64.scoreboard.Board;
import com.omar.system64.scoreboard.ScoreboardConfig;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main extends JavaPlugin {

    private AutoFly autoFly;
    private Ranks ranks;
    private static Main instance;
    private FileConfiguration scoreboardConfig;
    private File scoreboardFile;
    private LevelManager levelManager;
    private final Map<UUID, Board> boards = new HashMap<>();
    private HologramManager hologramManager;
    private BossBarManager bossBarManager;
    private AchievementManager achievementManager;
    private GuildManager guildManager;
    private LightningZoneManager lightningManager;
    private Coins coins;
    private LuckyGame luckyGame;
    private ShopManager shopManager;
    private AnimatedHead animatedHead;
    private GuildChatListener guildChatListener;
    private WorldRegionManager worldRegionManager;
    private GiftSystem giftSystem;
    
    @Override
    public void onEnable() {
        instance = this;
        
        Bukkit.getScheduler().runTaskLater(this, () -> {
            // 1. مسح كل الهيدز والهولوجرامات القديمة من العالم
            for (World world : Bukkit.getWorlds()) {
                for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
                    if (!stand.isVisible() && !stand.hasGravity() && stand.getHelmet() != null) {
                        stand.remove();
                    }
                }
            }

            // 2. إعادة إنشاء الهيدز من ملف heads.yml
            getAnimatedHead().removeAllHeads(); // تأكد من تفريغ الـ Set الداخلي
            getAnimatedHead().loadHeads();      // استدعاء دالة loadHeads لإعادة البناء
        }, 1L);
    
        MySQL.connect(getDataFolder());  // الاتصال بقاعدة البيانات
        this.animatedHead = new AnimatedHead(this);
        animatedHead.reloadAllHeads();
        
        // أنشئ Coins أولاً لأن الكلاسات الأخرى تعتمد عليها
        this.coins = new Coins(this);
        
        
        new JumpBoostManager(this);
        
        worldRegionManager = new WorldRegionManager(this);
        
        // أنشئ ShopManager بتمرير Coins
        this.shopManager = new ShopManager(coins);

        // أنشئ LuckyGame
        this.luckyGame = new LuckyGame(this, coins);

        // أنشئ Ranks بعد تحميل الملفات
        File ranksFile = new File(getDataFolder(), "playersrank.yml");
        if (!ranksFile.exists()) saveResource("playersrank.yml", false);
        this.ranks = new Ranks(this, ranksFile);

        // أنشئ LevelManager وحمّل ملف المستويات
        this.levelManager = new LevelManager(this, null);
        this.levelManager.loadLevelsFile();

        // أنشئ HologramManager وابدأ التحديث التلقائي
        this.hologramManager = new HologramManager(this, levelManager);
        this.hologramManager.startAutoUpdate();

        // أنشئ GuildManager
        this.guildManager = new GuildManager(this, ranks);
        this.guildManager.loadTopGuildHologram();
        
        giftSystem = new GiftSystem(this, coins, levelManager);
        
        // جلب ملف Guilds إذا غير موجود
        File guildsFile = new File(getDataFolder(), "guilds.yml");
        if (!guildsFile.exists()) saveResource("guilds.yml", false);

        // جلب ملف Levels إذا غير موجود
        File levelFile = new File(getDataFolder(), "levels.yml");
        if (!levelFile.exists()) saveResource("levels.yml", false);

        this.guildChatListener = new GuildChatListener(guildManager, ranks, levelManager);
        
        
        // أنشئ AchievementManager وابدأ تحديث الهولوجرام
        this.achievementManager = new AchievementManager(this);
        this.achievementManager.startAutoUpdateHologram();

        // تحميل إعدادات الـ scoreboard
        ScoreboardConfig.load(this);
        loadScoreboardConfig();

        // إنشاء BossBarManager
        this.bossBarManager = new BossBarManager(this);

        // بدء تحديث هولوجرام الجيلد كل 5 ثواني
        new BukkitRunnable() {
            @Override
            public void run() {
                guildManager.updateTopGuildHologram();
            }
        }.runTaskTimer(this, 20L * 5, 20L * 5);

        saveDefaultConfig();

        this.autoFly = new AutoFly(this);

        // تسجيل الأحداث العامة
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent event) {
                Player player = event.getPlayer();
                String message = ChatColor.translateAlternateColorCodes('&', "&aWelcome to the Server!");
                bossBarManager.showPermanentBossBar(player, message, 1.0f);
            }
        }, this);

        // تسجيل PlayerJoinListener مع تمرير Coins
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this, coins), this);

        // إنشاء الـ Boards لجميع اللاعبين المتصلين وتمرير coins
        for (Player player : Bukkit.getOnlinePlayers()) {
            Board board = new Board(player, levelManager, ranks, achievementManager, coins, guildManager, giftSystem);
            board.updateBoard();
            boards.put(player.getUniqueId(), board);
        }

        registerListeners();
        registerCommands();
        
        // تسجيل الـ PlaceholderAPI إن كان موجود
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new LevelPlaceholders(this, levelManager).register();
            getLogger().info("LevelPlaceholders registered!");
        }
    }


    

    private void loadScoreboardConfig() {
        scoreboardFile = new File(getDataFolder(), "scoreboard.yml");
        if (!scoreboardFile.exists()) {
            saveResource("scoreboard.yml", false);
        }
        scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new CommandOnJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new AutoFly(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemJoin(levelManager, ranks, achievementManager, guildManager), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeListener());
        Bukkit.getPluginManager().registerEvents(new JumpBoostManager(this), this);

        Bukkit.getPluginManager().registerEvents(giftSystem, this);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new RankPlaceholder(this, ranks, guildManager, levelManager).register();
            getServer().getPluginManager().registerEvents(new CoinsGUIListener(coins), this);
            getServer().getPluginManager().registerEvents(new RankColor(), this);
            getServer().getPluginManager().registerEvents(new LuckyGameListener(luckyGame), this);
            getServer().getPluginManager().registerEvents(new JoinListener(this, ranks), this);
            getServer().getPluginManager().registerEvents(new NoBreak(this), this);
            getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, coins), this);
            getServer().getPluginManager().registerEvents(new NoFallDamage(), this);
            getServer().getPluginManager().registerEvents(new LevelListener(levelManager), this);
            getServer().getPluginManager().registerEvents(new MapPVPCommand(this), this);
            getServer().getPluginManager().registerEvents(guildChatListener, this);
            lightningManager = new LightningZoneManager(this);
            Bukkit.getPluginManager().registerEvents(lightningManager, this);
            getServer().getPluginManager().registerEvents(new ShopGUI(), this);
            getServer().getPluginManager().registerEvents(worldRegionManager, this);
        }
        
    }

    private void registerCommands() {
        LevelCommands levelCommands = new LevelCommands(levelManager);
        getCommand("setlevel").setExecutor(levelCommands);
        getCommand("addlevel").setExecutor(levelCommands);


        if (getCommand("coins") != null) {
            getCommand("coins").setExecutor(new CoinsCommand(coins));
        }
        
        getCommand("sethead").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cThis command can only be used by players!");
                return true;
            }

            Player player = (Player) sender;

            if (args.length < 2) {
                player.sendMessage("§cUsage: /sethead <server|command> <value>");
                return true;
            }

            String type = args[0].toLowerCase();
            String value = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

            // القيم الافتراضية للرأس
            String textureId = animatedHead.getDefaultTexture();
            String effectName = animatedHead.getDefaultEffect();

            List<String> hologramText = animatedHead.getDefaultHologram(type.equals("server") ? value : "???");

            if (type.equals("server")) {
                // إنشاء رأس يرسل اللاعب للسيرفر عند النقر
                animatedHead.createHeadWithHologram(player, textureId, hologramText, effectName, value, null);
                player.sendMessage("§aHead created successfully for server: " + value);
            } else if (type.equals("command")) {
                // إنشاء رأس ينفذ الأمر عند النقر
                animatedHead.createHeadWithHologram(player, textureId, hologramText, effectName, null, value);
                player.sendMessage("§aHead created successfully for command: " + value);
            } else {
                player.sendMessage("§cInvalid type! Use 'server' or 'command'.");
            }

            return true;
        });

        getCommand("coinsgui").setExecutor(new CoinsGUICommand(coins));
        getCommand("luckygame").setExecutor(new LuckyGameCommand(luckyGame));
        getCommand("setxp").setExecutor(levelCommands);
        getCommand("addxp").setExecutor(levelCommands);
        getCommand("level").setExecutor(levelCommands);
        getCommand("achievements").setExecutor(new AchievementsCommand(achievementManager));
        getCommand("hologram").setExecutor(new HologramCommand(hologramManager));
        getCommand("pholoset").setExecutor(new HologramCommandExecutor(this));
        getCommand("guild").setExecutor(new GuildCommand(guildManager, guildChatListener));
        getCommand("g").setExecutor(new GuildCommand(guildManager, guildChatListener));
        getCommand("ach").setExecutor(new AchievementCommandExecutor(this));
        getCommand("setspawn").setExecutor(new LobbyCommand(this));
        getCommand("setlobby").setExecutor(new LobbyCommand(this));
        getCommand("lobby").setExecutor(new LobbyCommand(this));
        getCommand("setrank").setExecutor(new SetRankCommand(this));
        getCommand("fly").setExecutor(new FlyCommand(this));
        getCommand("setpvp").setExecutor(new MapPVPCommand(this));
        getCommand("rankcolor").setExecutor(new RankColor());
        getCommand("setgg").setExecutor(new SetGGCommand(lightningManager));
        getCommand("setpermission").setExecutor(ranks);
        getCommand("addpermission").setExecutor(ranks);
        getCommand("removepermission").setExecutor(ranks);
        getCommand("setworld").setExecutor(worldRegionManager);
        getCommand("setgift").setExecutor(giftSystem);
        this.getCommand("effect").setExecutor(new EffectCommand1(this));



        
    

        // هنا تسجل BuyCommand بتمرير this و coins
        BuyCommand buyCommand = new BuyCommand(this, coins, ranks, levelManager, null);
        getCommand("buy").setExecutor(buyCommand);
    }



    @Override
    public void onDisable() {
        saveConfig();
        if (hologramManager != null) hologramManager.removeHologram();
        if (achievementManager != null) achievementManager.removeHologram();
        if (animatedHead != null) animatedHead.removeAllHeads();
    }



    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public Ranks getRanks() {
        return ranks;
    }

    public Coins getCoins() {
        return coins;
    }

    public static Main getInstance() {
        return instance;
    }

    public FileConfiguration getScoreboardConfig() {
        return scoreboardConfig;
    }
    
    public WorldRegionManager getWorldRegionManager() {
        return worldRegionManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }
    
    public GuildChatListener getGuildChatListener() {
        return guildChatListener;
    }

    public GuildManager getGuildManager() {
        return guildManager;
    }
    
    public AnimatedHead getAnimatedHead() {
        return animatedHead;
    }

    public Board getBoard(UUID uuid) {
        return boards.get(uuid);
    }

    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }

    public AchievementManager getAchievementManager() {
        return achievementManager;
    }

    public LightningZoneManager getLightningManager() {
        return lightningManager;
    }

    public Location getSpawnLocation() {
        if (getConfig().contains("spawn")) {
            String world = getConfig().getString("spawn.world");
            double x = getConfig().getDouble("spawn.x");
            double y = getConfig().getDouble("spawn.y");
            double z = getConfig().getDouble("spawn.z");
            float yaw = (float) getConfig().getDouble("spawn.yaw");
            float pitch = (float) getConfig().getDouble("spawn.pitch");

            World w = Bukkit.getWorld(world);
            if (w == null) {
                Bukkit.getLogger().warning("[system64] World '" + world + "' not found!");
                return null;
            }
            return new Location(w, x, y, z, yaw, pitch);
        }
        return null;
    }

    public void setPlayerLevel(UUID playerUUID, int level) {
        try {
            File levelFile = new File(getDataFolder(), "levels.yml");
            if (!levelFile.exists()) {
                saveResource("levels.yml", false);
            }
            FileConfiguration levelConfig = YamlConfiguration.loadConfiguration(levelFile);
            levelConfig.set(playerUUID.toString() + ".level", level);
            levelConfig.save(levelFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
