package de.shardplugin.listeners;

import de.shardplugin.Main;
import de.shardplugin.store.RubyStore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GuiListener implements Listener {

    private final Main plugin;
    private final Map<UUID, ItemStack> pending = new HashMap<>();
    // Track who is currently in our GUIs
    private final Set<UUID> inGui = new HashSet<>();

    public GuiListener(Main plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        String title = e.getView().getTitle();

        // Only cancel clicks inside our GUIs
        if (!title.equals(RubyStore.MAIN_TITLE) && !title.equals(RubyStore.CONFIRM_TITLE)) return;

        e.setCancelled(true);
        inGui.add(player.getUniqueId());

        // ── Main Shop ─────────────────────────────────────────────────────────
        if (title.equals(RubyStore.MAIN_TITLE)) {
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            if (clicked.getType() == Material.PINK_STAINED_GLASS_PANE) return;

            pending.put(player.getUniqueId(), clicked.clone());
            plugin.getRubyStore().openConfirm(player, clicked);
            return;
        }

        // ── Confirm GUI ───────────────────────────────────────────────────────
        if (title.equals(RubyStore.CONFIRM_TITLE)) {
            int slot = e.getRawSlot();

            if (slot == 0) {
                // Cancel → back to shop
                pending.remove(player.getUniqueId());
                plugin.getRubyStore().openShop(player);
                return;
            }

            if (slot == 8) {
                // Confirm purchase
                ItemStack item = pending.remove(player.getUniqueId());
                if (item == null) { player.closeInventory(); return; }

                int price = plugin.getRubyStore().getPrice();
                long balance = plugin.getShards().get(player.getUniqueId());

                if (balance < price) {
                    player.sendMessage(ChatColor.RED + "You don't have enough Shards! This item costs "
                            + ChatColor.YELLOW + price + " Shards" + ChatColor.RED + ".");
                    player.closeInventory();
                    return;
                }

                plugin.getShards().remove(player.getUniqueId(), price);

                ItemStack give = plugin.getRubyStore().stripPriceLore(item);

                // Close first, then give item 1 tick later to avoid inventory glitch
                player.closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(give);
                    leftover.values().forEach(drop ->
                            player.getWorld().dropItemNaturally(player.getLocation(), drop));

                    String name = plugin.getRubyStore().getItemName(give);
                    player.sendMessage(ChatColor.GREEN + "You purchased " + ChatColor.YELLOW + name
                            + ChatColor.GREEN + " for " + ChatColor.YELLOW + price + " Shards" + ChatColor.GREEN + "!");

                    plugin.getDiscord().logPurchase(player.getName(), name, price);

                    // Force inventory update to unfreeze it
                    player.updateInventory();
                    inGui.remove(player.getUniqueId());
                }, 1L);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;
        String title = e.getView().getTitle();
        if (title.equals(RubyStore.MAIN_TITLE) || title.equals(RubyStore.CONFIRM_TITLE)) {
            // Small delay then update inventory to unfreeze
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.updateInventory();
                inGui.remove(player.getUniqueId());
            }, 1L);
        }
    }
}
