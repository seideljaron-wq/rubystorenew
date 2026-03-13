package de.shardplugin.commands;

import de.shardplugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class CmdShardRemove implements CommandExecutor {

    private final Main p;
    public CmdShardRemove(Main p) { this.p = p; }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        boolean allowed = s.isOp()
                || (s instanceof Player && p.getAdmins().isAdmin(s.getName()))
                || (s instanceof Player && p.getAdmins().isSuperAdmin(s.getName()));

        if (!allowed) { s.sendMessage(ChatColor.RED + "No permission."); return true; }
        if (a.length != 2) { s.sendMessage(ChatColor.RED + "Usage: /shardremove <player> <amount>"); return true; }

        Player t = Bukkit.getPlayer(a[0]);
        if (t == null) { s.sendMessage(ChatColor.RED + "Player not found."); return true; }

        long amount;
        try { amount = Long.parseLong(a[1]); if (amount <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { s.sendMessage(ChatColor.RED + "Invalid amount."); return true; }

        long cur = p.getShards().get(t.getUniqueId());
        if (!p.getShards().remove(t.getUniqueId(), amount)) {
            s.sendMessage(ChatColor.RED + t.getName() + " only has " + ChatColor.YELLOW + String.format("%,d", cur) + ChatColor.RED + " Shards.");
            return true;
        }

        String who = (s instanceof Player) ? s.getName() : "Console";
        s.sendMessage(ChatColor.GREEN + "Removed " + ChatColor.YELLOW + String.format("%,d", amount) + ChatColor.GREEN + " Shards from " + ChatColor.YELLOW + t.getName() + ChatColor.GREEN + "!");
        t.sendMessage(ChatColor.RED + "An admin removed " + ChatColor.YELLOW + String.format("%,d", amount) + ChatColor.RED + " Shards from your account.");
        p.getDiscord().logRemove(who, t.getName(), amount);
        return true;
    }
}
