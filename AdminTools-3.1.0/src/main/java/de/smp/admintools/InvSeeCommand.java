package de.smp.admintools;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class InvSeeCommand implements CommandExecutor {

    /*
     * GUI layout (54 slots / 6 rows):
     *  Row 1 │ [Helmet][Chest][Legs][Boots][Offhand][X][X][X][X]
     *  Row 2 │ [Inv 9 ][10][11][12][13][14][15][16][17]
     *  Row 3 │ [Inv 18][19][20][21][22][23][24][25][26]
     *  Row 4 │ [Inv 27][28][29][30][31][32][33][34][35]
     *  Row 5 │ [Hot  0][ 1][ 2][ 3][ 4][ 5][ 6][ 7][ 8]
     *  Row 6 │ [X][X][X][X][X][X][X][X][X]  ← all barriers
     */
    public static final int[] BLOCKED_SLOTS = {5, 6, 7, 8, 45, 46, 47, 48, 49, 50, 51, 52, 53};

    private final JavaPlugin plugin;
    private final OfflineInventoryUtil invUtil;
    private final PluginConfig cfg;

    public InvSeeCommand(JavaPlugin plugin, OfflineInventoryUtil invUtil, PluginConfig cfg) {
        this.plugin  = plugin;
        this.invUtil = invUtil;
        this.cfg     = cfg;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player admin)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }
        if (!cfg.hasAccess(admin, "invsee")) {
            admin.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }
        if (args.length != 1) {
            admin.sendMessage("§cUsage: /invsee <player>");
            return true;
        }
        if (admin.getName().equalsIgnoreCase(args[0])) {
            admin.sendMessage("§cYou cannot open your own inventory with this command.");
            return true;
        }

        Player online = Bukkit.getPlayerExact(args[0]);
        if (online != null) {
            openFromLivePlayer(admin, online);
            return true;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
        UUID uuid = offlinePlayer.getUniqueId();
        String name = offlinePlayer.getName() != null ? offlinePlayer.getName() : args[0];

        if (!offlinePlayer.hasPlayedBefore() && !invUtil.hasData(uuid)) {
            admin.sendMessage("§cPlayer §e" + args[0] + " §chas never been seen on this server.");
            return true;
        }

        OfflineInventoryUtil.SavedInventory saved = invUtil.load(uuid);
        if (saved == null) {
            admin.sendMessage("§eNo inventory data found for §e" + name
                    + "§e. They must log in at least once after the plugin is installed.");
            return true;
        }

        openFromSaved(admin, uuid, name, saved);
        return true;
    }

    private void openFromLivePlayer(Player admin, Player target) {
        boolean editable = cfg.isInvSeeEditable();
        String title = (editable ? "§8Inventory of §e" : "§8Inventory of §7") + target.getName()
                + (editable ? "" : " §8(read-only)");

        Inventory gui = Bukkit.createInventory(
                new InvSeeHolder(target.getUniqueId(), target.getName(), true), 54, title);

        ItemStack[] armor = target.getInventory().getArmorContents();
        gui.setItem(0, armor[3]);
        gui.setItem(1, armor[2]);
        gui.setItem(2, armor[1]);
        gui.setItem(3, armor[0]);
        gui.setItem(4, target.getInventory().getItemInOffHand());

        for (int i = 9; i <= 35; i++) gui.setItem(i, target.getInventory().getItem(i));
        for (int i = 0; i < 9; i++)   gui.setItem(36 + i, target.getInventory().getItem(i));

        fillBlocked(gui);
        admin.openInventory(gui);
        admin.sendMessage("§aShowing inventory of §e" + target.getName()
                + (editable ? "§a." : " §7(read-only)§a."));
    }

    private void openFromSaved(Player admin, UUID uuid, String name,
                               OfflineInventoryUtil.SavedInventory saved) {
        boolean editable = cfg.isInvSeeEditable();
        String title = "§8Inventory of §7" + name + " §8(offline"
                + (editable ? "" : ", read-only") + ")";

        Inventory gui = Bukkit.createInventory(
                new InvSeeHolder(uuid, name, false), 54, title);

        if (saved.armor() != null) {
            gui.setItem(0, saved.armor()[3]);
            gui.setItem(1, saved.armor()[2]);
            gui.setItem(2, saved.armor()[1]);
            gui.setItem(3, saved.armor()[0]);
        }
        gui.setItem(4, saved.offhand());

        if (saved.main() != null) {
            for (int i = 9; i <= 35; i++) gui.setItem(i, saved.main()[i]);
            for (int i = 0; i < 9; i++)   gui.setItem(36 + i, saved.main()[i]);
        }

        fillBlocked(gui);
        admin.openInventory(gui);
        admin.sendMessage("§aShowing §7offline §ainventory of §e" + name
                + (editable ? "§a." : " §7(read-only)§a."));
    }

    public static void fillBlocked(Inventory gui) {
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta meta = barrier.getItemMeta();
        if (meta != null) { meta.setDisplayName("§c"); barrier.setItemMeta(meta); }
        for (int slot : BLOCKED_SLOTS) {
            if (gui.getItem(slot) == null) gui.setItem(slot, barrier);
        }
    }
}
