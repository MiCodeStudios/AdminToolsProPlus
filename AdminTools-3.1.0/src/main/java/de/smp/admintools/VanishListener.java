package de.smp.admintools;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class VanishListener implements Listener {

    private final VanishManager vanishManager;
    private final PluginConfig cfg;

    public VanishListener(VanishManager vanishManager, PluginConfig cfg) {
        this.vanishManager = vanishManager;
        this.cfg           = cfg;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        vanishManager.applyVanishToNewPlayer(event.getPlayer());
        if (vanishManager.isVanished(event.getPlayer())) vanishManager.vanish(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (vanishManager.isVanished(event.getPlayer()))
            vanishManager.getVanishedPlayers().remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String lower = event.getMessage().toLowerCase();
        if (!lower.startsWith("/msg ") && !lower.startsWith("/tell ")
         && !lower.startsWith("/w ")   && !lower.startsWith("/whisper ")) return;

        String[] parts = event.getMessage().split(" ", 3);
        if (parts.length < 2) return;

        Player target = org.bukkit.Bukkit.getPlayerExact(parts[1]);
        if (target == null) return;

        if (vanishManager.isVanished(target) && !cfg.hasAccess(event.getPlayer(), "vanish")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cPlayer §e" + parts[1] + " §cis not online.");
        }
    }
}
