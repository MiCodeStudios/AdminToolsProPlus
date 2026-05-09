package de.smp.admintools;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Level;

/**
 * Manages fake players (join/leave broadcast, optional tab-list simulation).
 *
 * NMS-free implementation — uses only the Bukkit/Adventure API.
 *
 * NOTE: True tab-list injection requires NMS packets (ClientboundPlayerInfoUpdatePacket).
 *       If you want real tab entries, add the paperweight-userdev Gradle plugin and
 *       re-enable the NMS block below.  Until then, show-in-tab is ignored with a
 *       one-time warning in the server log.
 */
public class FakePlayerManager {

    private final JavaPlugin plugin;
    private final PluginConfig cfg;

    /** lowercase name → display name */
    private final Map<String, String> activeFakePlayers = new LinkedHashMap<>();
    /** lowercase name → stable UUID */
    private final Map<String, UUID>   fakeUUIDs         = new HashMap<>();

    private boolean tabWarningShown = false;

    public FakePlayerManager(JavaPlugin plugin, PluginConfig cfg) {
        this.plugin = plugin;
        this.cfg    = cfg;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Simulates a player joining.
     * @return false if a fake player with that name is already active.
     */
    public boolean fakeJoin(String name) {
        if (activeFakePlayers.containsKey(name.toLowerCase())) return false;

        UUID uuid = UUID.nameUUIDFromBytes(("FakePlayer:" + name).getBytes());
        activeFakePlayers.put(name.toLowerCase(), name);
        fakeUUIDs.put(name.toLowerCase(), uuid);

        warnIfTabEnabled();

        Bukkit.getServer().broadcast(
                Component.translatable("multiplayer.player.joined",
                        Component.text(name)).color(NamedTextColor.YELLOW));
        return true;
    }

    /**
     * Simulates a player leaving.
     * @return false if no fake player with that name exists.
     */
    public boolean fakeLeave(String name) {
        String displayName = activeFakePlayers.remove(name.toLowerCase());
        if (displayName == null) return false;

        fakeUUIDs.remove(name.toLowerCase());

        Bukkit.getServer().broadcast(
                Component.translatable("multiplayer.player.left",
                        Component.text(displayName)).color(NamedTextColor.YELLOW));
        return true;
    }

    /**
     * Called when a real player joins — currently a no-op in the NMS-free build
     * (tab entries cannot be injected without NMS).
     */
    public void applyToNewPlayer(Player newPlayer) {
        // Tab-list injection requires NMS — see class Javadoc.
    }

    /**
     * Routes a /msg targeting a fake player to all admins and back to the sender.
     * @return false if no such fake player is active.
     */
    public boolean routeMessage(Player sender, String targetName, String message) {
        String displayName = activeFakePlayers.get(targetName.toLowerCase());
        if (displayName == null) return false;

        sender.sendMessage(Component.text()
                .append(Component.text("You whisper to " + displayName + ": ", NamedTextColor.GRAY))
                .append(Component.text(message, NamedTextColor.WHITE))
                .build());

        Component notice = Component.text()
                .append(Component.text("[FakePlayer] ", NamedTextColor.DARK_GRAY))
                .append(Component.text(sender.getName(), NamedTextColor.WHITE))
                .append(Component.text(" → " + displayName + ": ", NamedTextColor.GRAY))
                .append(Component.text(message, NamedTextColor.WHITE))
                .build();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isOp() || p.hasPermission("admintools.fakeplayer")) {
                p.sendMessage(notice);
            }
        }
        return true;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public boolean isActive(String name)              { return activeFakePlayers.containsKey(name.toLowerCase()); }
    public Collection<String> getActiveDisplayNames() { return Collections.unmodifiableCollection(activeFakePlayers.values()); }

    /** Cleanly removes all active fake players (called on plugin disable). */
    public void cleanup() {
        for (String key : new ArrayList<>(activeFakePlayers.keySet())) {
            fakeLeave(activeFakePlayers.get(key));
        }
    }

    /**
     * Logs a one-time warning if show-in-tab is enabled in the config but
     * NMS support is not compiled in.
     */
    private void warnIfTabEnabled() {
        if (cfg.isFakePlayerTabEnabled() && !tabWarningShown) {
            tabWarningShown = true;
            plugin.getLogger().log(Level.WARNING,
                    "[AdminTools] fakeplayer.show-in-tab is enabled in config.yml, " +
                            "but this build was compiled without NMS access. " +
                            "Tab-list entries will NOT be shown. " +
                            "To enable them, add the paperweight-userdev Gradle plugin and " +
                            "restore the NMS packet code in FakePlayerManager.");
        }
    }
}