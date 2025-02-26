package com.markviews;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.ChatColor;

public class SettingsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        
        if (args.length != 2 || !sender.isOp()) {
            if (sender.isOp()) {
                sender.sendMessage("/settings [health, food, exp, potion, spawn, inventory] [true, false, log (true + logging)]");
            } else {
                sender.sendMessage(ChatColor.RED + "Read only. Only OPs can change settings.");
            }

            // display current settings
            sender.sendMessage(ChatColor.GRAY + "Health sync is " + (Main.plugin.syncHealth ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.GRAY + " and logging is " + (Main.plugin.log_health ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
            sender.sendMessage(ChatColor.GRAY + "Food sync is " + (Main.plugin.syncFood ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.GRAY + " and logging is " + (Main.plugin.log_food ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
            sender.sendMessage(ChatColor.GRAY + "Exp sync is " + (Main.plugin.syncExp ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.GRAY + " and logging is " + (Main.plugin.log_exp ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
            sender.sendMessage(ChatColor.GRAY + "Potion sync is " + (Main.plugin.syncPotion ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.GRAY + " and logging is " + (Main.plugin.log_potion ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
            sender.sendMessage(ChatColor.GRAY + "Spawn sync is " + (Main.plugin.syncSpawn ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.GRAY + " and logging is " + (Main.plugin.log_spawn ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
            sender.sendMessage(ChatColor.GRAY + "Inventory sync is " + (Main.plugin.syncInventory ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
            
            // display important gamerules
            
            for(World world : Main.plugin.getServer().getWorlds()) {
                sender.sendMessage(ChatColor.GRAY + "Important gamerules: " + world.getName());
                sender.sendMessage(ChatColor.GRAY + "KEEP_INVENTORY: " + (world.getGameRuleValue(GameRule.KEEP_INVENTORY) ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.GRAY + " (Recommended enabled)");
                sender.sendMessage(ChatColor.GRAY + "DO_IMMEDIATE_RESPAWN: " + (world.getGameRuleValue(GameRule.DO_IMMEDIATE_RESPAWN) ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.GRAY + " (Recommended enabled)");
                sender.sendMessage(ChatColor.GRAY + "NATURAL_REGENERATION: " + (world.getGameRuleValue(GameRule.NATURAL_REGENERATION) ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.GRAY + " (Recommended disabled)");
            }

            return true;
        }

        if (args[0].equalsIgnoreCase("health")) {
            if (args[1].equalsIgnoreCase("true")) {
                Main.plugin.syncHealth = true;
                Main.plugin.log_health = false;
            } else if (args[1].equalsIgnoreCase("false")) {
                Main.plugin.syncHealth = false;
                Main.plugin.log_health = false;
            } else if (args[1].equalsIgnoreCase("log")) {
                Main.plugin.syncHealth = true;
                Main.plugin.log_health = true;
            }

            sender.sendMessage(ChatColor.GRAY + "Health sync is now " + (Main.plugin.syncHealth ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.GRAY + " and logging is " + (Main.plugin.log_health ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
            return true;
        }

        if (args[0].equalsIgnoreCase("food")) {
            if (args[1].equalsIgnoreCase("true")) {
                Main.plugin.syncFood = true;
                Main.plugin.log_food = false;
            } else if (args[1].equalsIgnoreCase("false")) {
                Main.plugin.syncFood = false;
                Main.plugin.log_food = false;
            } else if (args[1].equalsIgnoreCase("log")) {
                Main.plugin.syncFood = true;
                Main.plugin.log_food = true;
            }

            sender.sendMessage(ChatColor.GRAY + "Food sync is now " + (Main.plugin.syncFood ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.GRAY + " and logging is " + (Main.plugin.log_food ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
            return true;
        }

        if (args[0].equalsIgnoreCase("exp")) {
            if (args[1].equalsIgnoreCase("true")) {
                Main.plugin.syncExp = true;
                Main.plugin.log_exp = false;
            } else if (args[1].equalsIgnoreCase("false")) {
                Main.plugin.syncExp = false;
                Main.plugin.log_exp = false;
            } else if (args[1].equalsIgnoreCase("log")) {
                Main.plugin.syncExp = true;
                Main.plugin.log_exp = true;
            }

            sender.sendMessage(ChatColor.GRAY + "Exp sync is now " + (Main.plugin.syncExp ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.GRAY + " and logging is " + (Main.plugin.log_exp ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
            return true;
        }

        if (args[0].equalsIgnoreCase("potion")) {
            if (args[1].equalsIgnoreCase("true")) {
                Main.plugin.syncPotion = true;
                Main.plugin.log_potion = false;
            } else if (args[1].equalsIgnoreCase("false")) {
                Main.plugin.syncPotion = false;
                Main.plugin.log_potion = false;
            } else if (args[1].equalsIgnoreCase("log")) {
                Main.plugin.syncPotion = true;
                Main.plugin.log_potion = true;
            }

            sender.sendMessage(ChatColor.GRAY + "Potion sync is now " + (Main.plugin.syncPotion ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.GRAY + " and logging is " + (Main.plugin.log_potion ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
            return true;
        }

        if (args[0].equalsIgnoreCase("spawn")) {
            if (args[1].equalsIgnoreCase("true")) {
                Main.plugin.syncSpawn = true;
                Main.plugin.log_spawn = false;
            } else if (args[1].equalsIgnoreCase("false")) {
                Main.plugin.syncSpawn = false;
                Main.plugin.log_spawn = false;
            } else if (args[1].equalsIgnoreCase("log")) {
                Main.plugin.syncSpawn = true;
                Main.plugin.log_spawn = true;
            }

            sender.sendMessage(ChatColor.GRAY + "Spawn sync is now " + (Main.plugin.syncSpawn ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.GRAY + " and logging is " + (Main.plugin.log_spawn ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
            return true;
        }

        if (args[0].equalsIgnoreCase("inventory")) {
            if (args[1].equalsIgnoreCase("true")) {
                Main.plugin.syncInventory = true;
            } else if (args[1].equalsIgnoreCase("false")) {
                Main.plugin.syncInventory = false;
            }

            sender.sendMessage(ChatColor.GRAY + "Inventory sync is now " + (Main.plugin.syncInventory ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
            return true;
        }

        return true;
    }

}
