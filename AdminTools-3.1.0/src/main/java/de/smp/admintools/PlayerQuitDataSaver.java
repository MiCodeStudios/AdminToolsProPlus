package de.smp.admintools;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Saves both the inventory and enderchest of every player to disk when they log out,
 * so /invsee and /ecsee can access and edit their data while they are offline.
 */
public class PlayerQuitDataSaver implements Listener {

    private final OfflineInventoryUtil invUtil;
    private final OfflineEnderChestUtil ecUtil;

    public PlayerQuitDataSaver(OfflineInventoryUtil invUtil, OfflineEnderChestUtil ecUtil) {
        this.invUtil = invUtil;
        this.ecUtil  = ecUtil;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        invUtil.save(event.getPlayer());
        ecUtil.save(event.getPlayer().getUniqueId(), event.getPlayer().getEnderChest());
    }
}
