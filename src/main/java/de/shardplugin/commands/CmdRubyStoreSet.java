package de.shardplugin.commands;

import de.shardplugin.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.List;

public class CmdRubyStoreSet implements CommandExecutor {

    private final Main p;

    private static final List<Material> SHULKERS = List.of(
        Material.SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.ORANGE_SHULKER_BOX,
        Material.MAGENTA_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX,
        Material.LIME_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.GRAY_SHULKER_BOX,
        Material.LIGHT_GRAY_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.PURPLE_SHULKER_BOX,
        Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.GREEN_SHULKER_BOX,
        Material.RED_SHULKER_BOX, Material.BLACK_SHULKER_BOX
    );

    public CmdRubyStoreSet(Main p) { this.p = p; }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!(s instanceof Player player)) { s.sendMessage("In-game only."); return true; }

        if (!p.getAdmins().isSuperAdmin(player.getName())) {
            player.sendMessage(ChatColor.RED + "Only javakuba can use this command.");
            return true;
        }

        ItemStack held = player.getInventory().getItemInMainHand();
        if (held == null || !SHULKERS.contains(held.getType())) {
            player.sendMessage(ChatColor.RED + "Hold a Shulker Box in your main hand!");
            return true;
        }

        if (!(held.getItemMeta() instanceof BlockStateMeta bsm) || !(bsm.getBlockState() instanceof ShulkerBox box)) {
            player.sendMessage(ChatColor.RED + "Could not read Shulker Box contents.");
            return true;
        }

        List<ItemStack> newItems = new ArrayList<>();
        for (ItemStack it : box.getInventory().getContents()) {
            if (it != null && it.getType() != Material.AIR) newItems.add(it.clone());
        }

        if (newItems.isEmpty()) {
            player.sendMessage(ChatColor.RED + "The Shulker Box is empty!");
            return true;
        }

        p.getRubyStore().setItems(newItems);
        player.sendMessage(ChatColor.GREEN + "Ruby Store updated with " + ChatColor.YELLOW + newItems.size() + ChatColor.GREEN + " items!");
        return true;
    }
}
