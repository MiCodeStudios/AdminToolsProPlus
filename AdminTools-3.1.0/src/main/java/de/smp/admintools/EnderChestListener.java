package de.smp.admintools;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class EnderChestListener implements Listener {

    private final OfflineEnderChestUtil ecUtil;
    private final PluginConfig cfg;

    public EnderChestListener(OfflineEnderChestUtil ecUtil, PluginConfig cfg) {
        this.ecUtil = ecUtil;
        this.cfg    = cfg;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof EnderChestHolder holder)) return;

        if (holder.wasOnline() && Bukkit.getPlayer(holder.getTargetUUID()) == null) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage("§cThat player is no longer online.");
            event.getWhoClicked().closeInventory();
            return;
        }
        if (!cfg.isEcSeeEditable()) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof EnderChestHolder holder)) return;

        if (holder.wasOnline() && Bukkit.getPlayer(holder.getTargetUUID()) == null) {
            event.setCancelled(true);
            return;
        }
        if (!cfg.isEcSeeEditable()) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof EnderChestHolder holder)) return;
        if (!cfg.isEcSeeEditable()) return;

        UUID targetUUID = holder.getTargetUUID();
        Inventory gui   = event.getInventory();
        ItemStack[] items = new ItemStack[27];
        for (int i = 0; i < 27; i++) items[i] = gui.getItem(i);

        ecUtil.saveItems(targetUUID, items);

        Player live = Bukkit.getPlayer(targetUUID);
        if (live != null) {
            for (int i = 0; i < 27; i++) live.getEnderChest().setItem(i, items[i]);
            live.updateInventory();
        }
        event.getPlayer().sendMessage("§aEnderchest of §e" + holder.getTargetName() + " §asaved.");
    }
}
