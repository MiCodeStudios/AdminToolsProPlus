package de.smp.admintools;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class EnderChestCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final OfflineEnderChestUtil ecUtil;
    private final PluginConfig cfg;

    public EnderChestCommand(JavaPlugin plugin, OfflineEnderChestUtil ecUtil, PluginConfig cfg) {
        this.plugin  = plugin;
        this.ecUtil  = ecUtil;
        this.cfg     = cfg;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player admin)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }
        if (!cfg.hasAccess(admin, "ecsee")) {
            admin.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }
        if (args.length != 1) {
            admin.sendMessage("§cUsage: /ecsee <player>");
            return true;
        }

        Player online = Bukkit.getPlayerExact(args[0]);
        if (online != null) {
            openGui(admin, online.getUniqueId(), online.getName(), true, toArray(online.getEnderChest()));
            return true;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
        UUID uuid = offlinePlayer.getUniqueId();
        String name = offlinePlayer.getName() != null ? offlinePlayer.getName() : args[0];

        if (!offlinePlayer.hasPlayedBefore() && !ecUtil.hasData(uuid)) {
            admin.sendMessage("§cPlayer §e" + args[0] + " §chas never been seen on this server.");
            return true;
        }
        ItemStack[] items = ecUtil.load(uuid);
        if (items == null) {
            admin.sendMessage("§eNo enderchest data found for §e" + name
                    + "§e. They must log in at least once after the plugin is installed.");
            return true;
        }
        openGui(admin, uuid, name, false, items);
        return true;
    }

    private void openGui(Player admin, UUID targetUUID, String targetName,
                         boolean isOnline, ItemStack[] items) {
        boolean editable = cfg.isEcSeeEditable();
        String title = (isOnline
                ? "§8EC of §e" + targetName
                : "§8EC of §7" + targetName + " §8(offline)")
                + (editable ? "" : "§8 (read-only)");

        Inventory gui = Bukkit.createInventory(
                new EnderChestHolder(targetUUID, targetName, isOnline), 27, title);
        for (int i = 0; i < Math.min(items.length, 27); i++) gui.setItem(i, items[i]);

        admin.openInventory(gui);
        admin.sendMessage("§aShowing enderchest of §e" + targetName
                + (editable ? "§a." : " §7(read-only)§a."));
    }

    private ItemStack[] toArray(org.bukkit.inventory.Inventory inv) {
        ItemStack[] arr = new ItemStack[27];
        for (int i = 0; i < 27; i++) arr[i] = inv.getItem(i);
        return arr;
    }
}
