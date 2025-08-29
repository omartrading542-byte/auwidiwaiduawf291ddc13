package com.omar.system64.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RankColor implements CommandExecutor, Listener {

    private final String GUI_TITLE = ChatColor.BLUE + "Select RankColor";

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        Inventory gui = Bukkit.createInventory(null, 54, GUI_TITLE);

        addColorItem(gui, 2, Material.INK_SACK, (short) 0, "&7Default", player.getName(), "setrank " + player.getName() + " default", "rankcolor.default", player);
        addColorItem(gui, 4, Material.INK_SACK, (short) 1, "&aVIP&c+", player.getName(), "setrank " + player.getName() + " vip+red", "rankcolor.vip+red", player);
        addColorItem(gui, 3, Material.INK_SACK, (short) 2, "&aVIP", player.getName(), "setrank " + player.getName() + " vip", "rankcolor.vip", player);
        addColorItem(gui, 5, Material.INK_SACK, (short) 14, "&aVIP&6+", player.getName(), "setrank " + player.getName() + " vip+", "rankcolor.vip+", player);
        addColorItem(gui, 6, Material.INK_SACK, (short) 6, "&bMVP", player.getName(), "setrank " + player.getName() + " mvp", "rankcolor.mvp", player);
        addColorItem(gui, 19, Material.INK_SACK, (short) 1, "&bMVP&c+", player.getName(), "setrank " + player.getName() + " mvp+red", "rankcolor.mvp+.red", player);
        addColorItem(gui, 20, Material.INK_SACK, (short) 2, "&bMVP&a+", player.getName(), "setrank " + player.getName() + " mvp+green", "rankcolor.mvp+.green", player);
        addColorItem(gui, 21, Material.INK_SACK, (short) 9, "&bMVP&d+", player.getName(), "setrank " + player.getName() + " mvp+pink", "rankcolor.mvp+.pink", player);
        addColorItem(gui, 22, Material.INK_SACK, (short) 4, "&bMVP&9+", player.getName(), "setrank " + player.getName() + " mvp+blue", "rankcolor.mvp+.blue", player);
        addColorItem(gui, 23, Material.INK_SACK, (short) 14, "&bMVP&6+", player.getName(), "setrank " + player.getName() + " mvp+orange", "rankcolor.mvp+.orange", player);
        addColorItem(gui, 24, Material.INK_SACK, (short) 6, "&bMVP&b+", player.getName(), "setrank " + player.getName() + " mvp+cyan", "rankcolor.mvp+.cyan", player);
        addColorItem(gui, 25, Material.INK_SACK, (short) 0, "&bMVP&0+", player.getName(), "setrank " + player.getName() + " mvp+black", "rankcolor.mvp+.black", player);
        addColorItem(gui, 28, Material.INK_SACK, (short) 1, "&6MVP&c++", player.getName(), "setrank " + player.getName() + " mvp++red", "rankcolor.mvp++.red", player);
        addColorItem(gui, 29, Material.INK_SACK, (short) 2, "&6MVP&a++", player.getName(), "setrank " + player.getName() + " mvp++green", "rankcolor.mvp++.green", player);
        addColorItem(gui, 30, Material.INK_SACK, (short) 9, "&6MVP&d++", player.getName(), "setrank " + player.getName() + " mvp++pink", "rankcolor.mvp++.pink", player);
        addColorItem(gui, 31, Material.INK_SACK, (short) 4, "&6MVP&9++", player.getName(), "setrank " + player.getName() + " mvp++blue", "rankcolor.mvp++.blue", player);
        addColorItem(gui, 32, Material.INK_SACK, (short) 14, "&6MVP&6++", player.getName(), "setrank " + player.getName() + " mvp++orange", "rankcolor.mvp++.orange", player);
        addColorItem(gui, 33, Material.INK_SACK, (short) 6, "&6MVP&b++", player.getName(), "setrank " + player.getName() + " mvp++cyan", "rankcolor.mvp++.cyan", player);
        addColorItem(gui, 34, Material.INK_SACK, (short) 0, "&6MVP&0++", player.getName(), "setrank " + player.getName() + " mvp++black", "rankcolor.mvp++.black", player);
        addColorItem(gui, 2, Material.INK_SACK, (short) 0, "&7Default", player.getName(), "setrank " + player.getName() + " default1", "rankcolor.default1", player);
        addColorItem(gui, 4, Material.INK_SACK, (short) 1, "&aVIP&c+", player.getName(), "setrank " + player.getName() + " vip+red1", "rankcolor.vip+red1", player);
        addColorItem(gui, 3, Material.INK_SACK, (short) 2, "&aVIP", player.getName(), "setrank " + player.getName() + " vip1", "rankcolor.vip1", player);
        addColorItem(gui, 5, Material.INK_SACK, (short) 14, "&aVIP&6+", player.getName(), "setrank " + player.getName() + " vip+1", "rankcolor.vip+1", player);
        addColorItem(gui, 6, Material.INK_SACK, (short) 6, "&bMVP", player.getName(), "setrank " + player.getName() + " mvp1", "rankcolor.mvp1", player);
        addColorItem(gui, 19, Material.INK_SACK, (short) 1, "&bMVP&c+", player.getName(), "setrank " + player.getName() + " mvp+red1", "rankcolor.mvp+.red1", player);
        addColorItem(gui, 20, Material.INK_SACK, (short) 2, "&bMVP&a+", player.getName(), "setrank " + player.getName() + " mvp+green1", "rankcolor.mvp+.green1", player);
        addColorItem(gui, 21, Material.INK_SACK, (short) 9, "&bMVP&d+", player.getName(), "setrank " + player.getName() + " mvp+pink1", "rankcolor.mvp+.pink1", player);
        addColorItem(gui, 22, Material.INK_SACK, (short) 4, "&bMVP&9+", player.getName(), "setrank " + player.getName() + " mvp+blue1", "rankcolor.mvp+.blue1", player);
        addColorItem(gui, 23, Material.INK_SACK, (short) 14, "&bMVP&6+", player.getName(), "setrank " + player.getName() + " mvp+orange1", "rankcolor.mvp+.orange1", player);
        addColorItem(gui, 24, Material.INK_SACK, (short) 6, "&bMVP&b+", player.getName(), "setrank " + player.getName() + " mvp+cyan1", "rankcolor.mvp+.cyan1", player);
        addColorItem(gui, 25, Material.INK_SACK, (short) 0, "&bMVP&0+", player.getName(), "setrank " + player.getName() + " mvp+black1", "rankcolor.mvp+.black1", player);
        addColorItem(gui, 28, Material.INK_SACK, (short) 1, "&6MVP&c++", player.getName(), "setrank " + player.getName() + " mvp++red1", "rankcolor.mvp++.red1", player);
        addColorItem(gui, 29, Material.INK_SACK, (short) 2, "&6MVP&a++", player.getName(), "setrank " + player.getName() + " mvp++green1", "rankcolor.mvp++.green1", player);
        addColorItem(gui, 30, Material.INK_SACK, (short) 9, "&6MVP&d++", player.getName(), "setrank " + player.getName() + " mvp++pink1", "rankcolor.mvp++.pink1", player);
        addColorItem(gui, 31, Material.INK_SACK, (short) 4, "&6MVP&9++", player.getName(), "setrank " + player.getName() + " mvp++blue1", "rankcolor.mvp++.blue1", player);
        addColorItem(gui, 32, Material.INK_SACK, (short) 14, "&6MVP&6++", player.getName(), "setrank " + player.getName() + " mvp++orange1", "rankcolor.mvp++.orange1", player);
        addColorItem(gui, 33, Material.INK_SACK, (short) 6, "&6MVP&b++", player.getName(), "setrank " + player.getName() + " mvp++cyan1", "rankcolor.mvp++.cyan1", player);
        addColorItem(gui, 34, Material.INK_SACK, (short) 0, "&6MVP&0++", player.getName(), "setrank " + player.getName() + " mvp++black1", "rankcolor.mvp++.black1", player);
        addColorItem(gui, 2, Material.INK_SACK, (short) 0, "&7Default", player.getName(), "setrank " + player.getName() + " default2", "rankcolor.default2", player);
        addColorItem(gui, 4, Material.INK_SACK, (short) 1, "&aVIP&c+", player.getName(), "setrank " + player.getName() + " vip+red2", "rankcolor.vip+red2", player);
        addColorItem(gui, 3, Material.INK_SACK, (short) 2, "&aVIP", player.getName(), "setrank " + player.getName() + " vip2", "rankcolor.vip2", player);
        addColorItem(gui, 5, Material.INK_SACK, (short) 14, "&aVIP&6+", player.getName(), "setrank " + player.getName() + " vip+2", "rankcolor.vip+2", player);
        addColorItem(gui, 6, Material.INK_SACK, (short) 6, "&bMVP", player.getName(), "setrank " + player.getName() + " mvp2", "rankcolor.mvp2", player);
        addColorItem(gui, 19, Material.INK_SACK, (short) 1, "&bMVP&c+", player.getName(), "setrank " + player.getName() + " mvp+red2", "rankcolor.mvp+.red2", player);
        addColorItem(gui, 20, Material.INK_SACK, (short) 2, "&bMVP&a+", player.getName(), "setrank " + player.getName() + " mvp+green2", "rankcolor.mvp+.green2", player);
        addColorItem(gui, 21, Material.INK_SACK, (short) 9, "&bMVP&d+", player.getName(), "setrank " + player.getName() + " mvp+pink2", "rankcolor.mvp+.pink2", player);
        addColorItem(gui, 22, Material.INK_SACK, (short) 4, "&bMVP&9+", player.getName(), "setrank " + player.getName() + " mvp+blue2", "rankcolor.mvp+.blue2", player);
        addColorItem(gui, 23, Material.INK_SACK, (short) 14, "&bMVP&6+", player.getName(), "setrank " + player.getName() + " mvp+orange2", "rankcolor.mvp+.orange2", player);
        addColorItem(gui, 24, Material.INK_SACK, (short) 6, "&bMVP&b+", player.getName(), "setrank " + player.getName() + " mvp+cyan2", "rankcolor.mvp+.cyan2", player);
        addColorItem(gui, 25, Material.INK_SACK, (short) 0, "&bMVP&0+", player.getName(), "setrank " + player.getName() + " mvp+black2", "rankcolor.mvp+.black2", player);
        addColorItem(gui, 2, Material.INK_SACK, (short) 0, "&7Default", player.getName(), "setrank " + player.getName() + " default3", "rankcolor.default3", player);
        addColorItem(gui, 4, Material.INK_SACK, (short) 1, "&aVIP&c+", player.getName(), "setrank " + player.getName() + " vip+red3", "rankcolor.vip+red3", player);
        addColorItem(gui, 3, Material.INK_SACK, (short) 2, "&aVIP", player.getName(), "setrank " + player.getName() + " vip3", "rankcolor.vip3", player);
        addColorItem(gui, 5, Material.INK_SACK, (short) 14, "&aVIP&6+", player.getName(), "setrank " + player.getName() + " vip+3", "rankcolor.vip+3", player);
        addColorItem(gui, 6, Material.INK_SACK, (short) 6, "&bMVP", player.getName(), "setrank " + player.getName() + " mvp3", "rankcolor.mvp3", player);
        addColorItem(gui, 2, Material.INK_SACK, (short) 0, "&7Default", player.getName(), "setrank " + player.getName() + " default4", "rankcolor.default4", player);
        addColorItem(gui, 4, Material.INK_SACK, (short) 1, "&aVIP&c+", player.getName(), "setrank " + player.getName() + " vip+red4", "rankcolor.vip+red4", player);
        addColorItem(gui, 3, Material.INK_SACK, (short) 2, "&aVIP", player.getName(), "setrank " + player.getName() + " vip4", "rankcolor.vip4", player);
        addColorItem(gui, 5, Material.INK_SACK, (short) 14, "&aVIP&6+", player.getName(), "setrank " + player.getName() + " vip+4", "rankcolor.vip+4", player);
        addColorItem(gui, 3, Material.INK_SACK, (short) 2, "&aVIP", player.getName(), "setrank " + player.getName() + " vip5", "rankcolor.vip5", player);
        addColorItem(gui, 2, Material.INK_SACK, (short) 0, "&7Default", player.getName(), "setrank " + player.getName() + " default5", "rankcolor.default5", player);
        player.openInventory(gui);
        return true;
    }

    private void addColorItem(Inventory inv, int slot, Material material, short data, String name, String playerName, String command, String permission, Player player) {
        // التحقق من الصلاحية أولاً
        if (!player.hasPermission(permission)) {
            return;  // إذا لم يكن يملك الصلاحية، لا نضيف العنصر
        }

        ItemStack item = new ItemStack(material, 1, data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        String description = ChatColor.translateAlternateColorCodes('&', descriptionWithPlayerName(permission, playerName));
        List<String> lore = Arrays.asList(description, ChatColor.MAGIC + "CMD:" + command);
        meta.setLore(lore);

        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }

    private String descriptionWithPlayerName(String permission, String playerName) {
        switch (permission) {
            case "rankcolor.default":
                return "&7" + playerName + "&7: Hello";
            case "rankcolor.vip":
                return "&a[VIP] " + playerName + "&f: Hello";
            case "rankcolor.vip+red":
                return "&a[VIP&c+&a] " + playerName + "&f: Hello";
            case "rankcolor.vip+":
                return "&a[VIP&6+&a] " + playerName + "&f: Hello";
            case "rankcolor.mvp":
                return "&b[MVP&b] " + playerName + "&f: Hello";
            case "rankcolor.mvp+.red":
                return "&b[MVP&c+&b] " + playerName + "&f: Hello";
            case "rankcolor.mvp+.green":
                return "&b[MVP&a+&b] " + playerName + "&f: Hello";
            case "rankcolor.mvp+.pink":
                return "&b[MVP&d+&b] " + playerName + "&f: Hello";
            case "rankcolor.mvp+.blue":
                return "&b[MVP&9+&b] " + playerName + "&f: Hello";
            case "rankcolor.mvp+.orange":
                return "&b[MVP&6+&b] " + playerName + "&f: Hello";
            case "rankcolor.mvp+.cyan":
                return "&b[MVP&b+&b] " + playerName + "&f: Hello";
            case "rankcolor.mvp+.black":
                return "&b[MVP&0+&b] " + playerName + "&f: Hello";
            case "rankcolor.mvp++.red":
                return "&6[MVP&c++&6] " + playerName + "&f: Hello";
            case "rankcolor.mvp++.green":
                return "&6[MVP&a++&6] " + playerName + "&f: Hello";
            case "rankcolor.mvp++.pink":
                return "&6[MVP&d++&6] " + playerName + "&f: Hello";
            case "rankcolor.mvp++.blue":
                return "&6[MVP&9++&6] " + playerName + "&f: Hello";
            case "rankcolor.mvp++.orange":
                return "&6[MVP&6++&6] " + playerName + "&f: Hello";
            case "rankcolor.mvp++.cyan":
                return "&6[MVP&b++&6] " + playerName + "&f: Hello";
            case "rankcolor.mvp++.black":
                return "&6[MVP&0++&6] " + playerName + "&f: Hello";
            case "rankcolor.default1":
                return "&7" + playerName + "&7: Hello";
            case "rankcolor.vip1":
                return "&a[VIP] " + playerName + "&f: Hello";
            case "rankcolor.vip+red1":
                return "&a[VIP&c+&a] " + playerName + "&f: Hello";
            case "rankcolor.vip+1":
                return "&a[VIP&6+&a] " + playerName + "&f: Hello";
            case "rankcolor.mvp1":
                return "&b[MVP&b] " + playerName + "&f: Hello";
            case "rankcolor.mvp+.red1":
                return "&b[MVP&c+&b] " + playerName + "&f: Hello";
            case "rankcolor.mvp+.green1":
                return "&b[MVP&a+&b] " + playerName + "&f: Hello";
            case "rankcolor.mvp+.pink1":
                return "&b[MVP&d+&b] " + playerName + "&f: Hello";
            case "rankcolor.mvp+.blue1":
                return "&b[MVP&9+&b] " + playerName + "&f: Hello";
            case "rankcolor.mvp+.orange1":
                return "&b[MVP&6+&b] " + playerName + "&f: Hello";
            case "rankcolor.mvp+.cyan1":
                return "&b[MVP&b+&b] " + playerName + "&f: Hello";
            case "rankcolor.mvp+.black1":
                return "&b[MVP&0+&b] " + playerName + "&f: Hello";
            case "rankcolor.mvp++.red1":
                return "&6[MVP&c++&6] " + playerName + "&f: Hello";
            case "rankcolor.mvp++.green1":
                return "&6[MVP&a++&6] " + playerName + "&f: Hello";
            case "rankcolor.mvp++.pink1":
                return "&6[MVP&d++&6] " + playerName + "&f: Hello";
            case "rankcolor.mvp++.blue1":
                return "&6[MVP&9++&6] " + playerName + "&f: Hello";
            case "rankcolor.mvp++.orange1":
                return "&6[MVP&6++&6] " + playerName + "&f: Hello";
            case "rankcolor.mvp++.cyan1":
                return "&6[MVP&b++&6] " + playerName + "&f: Hello";
            case "rankcolor.mvp++.black1":
                return "&6[MVP&0++&6] " + playerName + "&f: Hello";
            case "rankcolor.default2":
                return "&7" + playerName + "&7: Hello";
            case "rankcolor.vip2":
                return "&a[VIP] " + playerName + "&f: Hello";
            case "rankcolor.vip+red2":
                return "&a[VIP&c+&a] " + playerName + "&f: Hello";
            case "rankcolor.vip+2":
                return "&a[VIP&6+&a] " + playerName + "&f: Hello";
            case "rankcolor.mvp2":
                return "&b[MVP&b] " + playerName + "&f: Hello";
            case "rankcolor.mvp+.red2":
                return "&b[MVP&c+&b] " + playerName + "&f: Hello";
            case "rankcolor.mvp+.green2":
                return "&b[MVP&a+&b] " + playerName + "&f: Hello";
            case "rankcolor.mvp+.pink2":
                return "&b[MVP&d+&b] " + playerName + "&f: Hello";
            case "rankcolor.mvp+.blue2":
                return "&b[MVP&9+&b] " + playerName + "&f: Hello";
            case "rankcolor.mvp+.orange2":
                return "&b[MVP&6+&b] " + playerName + "&f: Hello";
            case "rankcolor.mvp+.cyan2":
                return "&b[MVP&b+&b] " + playerName + "&f: Hello";
            case "rankcolor.mvp+.black2":
                return "&b[MVP&0+&b] " + playerName + "&f: Hello";
            case "rankcolor.default3":
                return "&7" + playerName + "&7: Hello";
            case "rankcolor.vip3":
                return "&a[VIP] " + playerName + "&f: Hello";
            case "rankcolor.vip+red3":
                return "&a[VIP&c+&a] " + playerName + "&f: Hello";
            case "rankcolor.vip+3":
                return "&a[VIP&6+&a] " + playerName + "&f: Hello";
            case "rankcolor.mvp3":
                return "&b[MVP&b] " + playerName + "&f: Hello";
            case "rankcolor.default4":
                return "&7" + playerName + "&7: Hello";
            case "rankcolor.vip4":
                return "&a[VIP] " + playerName + "&f: Hello";
            case "rankcolor.vip+red4":
                return "&a[VIP&c+&a] " + playerName + "&f: Hello";
            case "rankcolor.vip+4":
                return "&a[VIP&6+&a] " + playerName + "&f: Hello";
            case "rankcolor.default5":
                return "&7" + playerName + "&7: Hello";
            case "rankcolor.vip5":
                return "&a[VIP] " + playerName + "&f: Hello";
            default:
                return "&b[MVP&7+&b] " + playerName + "&f: Hello";
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player player = (Player) e.getWhoClicked();

        if (!e.getView().getTitle().equals(GUI_TITLE)) return;

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        if (!meta.hasLore()) return;

        for (String line : meta.getLore()) {
            if (line.startsWith(ChatColor.MAGIC + "CMD:")) {
                String command = ChatColor.stripColor(line.replace("CMD:", "")).trim();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                player.sendMessage(ChatColor.GREEN + "You have successfully changed your rank color!");
                player.closeInventory();
                return;
            }
        }
    }
}
