package de.shardplugin.commands;

import de.shardplugin.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class CmdRubyStore implements CommandExecutor {

    private final Main p;
    public CmdRubyStore(Main p) { this.p = p; }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!(s instanceof Player player)) { s.sendMessage("In-game only."); return true; }
        if (!p.getRubyStore().hasItems()) {
            player.sendMessage(ChatColor.RED + "The Ruby Store has not been set up yet!");
            return true;
        }
        p.getRubyStore().openShop(player);
        return true;
    }
}
