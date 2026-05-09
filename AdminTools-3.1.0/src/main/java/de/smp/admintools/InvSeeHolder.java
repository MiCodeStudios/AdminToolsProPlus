package de.smp.admintools;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

/**
 * Holder for the InvSee GUI.
 * Works for both online and offline targets — identified by UUID + name.
 */
public class InvSeeHolder implements InventoryHolder {

    private final UUID   targetUUID;
    private final String targetName;
    /** True if the player was online when the GUI was opened. */
    private final boolean wasOnline;

    public InvSeeHolder(UUID targetUUID, String targetName, boolean wasOnline) {
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        this.wasOnline  = wasOnline;
    }

    public UUID   getTargetUUID() { return targetUUID; }
    public String getTargetName() { return targetName; }
    public boolean wasOnline()    { return wasOnline;  }

    /** Returns the live Player if they are still online, otherwise null. */
    public Player getLiveTarget() { return Bukkit.getPlayer(targetUUID); }

    @Override
    public Inventory getInventory() { return null; }
}
