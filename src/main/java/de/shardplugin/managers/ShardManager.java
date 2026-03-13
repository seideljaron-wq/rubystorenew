package de.shardplugin.managers;

import de.shardplugin.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShardManager {

    private final Main plugin;
    private final Map<UUID, Long> data = new HashMap<>();
    private File file;
    private FileConfiguration cfg;

    public ShardManager(Main plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "shards.yml");
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        cfg = YamlConfiguration.loadConfiguration(file);
        if (cfg.getConfigurationSection("shards") != null) {
            for (String key : cfg.getConfigurationSection("shards").getKeys(false)) {
                try { data.put(UUID.fromString(key), cfg.getLong("shards." + key)); }
                catch (Exception ignored) {}
            }
        }
    }

    public void save() {
        data.forEach((uuid, amount) -> cfg.set("shards." + uuid, amount));
        try { cfg.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public long get(UUID uuid) {
        return data.getOrDefault(uuid, 0L);
    }

    public void add(UUID uuid, long amount) {
        data.put(uuid, get(uuid) + amount);
        save();
    }

    public boolean remove(UUID uuid, long amount) {
        long cur = get(uuid);
        if (cur < amount) return false;
        data.put(uuid, cur - amount);
        save();
        return true;
    }

    public String color(String key) {
        String msg = plugin.getConfig().getString("messages." + key, "&cMissing: " + key);
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', msg);
    }
}
