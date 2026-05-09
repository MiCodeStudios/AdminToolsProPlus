package de.smp.admintools;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

/**
 * Holder für das Enderchest-GUI eines anderen Spielers.
 * Enthält UUID + Name für Online- und Offline-Spieler.
 */
public class EnderChestHolder implements InventoryHolder {

    private final UUID targetUUID;
    private final String targetName;
    /** true = Spieler war zum Zeitpunkt des Öffnens online */
    private final boolean wasOnline;

    public EnderChestHolder(UUID targetUUID, String targetName, boolean wasOnline) {
        this.targetUUID  = targetUUID;
        this.targetName  = targetName;
        this.wasOnline   = wasOnline;
    }

    public UUID   getTargetUUID()  { return targetUUID;  }
    public String getTargetName()  { return targetName;  }
    public boolean wasOnline()     { return wasOnline;   }

    @Override
    public Inventory getInventory() { return null; }
}
