package de.smp.admintools;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Central access point for all AdminTools config values.
 * Call {@link #reload()} to pick up changes from disk without restarting.
 */
public class PluginConfig {

    private final JavaPlugin plugin;
    private FileConfiguration cfg;

    public PluginConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        cfg = plugin.getConfig();
    }

    /** Re-reads config.yml from disk. */
    public void reload() {
        plugin.reloadConfig();
        cfg = plugin.getConfig();
    }

    // ── Permission checks ─────────────────────────────────────────────────────

    /**
     * Returns true if the sender is allowed to use the given command key
     * based on the permissions section of config.yml.
     *
     * Config value meanings:
     *   "op"        → sender must be an operator
     *   "all"       → always allowed
     *   anything else → treated as a Bukkit permission node
     */
    public boolean hasAccess(CommandSender sender, String commandKey) {
        String value = cfg.getString("permissions." + commandKey, "op");
        return switch (value.toLowerCase()) {
            case "op"  -> sender.isOp();
            case "all" -> true;
            default    -> sender.hasPermission(value);
        };
    }

    /** Human-readable description of the required permission for error messages. */
    public String accessDescription(String commandKey) {
        String value = cfg.getString("permissions." + commandKey, "op");
        return switch (value.toLowerCase()) {
            case "op"  -> "operator";
            case "all" -> "everyone";
            default    -> value;
        };
    }

    // ── Feature flags ─────────────────────────────────────────────────────────

    /** Whether fake players should appear as tab-list entries. */
    public boolean isFakePlayerTabEnabled() {
        return cfg.getBoolean("fakeplayer.show-in-tab", true);
    }

    /** Whether the InvSee GUI allows item edits. */
    public boolean isInvSeeEditable() {
        return cfg.getBoolean("invsee.editable", true);
    }

    /** Whether the EnderChest GUI allows item edits. */
    public boolean isEcSeeEditable() {
        return cfg.getBoolean("ecsee.editable", true);
    }
}
