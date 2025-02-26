package com.markviews;

import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

public class Main extends JavaPlugin {

    public static Main plugin;
    public ProtocolManager protocolManager;

    public boolean syncInventory = true;
    public boolean syncHealth = true;
    public boolean syncFood = true;
    public boolean syncExp = true;
    public boolean syncPotion = true;
    public boolean syncSpawn = true;

    public boolean log_health = true;
    public boolean log_food = true;
    public boolean log_exp = true;
    public boolean log_potion = true;
    public boolean log_spawn = true;

    @Override
    public void onEnable() {
        plugin = this;
        protocolManager = ProtocolLibrary.getProtocolManager();

        this.getCommand("settings").setExecutor(new SettingsCommand());
        WorldEvents worldEvents = new WorldEvents();
        worldEvents.Setup();
        getServer().getPluginManager().registerEvents(worldEvents, this);
    }

}
