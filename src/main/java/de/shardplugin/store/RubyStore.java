package de.shardplugin.store;

import de.shardplugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RubyStore {

    private final Main plugin;
    private final List<ItemStack> items = new ArrayList<>();
    private File file;
    private FileConfiguration cfg;

    public static final String MAIN_TITLE    = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Ruby Shop";
    public static final String CONFIRM_TITLE = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Ruby Shop " + ChatColor.DARK_GRAY + "| " + ChatColor.WHITE + "Confirm";

    // slots 0-26 = items, slots 27-35 = bottom border row
    private static final int GUI_SIZE = 36;

    public RubyStore(Main plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "rubystore.yml");
        if (!file.exists()) return;
        cfg = YamlConfiguration.loadConfiguration(file);
        int count = cfg.getInt("count", 0);
        for (int i = 0; i < count; i++) {
            ItemStack it = cfg.getItemStack("item." + i);
            if (it != null) items.add(it);
        }
    }

    public void setItems(List<ItemStack> newItems) {
        items.clear();
        // max 27
        for (int i = 0; i < newItems.size() && i < 27; i++) items.add(newItems.get(i));
        if (file == null) file = new File(plugin.getDataFolder(), "rubystore.yml");
        cfg = new YamlConfiguration();
        cfg.set("count", items.size());
        for (int i = 0; i < items.size(); i++) cfg.set("item." + i, items.get(i));
        try { cfg.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public boolean hasItems() { return !items.isEmpty(); }
    public List<ItemStack> getItems() { return items; }
    public int getPrice() { return plugin.getConfig().getInt("settings.store-price", 200); }

    // ─── Open main shop (4 rows: 3 item rows + 1 border row) ────────────────────

    public void openShop(Player player) {
        Inventory inv = Bukkit.createInventory(null, GUI_SIZE, MAIN_TITLE);

        // Bottom border row (slots 27-35) = pink glass
        ItemStack fill = glass(Material.PINK_STAINED_GLASS_PANE, " ");
        for (int i = 27; i < 36; i++) inv.setItem(i, fill);

        // Place items in slots 0-26
        for (int i = 0; i < items.size(); i++) {
            ItemStack display = items.get(i).clone();
            ItemMeta meta = display.getItemMeta();
            if (meta == null) meta = Bukkit.getItemFactory().getItemMeta(display.getType());
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.RED + "Price: " + getPrice() + " Shards");
            meta.setLore(lore);
            display.setItemMeta(meta);
            inv.setItem(i, display);
        }

        player.openInventory(inv);
    }

    // ─── Open confirm GUI ────────────────────────────────────────────────────────

    public void openConfirm(Player player, ItemStack item) {
        Inventory inv = Bukkit.createInventory(null, 9, CONFIRM_TITLE);

        // Gray filler
        ItemStack gray = glass(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) inv.setItem(i, gray);

        inv.setItem(0, glass(Material.RED_STAINED_GLASS_PANE,
                ChatColor.RED + "" + ChatColor.BOLD + "✗ Cancel"));
        inv.setItem(4, stripPriceLore(item));
        inv.setItem(8, glass(Material.LIME_STAINED_GLASS_PANE,
                ChatColor.GREEN + "" + ChatColor.BOLD + "✔ Buy for " + getPrice() + " Shards"));

        player.openInventory(inv);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    private ItemStack glass(Material mat, String name) {
        ItemStack it = new ItemStack(mat);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(name);
        it.setItemMeta(m);
        return it;
    }

    public ItemStack stripPriceLore(ItemStack item) {
        ItemStack copy = item.clone();
        ItemMeta meta = copy.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<String> lore = new ArrayList<>(meta.getLore());
            if (lore.size() >= 2) {
                lore.remove(lore.size() - 1);
                lore.remove(lore.size() - 1);
            }
            meta.setLore(lore.isEmpty() ? null : lore);
            copy.setItemMeta(meta);
        }
        return copy;
    }

    public String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
            return ChatColor.stripColor(item.getItemMeta().getDisplayName());
        String n = item.getType().name().toLowerCase().replace("_", " ");
        StringBuilder sb = new StringBuilder();
        for (String w : n.split(" "))
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
        return sb.toString().trim();
    }
}
