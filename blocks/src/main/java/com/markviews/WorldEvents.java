package com.markviews;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPotionEffectEvent.Cause;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSpawnChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;

import net.md_5.bungee.api.ChatColor;

public class WorldEvents implements Listener {

    private static Inventory sharedInv;

    // doing this instead of .GetOnlinePlayers() to prevent new joiners from overwriting shared inv with their own
    private ArrayList<Player> sharedInvUsers = new ArrayList<Player>();

    // dict to store tool durability when player starts using tool
    private Map<Player, Short> toolDurability = new HashMap<>();

    private int health = 20;
    private int food = 20;
    private float exp = 0;
    private int expLevel = 0;

    private Location spawnLocation = null;

    public void Setup() {
        sharedInv = Bukkit.createInventory(null, InventoryType.PLAYER);

        // set gamerules
        for (World world : Main.plugin.getServer().getWorlds()) {
            world.setGameRule(GameRule.KEEP_INVENTORY, true);
            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
            world.setGameRule(GameRule.NATURAL_REGENERATION, false);
        }
        
        new BukkitRunnable() {
            @Override
            public void run() {
                Player leastAir = null;

                for (Player player : sharedInvUsers) {

                    if (Main.plugin.syncInventory) {
                        CheckInvDiff(player);
                    }

                    if (Main.plugin.syncHealth && (leastAir == null || player.getRemainingAir() < leastAir.getRemainingAir())) {
                        leastAir = player;
                    }

                    if (Main.plugin.syncExp) {
                        CheckEXPDiff(player);
                    }

                }

                if (Main.plugin.syncHealth) {
                    // sync air levels
                    for (Player player : sharedInvUsers) {
                        if (player == leastAir) continue;
                        player.setRemainingAir(leastAir.getRemainingAir());
                    }
                }

            }
        }.runTaskTimer(Main.plugin, 0, 1);

        Main.plugin.protocolManager.addPacketListener(
                new PacketAdapter(Main.plugin, ListenerPriority.NORMAL, PacketType.Play.Client.BLOCK_DIG) {
                    @SuppressWarnings("deprecation")
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        if (!Main.plugin.syncInventory) return;

                        PacketContainer packet = event.getPacket();
                        EnumWrappers.PlayerDigType digType = packet.getPlayerDigTypes().getValues().get(0);

                        Player player = event.getPlayer();
                        int slot = player.getInventory().getHeldItemSlot();

                        ItemStack sharedItem = sharedInv.getItem(slot);
                        if (sharedItem == null) return;

                        short maxDurability = sharedItem.getType().getMaxDurability();
                        if (maxDurability == 0) return;

                        if (digType == EnumWrappers.PlayerDigType.START_DESTROY_BLOCK) {
                            toolDurability.put(player, sharedItem.getDurability());
                        } else if (digType == EnumWrappers.PlayerDigType.ABORT_DESTROY_BLOCK) {
                            toolDurability.remove(player);
                        } else if (digType == EnumWrappers.PlayerDigType.STOP_DESTROY_BLOCK) {
                            short durability_prev = toolDurability.getOrDefault(player, (short) 0);
                            short durability_current = sharedItem.getDurability();
                            short durability_new = (short) Math.max(durability_current, durability_prev + 1);
                            sharedItem.setDurability(durability_new);
                            sharedInv.setItem(slot, sharedItem);
                        }

                    }
                });

    }

    public void CheckEXPDiff(Player player) {
        float exp_other = player.getExp();
        int expLevel_other = player.getLevel();

        if (exp_other != exp || expLevel_other != expLevel) {

            int exp_diff = (int) ((exp_other - exp) * 100);
            int expLevel_diff = expLevel_other - expLevel;
            exp = exp_other;
            expLevel = expLevel_other;
            Broadcast(Main.plugin.log_exp, ChatColor.DARK_AQUA + player.getName() + ChatColor.GRAY + " changed exp by " + ChatColor.GOLD + exp_diff + ChatColor.GRAY + " and level by " + ChatColor.GOLD + expLevel_diff);

            for (Player otherPlayer : sharedInvUsers) {
                if (otherPlayer.equals(player)) continue;
                otherPlayer.setExp(exp);
                otherPlayer.setLevel(expLevel);
            }

        }
    }

    // check if player's inventory changed and update shared inventory
    public void CheckInvDiff(Player player) {
        Inventory inv = player.getInventory();
        for (int slot = 0; slot < 41; slot++) {

            ItemStack item = inv.getItem(slot);
            ItemStack sharedItem = sharedInv.getItem(slot);
            if (!Objects.equals(item, sharedItem)) {

                sharedInv.setItem(slot, item);

                // load slot for all other players
                for (Player otherPlayer : sharedInvUsers) {
                    if (otherPlayer.equals(player)) continue;

                    // if this player is using the slot, don't update it
                    boolean isUsingTool = (player.getInventory().getHeldItemSlot() == slot)
                            && toolDurability.containsKey(player);
                    // boolean isUsingTool = (otherPlayer.getInventory().getHeldItemSlot() == slot);
                    if (isUsingTool) continue;

                    otherPlayer.getInventory().setItem(slot, item);
                }

            }

        }
    }

    // sync health and food levels for all players
    private void SyncHealth() {
        // wait 1 tick so we don't have to cancel the events
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : sharedInvUsers) {
                    if (Main.plugin.syncHealth) {
                        double change = health - player.getHealth();
                        if (change > 0) {
                            player.setHealth(health);
                        } else if (change < 0) {
                            player.damage(-change);
                        }
                    }
                        
                    if (Main.plugin.syncFood) {
                        player.setFoodLevel(food);
                    }
                }
            }
        }.runTaskLater(Main.plugin, 1);
    }

    @EventHandler
    public void damage(EntityDamageEvent event) {
        if (!Main.plugin.syncHealth) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        int damage = (int) event.getDamage();
        DamageCause cause = event.getCause();
        if (cause == DamageCause.CUSTOM) return;
        health = Math.max(health - damage, 0);

        Broadcast(Main.plugin.log_health, ChatColor.DARK_AQUA + player.getName() + ChatColor.GRAY + " took " + ChatColor.GOLD + damage + ChatColor.GRAY + " damage from " + ChatColor.GOLD + cause);

        SyncHealth();
    }

    @EventHandler
    public void food(FoodLevelChangeEvent event) {
        if (!Main.plugin.syncFood) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        int oldFoodLevel = player.getFoodLevel();
        int newFoodLevel = event.getFoodLevel();
        int change = newFoodLevel - oldFoodLevel;
        food = clamp(food + change, 0, 20);

        if (event.getItem() != null) {
            String cause = event.getItem().getType().name();
            Broadcast(Main.plugin.log_food, ChatColor.DARK_AQUA + player.getName() + ChatColor.GRAY + " changed food level by " + ChatColor.GOLD + change + ChatColor.GRAY + " using " + ChatColor.GOLD + cause);
        } else {
            Broadcast(Main.plugin.log_food, ChatColor.DARK_AQUA + player.getName() + ChatColor.GRAY + " changed food level by " + ChatColor.GOLD + change);
        }

        SyncHealth();
    }

    @EventHandler
    public void regain(EntityRegainHealthEvent event) {
        if (!Main.plugin.syncHealth) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        int amount = (int) event.getAmount();
        health = Math.max(health + amount, 20);

        Broadcast(Main.plugin.log_health, ChatColor.DARK_AQUA + player.getName() + ChatColor.GRAY + " gained " + ChatColor.GOLD + amount + ChatColor.GRAY + " health");

        SyncHealth();
    }

    @EventHandler
    public void respawn(PlayerRespawnEvent event) {
        if (!Main.plugin.syncHealth) return;
        health = 20; // this feels kinda cheap.. maybe handle this better
        SyncHealth();

        if (Main.plugin.syncSpawn && spawnLocation != null) {
            event.setRespawnLocation(spawnLocation);
        }

    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Inventory inv = player.getInventory();

        // if there's someone else already using the shared inventory, sync with them
        if (sharedInvUsers.size() != 0) {
            // apply potion effects to new player
            if (Main.plugin.syncPotion) {
                for (PotionEffect effect : sharedInvUsers.get(0).getActivePotionEffects()) {
                    player.addPotionEffect(effect);
                }
            }
        } else {
            // first player. reset some values
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setExp(0);
            player.setLevel(0);
        }

        // sync this player with the shared inventory
        for (int slot = 0; slot < 41; slot++) {
            ItemStack item = sharedInv.getItem(slot);
            inv.setItem(slot, item);
        }
        sharedInvUsers.add(player);

        if (Main.plugin.syncHealth) {
            SyncHealth();
        }
    }

    @EventHandler
    public void leave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        sharedInvUsers.remove(player);
    }

    @EventHandler
    public void onEntityPotionEffect(EntityPotionEffectEvent event) {
        if (!Main.plugin.syncPotion) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getCause() == Cause.PLUGIN) return;

        Player player = (Player) event.getEntity();
        PotionEffect newEffect = event.getNewEffect();
        PotionEffect oldEffect = event.getOldEffect();

        if (newEffect != null) {
            Broadcast(Main.plugin.log_potion, ChatColor.DARK_AQUA + player.getName() + ChatColor.GRAY + " added effect " + ChatColor.GOLD + newEffect.getType().getKey() + ChatColor.GRAY + " for " + ChatColor.GOLD + newEffect.getDuration());
        }
        if (oldEffect != null) {
            Broadcast(Main.plugin.log_potion, ChatColor.DARK_AQUA + player.getName() + ChatColor.GRAY + " removed effect " + ChatColor.GOLD + oldEffect.getType().getKey());
        }

        for (Player otherPlayer : sharedInvUsers) {
            if (otherPlayer.equals(player)) continue;
            if (oldEffect != null) {
                otherPlayer.removePotionEffect(oldEffect.getType());
            }
            if (newEffect != null) {
                otherPlayer.addPotionEffect(newEffect);
            }
        }
    }

    @EventHandler
    public void changeSpawn(PlayerSpawnChangeEvent event) {
        if (!Main.plugin.syncSpawn) return;
        Player player = event.getPlayer();
        Location loc = player.getRespawnLocation();
        if (loc == null) return;

        Broadcast(Main.plugin.log_spawn, ChatColor.DARK_AQUA + player.getName() + ChatColor.GRAY + " changed spawn to " + ChatColor.GOLD + loc.getX() + ", " + loc.getY() + ", " + loc.getZ());
        spawnLocation = loc;
    }

    private void Broadcast(boolean display, String message) {
        if (!display) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

}
