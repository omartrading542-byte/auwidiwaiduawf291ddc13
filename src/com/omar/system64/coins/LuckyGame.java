package com.omar.system64.coins;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class LuckyGame implements Listener {

    private final JavaPlugin plugin;
    private final Coins coinsManager;

    private boolean gameRunning = false;
    private UUID currentPlayer;
    private long betAmountPerItem;

    private static final int GUI_SIZE = 27;
    private static final String GUI_TITLE = ChatColor.GOLD + "Lucky Game - Choose your items";

    private Inventory gameInventory;

    private boolean canChoose = false;
    private Set<Integer> chosenSlots = new HashSet<>();

    private final List<ItemStack> normalItems = new ArrayList<>();
    private final List<ItemStack> rareItems = new ArrayList<>();

    private int winningItemsCount;

    private final Map<UUID, Long> lastBetAmounts = new HashMap<>();

    private Set<Integer> winningSlots = new HashSet<>();

    // نسخة ثابتة للعناصر في كل خانة
    private Map<Integer, ItemStack> slotToItem = new HashMap<>();

    public LuckyGame(JavaPlugin plugin, Coins coinsManager) {
        this.plugin = plugin;
        this.coinsManager = coinsManager;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        normalItems.add(createItem(Material.IRON_INGOT, ChatColor.AQUA + "Iron Ingot x5", 5));
        normalItems.add(createItem(Material.GOLD_INGOT, ChatColor.AQUA + "Gold Ingot x3", 3));
        normalItems.add(createItem(Material.COAL, ChatColor.AQUA + "Coal x2", 2));
        normalItems.add(createItem(Material.REDSTONE, ChatColor.AQUA + "Redstone x6", 6));
        normalItems.add(createItem(Material.INK_SACK, ChatColor.AQUA + "Ink Sack x4", 4));

        rareItems.add(createItem(Material.DIAMOND, ChatColor.LIGHT_PURPLE + "Diamond x10", 10));
        rareItems.add(createItem(Material.EMERALD, ChatColor.LIGHT_PURPLE + "Emerald x7", 7));
    }

    public void startGame(Player player, long amount) {
        if (gameRunning) {
            player.sendMessage(ChatColor.RED + "The game is currently running, please wait.");
            return;
        }
        if (amount < 50) {
            player.sendMessage(ChatColor.RED + "Minimum bet amount is 50 coins.");
            return;
        }

        long playerCoins = coinsManager.getCoins(player.getUniqueId());
        long totalPossibleBet = amount * 5;

        if (playerCoins < totalPossibleBet) {
            player.sendMessage(ChatColor.RED + "You don't have enough coins to bet on 5 items (" + totalPossibleBet + " coins required).");
            return;
        }

        betAmountPerItem = amount;
        lastBetAmounts.put(player.getUniqueId(), amount);

        gameRunning = true;
        currentPlayer = player.getUniqueId();
        chosenSlots.clear();
        winningSlots.clear();
        slotToItem.clear();

        player.sendMessage(ChatColor.GREEN + "Lucky game started! You can select up to 5 items. Each item costs " + amount + " coins.");
        Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " started Lucky Game betting " + amount + " coins per item.");

        openGameGUI(player);

        canChoose = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                canChoose = false;
                player.sendMessage(ChatColor.RED + "Time's up! No more choices allowed.");
                startAnimationAndAnnounce(player);
            }
        }.runTaskLater(plugin, 20 * 10L);
    }

    private void openGameGUI(Player player) {
        gameInventory = Bukkit.createInventory(null, GUI_SIZE, GUI_TITLE);
        Random random = new Random();

        for (int i = 0; i < GUI_SIZE; i++) {
            ItemStack item;
            if (random.nextInt(100) < 5) {
                item = rareItems.get(random.nextInt(rareItems.size())).clone();
            } else {
                item = normalItems.get(random.nextInt(normalItems.size())).clone();
            }
            gameInventory.setItem(i, item);
            slotToItem.put(i, item.clone());
        }

        player.openInventory(gameInventory);
    }

    private ItemStack createItem(Material material, String name, int amount) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory() == null) return;
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        if (slot >= 0 && slot < GUI_SIZE) {
            handlePlayerChoice(player, slot);
        }
    }

    public void handlePlayerChoice(Player player, int slot) {
        if (!gameRunning || currentPlayer == null || !player.getUniqueId().equals(currentPlayer)) {
            Long lastBet = lastBetAmounts.get(player.getUniqueId());
            if (lastBet != null) {
                startGame(player, lastBet);
                player.sendMessage(ChatColor.GREEN + "Starting a new game automatically with your last bet: " + lastBet);
                return;
            } else {
                player.sendMessage(ChatColor.RED + "There is no running game for you. Use /luckygame <amount> to start.");
                return;
            }
        }

        if (!canChoose) {
            player.sendMessage(ChatColor.RED + "You can't choose items right now.");
            return;
        }

        if (chosenSlots.size() >= 5) {
            player.sendMessage(ChatColor.RED + "You can only select up to 5 items.");
            return;
        }

        if (slot < 0 || slot >= GUI_SIZE) {
            player.sendMessage(ChatColor.RED + "Invalid slot selected.");
            return;
        }

        if (chosenSlots.contains(slot)) {
            player.sendMessage(ChatColor.RED + "You already selected this item.");
            return;
        }

        long playerCoins = coinsManager.getCoins(player.getUniqueId());
        if (playerCoins < betAmountPerItem) {
            player.sendMessage(ChatColor.RED + "You don't have enough coins to bet on this item.");
            return;
        }

        boolean removed = coinsManager.removeCoins(player.getUniqueId(), betAmountPerItem);
        if (!removed) {
            player.sendMessage(ChatColor.RED + "Error deducting coins for your bet.");
            return;
        }

        chosenSlots.add(slot);
        ItemStack selectedItem = slotToItem.get(slot);
        String itemName = (selectedItem != null && selectedItem.getItemMeta() != null) ?
                selectedItem.getItemMeta().getDisplayName() : "Unknown Item";

        player.sendMessage(ChatColor.GREEN + "Item selected: " + itemName);
        player.sendMessage(ChatColor.YELLOW + "betAmountPerItem" + " coins have been deducted.");

        ItemStack itemInGUI = gameInventory.getItem(slot);
        if (itemInGUI != null) {
            ItemMeta meta = itemInGUI.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + "[Selected] " + ChatColor.stripColor(meta.getDisplayName()));
                itemInGUI.setItemMeta(meta);
                gameInventory.setItem(slot, itemInGUI);
                player.updateInventory();
            }
        }
    }

    private void startAnimationAndAnnounce(Player player) {
        player.closeInventory();

        winningSlots.clear();
        Random rand = new Random();

        // تعديل الاحتمالات: 1 أو 2 أو 3 عناصر فقط ، بدون 0
        int chance = rand.nextInt(100); // 0-99

        if (chance < 60) {
            winningItemsCount = 1; // 60% 1 عنصر
        } else if (chance < 95) {
            winningItemsCount = 3; // 35% 2 عنصر
        } else {
            winningItemsCount = 6; // 5% 3 عناصر نادرة
        }

        int rareWinCount = 0;
        int normalWinCount = winningItemsCount;

        if (winningItemsCount >= 2) {
            rareWinCount = 1;
            normalWinCount = winningItemsCount - rareWinCount;
        } else if (winningItemsCount == 1) {
            rareWinCount = rand.nextBoolean() ? 1 : 0;
            normalWinCount = winningItemsCount - rareWinCount;
        }

        List<Integer> normalCandidateSlots = new ArrayList<>();
        List<Integer> rareCandidateSlots = new ArrayList<>();

        for (int i = 0; i < GUI_SIZE; i++) {
            ItemStack item = slotToItem.get(i);
            if (item == null) continue;
            if (isRareItem(item)) rareCandidateSlots.add(i);
            else if (isNormalItem(item)) normalCandidateSlots.add(i);
        }

        Collections.shuffle(normalCandidateSlots);
        Collections.shuffle(rareCandidateSlots);

        winningSlots.clear();

        winningSlots.addAll(normalCandidateSlots.subList(0, Math.min(normalWinCount, normalCandidateSlots.size())));
        winningSlots.addAll(rareCandidateSlots.subList(0, Math.min(rareWinCount, rareCandidateSlots.size())));

        while (winningSlots.size() < winningItemsCount && normalCandidateSlots.size() > 0) {
            for (int i : normalCandidateSlots) {
                if (winningSlots.size() >= winningItemsCount) break;
                if (!winningSlots.contains(i)) winningSlots.add(i);
            }
            break;
        }

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 60;

            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    cancel();
                    showWinningItemsOnly(player);
                    return;
                }
                ticks++;

                ItemStack last = gameInventory.getItem(GUI_SIZE - 1);
                for (int i = GUI_SIZE - 1; i > 0; i--) {
                    gameInventory.setItem(i, gameInventory.getItem(i - 1));
                }
                gameInventory.setItem(0, last);

                player.openInventory(gameInventory);
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }

    private void showWinningItemsOnly(Player player) {
        for (int i = 0; i < GUI_SIZE; i++) {
            gameInventory.setItem(i, null);
        }

        for (int slot : winningSlots) {
            ItemStack winningItem = slotToItem.get(slot);
            if (winningItem == null) {
                winningItem = generateRandomItem();
            }
            gameInventory.setItem(slot, winningItem.clone());
        }

        player.openInventory(gameInventory);

        new BukkitRunnable() {
            @Override
            public void run() {
                Random random = new Random();
                for (int i = 0; i < GUI_SIZE; i++) {
                    ItemStack item;
                    if (random.nextInt(100) < 5) {
                        item = rareItems.get(random.nextInt(rareItems.size())).clone();
                    } else {
                        item = normalItems.get(random.nextInt(normalItems.size())).clone();
                    }
                    gameInventory.setItem(i, item);
                }
                player.openInventory(gameInventory);

                announceResult(player);
            }
        }.runTaskLater(plugin, 20 * 3L);
    }

    private ItemStack generateRandomItem() {
        Random random = new Random();
        if (random.nextInt(100) < 5) {
            return rareItems.get(random.nextInt(rareItems.size())).clone();
        } else {
            return normalItems.get(random.nextInt(normalItems.size())).clone();
        }
    }

    private boolean isRareItem(ItemStack item) {
        for (ItemStack rare : rareItems) {
            if (rare.getType() == item.getType()) return true;
        }
        return false;
    }

    private boolean isNormalItem(ItemStack item) {
        for (ItemStack normal : normalItems) {
            if (normal.getType() == item.getType()) return true;
        }
        return false;
    }

    private void announceResult(Player player) {
        int winCount = 0;
        int maxMultiplier = 1;

        for (int slot : chosenSlots) {
            if (winningSlots.contains(slot)) {
                winCount++;
                ItemStack item = slotToItem.get(slot);
                if (item == null) continue;
                ItemMeta meta = item.getItemMeta();
                if (meta == null) continue;
                String name = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();

                int multiplier = 1;
                if (name.contains("x10")) multiplier = 10;
                else if (name.contains("x7")) multiplier = 7;
                else if (name.contains("x6")) multiplier = 6;
                else if (name.contains("x5")) multiplier = 5;
                else if (name.contains("x4")) multiplier = 4;
                else if (name.contains("x3")) multiplier = 3;
                else if (name.contains("x2")) multiplier = 2;

                if (multiplier > maxMultiplier) maxMultiplier = multiplier;
            }
        }

        if (winCount > 0) {
            long winAmount = betAmountPerItem * maxMultiplier * winCount;
            coinsManager.addCoins(currentPlayer, winAmount);

            player.sendMessage(ChatColor.GREEN + "Congratulations! You won " + winAmount + " coins with " + winCount + " winning item(s) and a x" + maxMultiplier + " multiplier!");
            if (winAmount > 15) {
                Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "Congrats to " + player.getName() + " for winning " + winAmount + " coins in the Lucky Game!");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Sorry, you lost your bet in the Lucky Game.");
        }

        gameRunning = false;
        currentPlayer = null;
        chosenSlots.clear();
        winningSlots.clear();
        slotToItem.clear();
        canChoose = false;

        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage(ChatColor.GOLD + "Use /luckygame <amount> to start a new game.");
            }
        }.runTaskLater(plugin, 20L);
    }
}
