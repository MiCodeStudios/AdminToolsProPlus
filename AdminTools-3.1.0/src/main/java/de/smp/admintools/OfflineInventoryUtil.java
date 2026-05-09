package de.smp.admintools;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Persists full player inventories (36 main slots + 4 armor + offhand)
 * to YAML so they can be inspected and edited while the player is offline.
 *
 * File location: plugins/AdminTools/inventories/<UUID>.yml
 *
 * Dirty-flag mechanism:
 *   When an admin edits an offline inventory, a marker file
 *   plugins/AdminTools/inventories/<UUID>.dirty is created.
 *   On the next login, PlayerJoinDataApplier reads this flag, applies the
 *   YAML to the live player, and deletes the marker so normal saves take
 *   over again.
 */
public class OfflineInventoryUtil {

    private final JavaPlugin plugin;
    private final File baseDir;

    public OfflineInventoryUtil(JavaPlugin plugin) {
        this.plugin  = plugin;
        this.baseDir = new File(plugin.getDataFolder(), "inventories");
        if (!baseDir.exists()) baseDir.mkdirs();
    }

    // ── Save on quit (normal flow) ────────────────────────────────────────────

    /** Called on player quit — snapshot the whole inventory. */
    public void save(Player player) {
        YamlConfiguration cfg = new YamlConfiguration();

        for (int i = 0; i < 36; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null) cfg.set("main." + i, item);
        }

        ItemStack[] armor = player.getInventory().getArmorContents();
        for (int i = 0; i < 4; i++) {
            if (armor[i] != null) cfg.set("armor." + i, armor[i]);
        }

        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand != null && offhand.getType() != Material.AIR)
            cfg.set("offhand", offhand);

        try {
            cfg.save(file(player.getUniqueId()));
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING,
                    "Could not save inventory for " + player.getName(), e);
        }
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    /**
     * Loads the last-saved inventory snapshot.
     * @return a {@link SavedInventory} or {@code null} if no data exists.
     */
    public SavedInventory load(UUID uuid) {
        File f = file(uuid);
        if (!f.exists()) return null;

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);

        ItemStack[] main  = new ItemStack[36];
        ItemStack[] armor = new ItemStack[4];

        for (int i = 0; i < 36; i++) main[i]  = cfg.getItemStack("main."  + i);
        for (int i = 0; i < 4;  i++) armor[i] = cfg.getItemStack("armor." + i);
        ItemStack offhand = cfg.getItemStack("offhand");

        return new SavedInventory(main, armor, offhand);
    }

    // ── Admin edit (sets dirty flag) ──────────────────────────────────────────

    /**
     * Writes a modified snapshot to disk and marks it dirty so it is applied
     * to the player on their next login.
     */
    public void saveDirect(UUID uuid, ItemStack[] main, ItemStack[] armor, ItemStack offhand) {
        YamlConfiguration cfg = new YamlConfiguration();

        for (int i = 0; i < 36; i++) if (main[i]  != null) cfg.set("main."  + i, main[i]);
        for (int i = 0; i < 4;  i++) if (armor[i] != null) cfg.set("armor." + i, armor[i]);
        if (offhand != null && offhand.getType() != Material.AIR)
            cfg.set("offhand", offhand);

        try {
            cfg.save(file(uuid));
            markDirty(uuid);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING,
                    "Could not write inventory for " + uuid, e);
        }
    }

    // ── Dirty flag ────────────────────────────────────────────────────────────

    /** Returns true if an admin has edited this player's inventory since their last login. */
    public boolean isDirty(UUID uuid) {
        return dirtyFile(uuid).exists();
    }

    /** Removes the dirty marker (called after applying on join). */
    public void clearDirty(UUID uuid) {
        dirtyFile(uuid).delete();
    }

    private void markDirty(UUID uuid) throws IOException {
        File f = dirtyFile(uuid);
        if (!f.exists()) f.createNewFile();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public boolean hasData(UUID uuid) { return file(uuid).exists(); }

    private File file(UUID uuid)      { return new File(baseDir, uuid + ".yml"); }
    private File dirtyFile(UUID uuid) { return new File(baseDir, uuid + ".dirty"); }

    // ── Inner record ──────────────────────────────────────────────────────────

    public record SavedInventory(ItemStack[] main, ItemStack[] armor, ItemStack offhand) {}
}