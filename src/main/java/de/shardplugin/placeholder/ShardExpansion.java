package de.shardplugin.placeholder;

import de.shardplugin.Main;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShardExpansion extends PlaceholderExpansion {

    private final Main plugin;
    public ShardExpansion(Main plugin) { this.plugin = plugin; }

    @Override public @NotNull String getIdentifier() { return "shards"; }
    @Override public @NotNull String getAuthor()     { return "javakuba"; }
    @Override public @NotNull String getVersion()    { return plugin.getDescription().getVersion(); }
    @Override public boolean persist()               { return true; }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "0";
        long v = plugin.getShards().get(player.getUniqueId());
        return switch (params.toLowerCase()) {
            case "amount"    -> String.valueOf(v);
            case "formatted" -> String.format("%,d", v);
            case "short"     -> toShort(v);
            default          -> null;
        };
    }

    private String toShort(long v) {
        if (v < 1_000)       return String.valueOf(v);
        if (v < 1_000_000)   return clean(v / 1_000.0) + "K";
        if (v < 1_000_000_000) return clean(v / 1_000_000.0) + "M";
        return clean(v / 1_000_000_000.0) + "B";
    }

    private String clean(double d) {
        return d % 1.0 == 0 ? String.format("%.0f", d) : String.format("%.1f", d);
    }
}
