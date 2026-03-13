package de.shardplugin.managers;

import de.shardplugin.Main;
import org.bukkit.Bukkit;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DiscordManager {

    private final Main plugin;
    private static final ZoneId TZ = ZoneId.of("Europe/Berlin");
    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter T = DateTimeFormatter.ofPattern("HH:mm:ss");

    public DiscordManager(Main plugin) { this.plugin = plugin; }

    private String now(DateTimeFormatter fmt) { return ZonedDateTime.now(TZ).format(fmt); }

    // ─── Shard give ─────────────────────────────────────────────────────────────
    public void logGive(String from, String to, long amount) {
        send(plugin.getConfig().getString("discord.shard-webhook"),
             embed("💎 Shards Given",
                   "**" + from + "** gave **" + fmt(amount) + " Shards** to **" + to + "**",
                   0x00D26A,
                   field("👤 From", from), field("🎯 To", to), field("💎 Amount", fmt(amount))));
    }

    // ─── Shard remove ────────────────────────────────────────────────────────────
    public void logRemove(String from, String target, long amount) {
        send(plugin.getConfig().getString("discord.shard-webhook"),
             embed("🗑️ Shards Removed",
                   "**" + from + "** removed **" + fmt(amount) + " Shards** from **" + target + "**",
                   0xED4245,
                   field("👤 Admin", from), field("🎯 Player", target), field("💎 Amount", fmt(amount))));
    }

    // ─── Admin change ────────────────────────────────────────────────────────────
    public void logAdminChange(String by, String target, boolean added) {
        String title = added ? "🛡️ Admin Added" : "🚫 Admin Removed";
        String desc  = added ? "**" + by + "** granted admin to **" + target + "**"
                             : "**" + by + "** revoked admin from **" + target + "**";
        send(plugin.getConfig().getString("discord.shard-webhook"),
             embed(title, desc, added ? 0x5865F2 : 0xFEE75C,
                   field("👑 By", by), field("🎯 Player", target)));
    }

    // ─── Reload ──────────────────────────────────────────────────────────────────
    public void logReload(String by) {
        send(plugin.getConfig().getString("discord.shard-webhook"),
             embed("🔄 Plugin Reloaded", "**" + by + "** reloaded ShardPlugin.", 0xFFFF00));
    }

    // ─── Ruby Store purchase ─────────────────────────────────────────────────────
    public void logPurchase(String player, String item, int price) {
        send(plugin.getConfig().getString("discord.store-webhook"),
             embed("🛍️ Ruby Store Purchase",
                   "**" + player + "** bought an item from the Ruby Store.",
                   0xA855F7,
                   field("👤 Player", player), field("📦 Item", item), field("💎 Price", price + " Shards")));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────
    private String fmt(long v) { return String.format("%,d", v); }

    private String field(String name, String value) {
        return "{\"name\":\"" + esc(name) + "\",\"value\":\"" + esc(value) + "\",\"inline\":true}";
    }

    private String embed(String title, String desc, int color, String... fields) {
        StringBuilder f = new StringBuilder();
        if (fields.length > 0) {
            f.append(",\"fields\":[");
            for (int i = 0; i < fields.length; i++) {
                f.append(fields[i]);
                if (i < fields.length - 1) f.append(",");
            }
            f.append("]");
        }
        return "{\"embeds\":[{\"title\":\"" + esc(title) + "\","
             + "\"description\":\"" + esc(desc) + "\","
             + "\"color\":" + color
             + f
             + ",\"footer\":{\"text\":\"📅 " + now(D) + "  ⏰ " + now(T) + " (German Time)\"}"
             + "}]}";
    }

    private void send(String webhookUrl, String payload) {
        if (webhookUrl == null || webhookUrl.isEmpty()) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(webhookUrl).openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Content-Type", "application/json");
                c.setRequestProperty("User-Agent", "ShardPlugin/3.0");
                c.setDoOutput(true);
                try (OutputStream os = c.getOutputStream()) {
                    os.write(payload.getBytes(StandardCharsets.UTF_8));
                }
                c.getResponseCode();
                c.disconnect();
            } catch (Exception e) {
                plugin.getLogger().warning("Discord webhook failed: " + e.getMessage());
            }
        });
    }

    private String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
