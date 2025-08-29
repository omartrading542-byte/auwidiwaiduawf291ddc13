package com.omar.system64;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class NoBreak implements Listener {

    private final PvPLocationManager pvpLocationManager;

    public NoBreak(Main plugin) {
        this.pvpLocationManager = new PvPLocationManager(plugin);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().isOp()) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.getPlayer().isOp()) {
            event.setCancelled(true);
            return;
        }
        Material type = event.getBlock().getType();
        if (type == Material.LAVA || type == Material.STATIONARY_LAVA || event.getItemInHand().getType() == Material.LAVA_BUCKET) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        Material type = event.getClickedBlock().getType();
        if (type == Material.CHEST || type == Material.TRAPPED_CHEST || type == Material.FURNACE ||
                type == Material.BURNING_FURNACE || type == Material.DISPENSER || type == Material.HOPPER ||
                type == Material.DROPPER || type == Material.BREWING_STAND || type == Material.ENCHANTMENT_TABLE ||
                type == Material.ANVIL || type == Material.ENDER_CHEST || type == Material.WORKBENCH ||
                type == Material.WOODEN_DOOR || type == Material.IRON_DOOR || type == Material.BIRCH_DOOR ||
                type == Material.SPRUCE_DOOR || type == Material.IRON_TRAPDOOR) {
            if (!event.getPlayer().isOp()) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        Entity damagerEntity = event.getDamager();
        Entity targetEntity = event.getEntity();

        // ================== منع ضرب الكائنات الخاصة ==================
        if (damagerEntity instanceof Player) {
            Player damager = (Player) damagerEntity;

            if (!damager.isOp()) {
                // منع ضرب الكائنات البحرية (Squid)، الساحر (Witch)، كريبر وسكليتون
                if (targetEntity instanceof Squid || targetEntity instanceof Witch ||
                    targetEntity instanceof Creeper || targetEntity instanceof Skeleton) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // ================== التعامل مع اللاعبين ==================
        if (damagerEntity instanceof Player && targetEntity instanceof Player) {
            Player damager = (Player) damagerEntity;
            ItemStack item = damager.getItemInHand(); // 1.8 يستخدم getItemInHand()

            // السماح بالضرر إذا يضرب بسيف
            if (item != null && item.getType().name().endsWith("_SWORD")) return;

            // أي شيء آخر ممنوع
            event.setCancelled(true);
            return;
        }

        // ================== الأسهم الموجهة من لاعب ==================
        if (damagerEntity instanceof Arrow && targetEntity instanceof Player) {
            Arrow arrow = (Arrow) damagerEntity;
            if (arrow.getShooter() instanceof Player) {
                event.setCancelled(true);
                return;
            }
        }

        // ================== زومبي صغير أو وحوش تضرب لاعب ==================
        if (damagerEntity instanceof Zombie && ((Zombie) damagerEntity).isBaby() && targetEntity instanceof Player) {
            event.setCancelled(true);
            return;
        }

        if (damagerEntity instanceof Monster && targetEntity instanceof Player) {
            Player player = (Player) targetEntity;
            if (!player.isOp()) {
                event.setCancelled(true);
                return;
            }
        }

        // ================== لاعب يضرب زومبي أو حيوان ==================
        if (damagerEntity instanceof Player) {
            Player damager = (Player) damagerEntity;
            if (!damager.isOp() && (targetEntity instanceof Zombie || targetEntity instanceof Animals)) {
                event.setCancelled(true);
                return;
            }
        }

        // ================== منع اللاعبين في كرياتيف من ضرب الوحوش ==================
        if (damagerEntity instanceof Player && targetEntity instanceof Monster) {
            Player player = (Player) damagerEntity;
            if (player.getGameMode() == GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        }
    }





    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        if (!event.getPlayer().isOp()) event.setCancelled(true);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (!event.getPlayer().isOp()) event.setCancelled(true);
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!event.getPlayer().isOp() && event.getBucket() == Material.LAVA_BUCKET) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onIgnite(BlockIgniteEvent event) {
        if (event.getPlayer() != null && !event.getPlayer().isOp()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        // منع الضرر من الغرق
        if (event.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
            event.setCancelled(true);
        }

        // منع الضرر من النار أو الحريق أو اللّافا
        if (event.getCause() == EntityDamageEvent.DamageCause.FIRE
                || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK
                || event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof Creeper) {
            event.blockList().clear();
        }
    }
    
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player && event.getEntity() instanceof Monster) {
            event.setCancelled(true); // يمنع الوحش من استهداف اللاعب
        }
    }
    
    @EventHandler
    public void onAchievement(PlayerAchievementAwardedEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }
}
