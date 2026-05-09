package de.smp.admintools;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VanishCommand implements CommandExecutor {

    private final VanishManager vanishManager;
    private final PluginConfig cfg;

    public VanishCommand(VanishManager vanishManager, PluginConfig cfg) {
        this.vanishManager = vanishManager;
        this.cfg           = cfg;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }
        if (!cfg.hasAccess(player, "vanish")) {
            player.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }
        if (args.length != 1) {
            player.sendMessage("§cUsage: /vanish <on|off>");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "on", "true" -> {
                if (vanishManager.isVanished(player)) { player.sendMessage("§eYou are already vanished."); return true; }
                vanishManager.vanish(player);
            }
            case "off", "false" -> {
                if (!vanishManager.isVanished(player)) { player.sendMessage("§eYou are already visible."); return true; }
                vanishManager.unvanish(player);
            }
            default -> player.sendMessage("§cUsage: /vanish <on|off>");
        }
        return true;
    }
}
