package de.smp.admintools;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Applies admin-edited inventory / enderchest data to a player on login.
 *
 * Why the 1-tick delay?
 *   Paper loads playerdata (world/playerdata/<uuid>.dat) during the join
 *   sequence, which happens AFTER PlayerJoinEvent fires on MONITOR priority.
 *   If we set the inventory in the same tick, Minecraft overwrites it with
 *   the .dat contents immediately after.  Scheduling one tick later lets the
 *   server finish its own loading before we apply our changes.
 */
public class PlayerJoinDataApplier implements Listener {

    private final JavaPlugin plugin;
    private final OfflineInventoryUtil invUtil;
    private final OfflineEnderChestUtil ecUtil;

    public PlayerJoinDataApplier(JavaPlugin plugin,
                                 OfflineInventoryUtil invUtil,
                                 OfflineEnderChestUtil ecUtil) {
        this.plugin  = plugin;
        this.invUtil = invUtil;
        this.ecUtil  = ecUtil;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID   uuid   = player.getUniqueId();

        boolean invDirty = invUtil.isDirty(uuid);
        boolean ecDirty  = ecUtil.isDirty(uuid);

        if (!invDirty && !ecDirty) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                if (invDirty) {
                    applyInventory(player, uuid);
                }
                if (ecDirty) {
                    applyEnderChest(player, uuid);
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    // ── Apply helpers ─────────────────────────────────────────────────────────

    private void applyInventory(Player player, UUID uuid) {
        OfflineInventoryUtil.SavedInventory saved = invUtil.load(uuid);
        if (saved == null) {
            invUtil.clearDirty(uuid);
            return;
        }

        // Main inventory (slots 0–35)
        if (saved.main() != null) {
            for (int i = 0; i < 36; i++) {
                player.getInventory().setItem(i,
                        saved.main()[i] != null ? saved.main()[i] : new ItemStack(Material.AIR));
            }
        }

        // Armor (index 0 = boots … 3 = helmet)
        if (saved.armor() != null) {
            player.getInventory().setArmorContents(saved.armor());
        }

        // Offhand
        player.getInventory().setItemInOffHand(
                saved.offhand() != null ? saved.offhand() : new ItemStack(Material.AIR));

        player.updateInventory();
        invUtil.clearDirty(uuid);

        plugin.getLogger().info("[AdminTools] Applied edited inventory to " + player.getName() + " on join.");
    }

    private void applyEnderChest(Player player, UUID uuid) {
        ItemStack[] items = ecUtil.load(uuid);
        if (items == null) {
            ecUtil.clearDirty(uuid);
            return;
        }

        for (int i = 0; i < 27; i++) {
            player.getEnderChest().setItem(i,
                    items[i] != null ? items[i] : new ItemStack(Material.AIR));
        }

        ecUtil.clearDirty(uuid);

        plugin.getLogger().info("[AdminTools] Applied edited enderchest to " + player.getName() + " on join.");
    }
}