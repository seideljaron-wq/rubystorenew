package de.shardplugin;

import de.shardplugin.commands.*;
import de.shardplugin.listeners.GuiListener;
import de.shardplugin.managers.AdminManager;
import de.shardplugin.managers.DiscordManager;
import de.shardplugin.managers.ShardManager;
import de.shardplugin.placeholder.ShardExpansion;
import de.shardplugin.store.RubyStore;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private ShardManager shardManager;
    private AdminManager adminManager;
    private DiscordManager discordManager;
    private RubyStore rubyStore;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        adminManager   = new AdminManager(this);
        discordManager = new DiscordManager(this);
        shardManager   = new ShardManager(this);
        rubyStore      = new RubyStore(this);

        getCommand("shardgive").setExecutor(new CmdShardGive(this));
        getCommand("shardremove").setExecutor(new CmdShardRemove(this));
        getCommand("shardadmin").setExecutor(new CmdShardAdmin(this));
        getCommand("rubystore").setExecutor(new CmdRubyStore(this));
        getCommand("rubystoreset").setExecutor(new CmdRubyStoreSet(this));

        getServer().getPluginManager().registerEvents(new GuiListener(this), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ShardExpansion(this).register();
            getLogger().info("PlaceholderAPI hooked — use %shards_amount% or %shards_short%");
        } else {
            getLogger().warning("PlaceholderAPI not found! Placeholders won't work.");
        }

        getLogger().info("ShardPlugin v3 enabled!");
    }

    @Override
    public void onDisable() {
        if (shardManager != null) shardManager.save();
        if (adminManager  != null) adminManager.save();
        getLogger().info("ShardPlugin disabled.");
    }

    public static Main get()               { return instance; }
    public ShardManager   getShards()      { return shardManager; }
    public AdminManager   getAdmins()      { return adminManager; }
    public DiscordManager getDiscord()     { return discordManager; }
    public RubyStore      getRubyStore()   { return rubyStore; }
}
