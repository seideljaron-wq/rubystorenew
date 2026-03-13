package de.shardplugin.managers;

import de.shardplugin.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class AdminManager {

    private final Main plugin;
    private final Set<String> admins = new HashSet<>();
    private File file;
    private FileConfiguration cfg;

    public AdminManager(Main plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "admins.yml");
        if (!file.exists()) {
            // seed from config on first run
            plugin.getConfig().getStringList("settings.admins")
                  .stream().map(String::toLowerCase).forEach(admins::add);
            save();
            return;
        }
        cfg = YamlConfiguration.loadConfiguration(file);
        cfg.getStringList("admins").stream().map(String::toLowerCase).forEach(admins::add);
    }

    public void save() {
        if (file == null) file = new File(plugin.getDataFolder(), "admins.yml");
        if (cfg  == null) cfg  = new YamlConfiguration();
        cfg.set("admins", admins.stream().toList());
        try { cfg.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public boolean isAdmin(String name)  { return admins.contains(name.toLowerCase()); }
    public boolean isSuperAdmin(String name) {
        return name.equalsIgnoreCase(plugin.getConfig().getString("settings.superadmin", "javakuba"));
    }

    public boolean addAdmin(String name)    { boolean r = admins.add(name.toLowerCase());    if (r) save(); return r; }
    public boolean removeAdmin(String name) { boolean r = admins.remove(name.toLowerCase()); if (r) save(); return r; }
}
