package de.smp.admintools;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class FakePlayerListener implements Listener {

    private final FakePlayerManager fakePlayerManager;
    private final PluginConfig cfg;

    public FakePlayerListener(FakePlayerManager fakePlayerManager, PluginConfig cfg) {
        this.fakePlayerManager = fakePlayerManager;
        this.cfg               = cfg;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        fakePlayerManager.applyToNewPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String raw   = event.getMessage();
        String lower = raw.toLowerCase();
        if (!lower.startsWith("/msg ")     && !lower.startsWith("/tell ")
         && !lower.startsWith("/w ")       && !lower.startsWith("/whisper ")) return;

        String[] parts = raw.split(" ", 3);
        if (parts.length < 3) return;

        if (fakePlayerManager.isActive(parts[1])) {
            event.setCancelled(true);
            fakePlayerManager.routeMessage(event.getPlayer(), parts[1], parts[2]);
        }
    }
}
