package de.shardplugin.commands;

import de.shardplugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class CmdShardGive implements CommandExecutor {

    private final Main p;
    public CmdShardGive(Main p) { this.p = p; }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!s.isOp() && !(s instanceof Player && p.getAdmins().isAdmin(s.getName()))) {
            s.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (a.length != 2) { s.sendMessage(ChatColor.RED + "Usage: /shardgive <player> <amount>"); return true; }

        Player t = Bukkit.getPlayer(a[0]);
        if (t == null) { s.sendMessage(ChatColor.RED + "Player not found."); return true; }

        long amount;
        try { amount = Long.parseLong(a[1]); if (amount <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { s.sendMessage(ChatColor.RED + "Invalid amount."); return true; }

        p.getShards().add(t.getUniqueId(), amount);

        String who = (s instanceof Player) ? s.getName() : "Console";
        s.sendMessage(ChatColor.GREEN + "Gave " + ChatColor.YELLOW + String.format("%,d", amount) + ChatColor.GREEN + " Shards to " + ChatColor.YELLOW + t.getName() + ChatColor.GREEN + "!");
        t.sendMessage(ChatColor.GREEN + "You received " + ChatColor.YELLOW + String.format("%,d", amount) + ChatColor.GREEN + " Shards from " + ChatColor.YELLOW + who + ChatColor.GREEN + "!");
        p.getDiscord().logGive(who, t.getName(), amount);
        return true;
    }
}
