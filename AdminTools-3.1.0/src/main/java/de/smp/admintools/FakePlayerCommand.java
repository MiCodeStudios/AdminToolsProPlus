package de.smp.admintools;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Collection;

public class FakePlayerCommand implements CommandExecutor {

    private final FakePlayerManager manager;
    private final PluginConfig cfg;

    public FakePlayerCommand(FakePlayerManager manager, PluginConfig cfg) {
        this.manager = manager;
        this.cfg     = cfg;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!cfg.hasAccess(sender, "fakeplayer")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }
        if (args.length == 0) { sendUsage(sender); return true; }

        switch (args[0].toLowerCase()) {
            case "join" -> {
                if (args.length < 2) { sendUsage(sender); return true; }
                if (manager.fakeJoin(args[1]))
                    sender.sendMessage("§aFake player §e" + args[1] + " §ahas joined.");
                else
                    sender.sendMessage("§cA fake player named §e" + args[1] + " §cis already active.");
            }
            case "leave" -> {
                if (args.length < 2) { sendUsage(sender); return true; }
                if (manager.fakeLeave(args[1]))
                    sender.sendMessage("§aFake player §e" + args[1] + " §ahas left.");
                else
                    sender.sendMessage("§cNo active fake player named §e" + args[1] + "§c.");
            }
            case "list" -> {
                Collection<String> active = manager.getActiveDisplayNames();
                sender.sendMessage(active.isEmpty()
                        ? "§eNo fake players are currently active."
                        : "§eActive fake players: §f" + String.join("§e, §f", active));
            }
            default -> sendUsage(sender);
        }
        return true;
    }

    private void sendUsage(CommandSender s) {
        s.sendMessage("§cUsage: /fakeplayer <join|leave|list> [name]");
    }
}
