package de.smp.admintools;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Speichert die Enderchests der Spieler in YAML-Dateien,
 * damit sie auch im Offline-Zustand gelesen/bearbeitet werden können.
 *
 * Datei: plugins/AdminTools/enderchests/<UUID>.yml
 *
 * Dirty-flag mechanism:
 *   When an admin edits an offline enderchest, a marker file
 *   plugins/AdminTools/enderchests/<UUID>.dirty is created.
 *   On the next login, PlayerJoinDataApplier reads this flag, applies the
 *   YAML to the live player, and deletes the marker.
 */
public class OfflineEnderChestUtil {

    private final JavaPlugin plugin;
    private final File baseDir;

    public OfflineEnderChestUtil(JavaPlugin plugin) {
        this.plugin  = plugin;
        this.baseDir = new File(plugin.getDataFolder(), "enderchests");
        if (!baseDir.exists()) baseDir.mkdirs();
    }

    // ── Save on quit (normal flow) ────────────────────────────────────────────

    /** Speichert einen Enderchest-Inhalt (27 Slots) für die gegebene UUID. */
    public void save(UUID uuid, Inventory enderChest) {
        YamlConfiguration config = new YamlConfiguration();
        for (int i = 0; i < 27; i++) {
            ItemStack item = enderChest.getItem(i);
            if (item != null) config.set("slot." + i, item);
        }
        try {
            config.save(getFile(uuid));
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING,
                    "Fehler beim Speichern des Enderchests für " + uuid, e);
        }
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    /**
     * Lädt den gespeicherten Enderchest-Inhalt (27 Slots).
     * @return Array mit 27 Einträgen oder {@code null} wenn keine Datei vorhanden.
     */
    public ItemStack[] load(UUID uuid) {
        File file = getFile(uuid);
        if (!file.exists()) return null;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ItemStack[] items = new ItemStack[27];
        for (int i = 0; i < 27; i++) {
            items[i] = config.getItemStack("slot." + i);
        }
        return items;
    }

    // ── Admin edit (sets dirty flag) ──────────────────────────────────────────

    /**
     * Speichert geänderte Items in die YAML-Datei und setzt den Dirty-Flag,
     * damit die Daten beim nächsten Login des Spielers angewendet werden.
     */
    public void saveItems(UUID uuid, ItemStack[] items) {
        YamlConfiguration config = new YamlConfiguration();
        for (int i = 0; i < 27; i++) {
            if (items[i] != null) config.set("slot." + i, items[i]);
        }
        try {
            config.save(getFile(uuid));
            markDirty(uuid);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING,
                    "Fehler beim Speichern des Enderchests für " + uuid, e);
        }
    }

    // ── Dirty flag ────────────────────────────────────────────────────────────

    /** Returns true if an admin has edited this player's enderchest since their last login. */
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

    public boolean hasData(UUID uuid) {
        return getFile(uuid).exists();
    }

    private File getFile(UUID uuid)   { return new File(baseDir, uuid + ".yml"); }
    private File dirtyFile(UUID uuid) { return new File(baseDir, uuid + ".dirty"); }
}