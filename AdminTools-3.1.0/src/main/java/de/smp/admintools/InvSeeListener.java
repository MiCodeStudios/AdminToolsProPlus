package de.smp.admintools;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class InvSeeListener implements Listener {

    private static final Set<Integer> BLOCKED = Arrays.stream(InvSeeCommand.BLOCKED_SLOTS)
            .boxed().collect(Collectors.toSet());

    private final OfflineInventoryUtil invUtil;
    private final PluginConfig cfg;

    public InvSeeListener(OfflineInventoryUtil invUtil, PluginConfig cfg) {
        this.invUtil = invUtil;
        this.cfg     = cfg;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof InvSeeHolder holder)) return;

        if (holder.wasOnline() && holder.getLiveTarget() == null) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage("§cThat player is no longer online.");
            event.getWhoClicked().closeInventory();
            return;
        }

        // Read-only mode from config
        if (!cfg.isInvSeeEditable()) {
            event.setCancelled(true);
            return;
        }

        if (BLOCKED.contains(event.getRawSlot())) {
            event.setCancelled(true);
            return;
        }
        if (event.isShiftClick() && event.getRawSlot() >= 54) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof InvSeeHolder holder)) return;

        if (holder.wasOnline() && holder.getLiveTarget() == null) { event.setCancelled(true); return; }
        if (!cfg.isInvSeeEditable()) { event.setCancelled(true); return; }

        for (int slot : event.getRawSlots()) {
            if (BLOCKED.contains(slot)) { event.setCancelled(true); return; }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof InvSeeHolder holder)) return;
        if (!cfg.isInvSeeEditable()) return; // read-only: nothing to save

        Inventory gui = event.getInventory();

        ItemStack[] armor = new ItemStack[4];
        armor[3] = clean(gui.getItem(0));
        armor[2] = clean(gui.getItem(1));
        armor[1] = clean(gui.getItem(2));
        armor[0] = clean(gui.getItem(3));
        ItemStack offhand = clean(gui.getItem(4));

        ItemStack[] main = new ItemStack[36];
        for (int i = 9; i <= 35; i++) main[i] = clean(gui.getItem(i));
        for (int i = 0; i < 9; i++)   main[i] = clean(gui.getItem(36 + i));

        UUID targetUUID = holder.getTargetUUID();
        invUtil.saveDirect(targetUUID, main, armor, offhand);

        Player live = Bukkit.getPlayer(targetUUID);
        if (live != null) {
            for (int i = 0; i < 36; i++) live.getInventory().setItem(i, main[i]);
            live.getInventory().setArmorContents(armor);
            live.getInventory().setItemInOffHand(offhand != null ? offhand : new ItemStack(Material.AIR));
            live.updateInventory();
        }

        event.getPlayer().sendMessage("§aInventory of §e" + holder.getTargetName() + " §asaved.");
    }

    private ItemStack clean(ItemStack item) {
        return (item == null || item.getType() == Material.BARRIER) ? null : item;
    }
}
