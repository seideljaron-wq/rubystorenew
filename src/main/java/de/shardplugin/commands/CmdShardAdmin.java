package de.shardplugin.commands;

import de.shardplugin.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class CmdShardAdmin implements CommandExecutor {

    private final Main p;
    public CmdShardAdmin(Main p) { this.p = p; }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        boolean isSA = s instanceof Player
                ? p.getAdmins().isSuperAdmin(s.getName())
                : true; // console can always

        if (!isSA) { s.sendMessage(ChatColor.RED + "Only javakuba can use this command."); return true; }
        if (a.length == 0) { sendUsage(s); return true; }

        // /shardadmin reload
        if (a[0].equalsIgnoreCase("reload")) {
            p.reloadConfig();
            s.sendMessage(ChatColor.GREEN + "Config reloaded!");
            p.getDiscord().logReload(s.getName());
            return true;
        }

        // /shardadmin add <player> | /shardadmin remove <player>
        if (a.length < 2) { sendUsage(s); return true; }

        String target = a[1];

        if (a[0].equalsIgnoreCase("add")) {
            if (!p.getAdmins().addAdmin(target)) {
                s.sendMessage(ChatColor.YELLOW + target + " is already an admin.");
            } else {
                s.sendMessage(ChatColor.GREEN + target + " is now an admin.");
                p.getDiscord().logAdminChange(s.getName(), target, true);
            }
        } else if (a[0].equalsIgnoreCase("remove")) {
            if (!p.getAdmins().removeAdmin(target)) {
                s.sendMessage(ChatColor.YELLOW + target + " is not an admin.");
            } else {
                s.sendMessage(ChatColor.RED + target + " is no longer an admin.");
                p.getDiscord().logAdminChange(s.getName(), target, false);
            }
        } else {
            sendUsage(s);
        }
        return true;
    }

    private void sendUsage(CommandSender s) {
        s.sendMessage(ChatColor.RED + "Usage: /shardadmin <add|remove> <player>  |  /shardadmin reload");
    }
}
