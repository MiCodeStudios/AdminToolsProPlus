package de.smp.admintools;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class HelpCommand implements CommandExecutor {

    private static final String VERSION = "1.1.0";
    private final PluginConfig cfg;

    public HelpCommand(PluginConfig cfg) {
        this.cfg = cfg;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // /admintools reload
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!cfg.hasAccess(sender, "admintools")) {
                sender.sendMessage("§cYou don't have permission to do that.");
                return true;
            }
            cfg.reload();
            sender.sendMessage("§aAdminTools config reloaded.");
            return true;
        }

        // ── Header ────────────────────────────────────────────────────────────
        sender.sendMessage(bar());
        sender.sendMessage(center("§6§lAdminTools §r§7v" + VERSION));
        sender.sendMessage(center("§8Admin utilities for Paper 1.21.1"));
        sender.sendMessage(bar());
        sender.sendMessage(Component.empty());

        // ── Inventory ─────────────────────────────────────────────────────────
        sender.sendMessage(section("📦 Inventory"));
        sender.sendMessage(entry(
                "/invsee <player>",
                "Open a player's full inventory — armor, offhand, hotbar included.\n"
              + "      §7Works for §boffline §7players too. "
              + (cfg.isInvSeeEditable() ? "§aEditing enabled." : "§cRead-only (config)."),
                "/invsee "));
        sender.sendMessage(Component.empty());

        // ── EnderChest ────────────────────────────────────────────────────────
        sender.sendMessage(section("🟣 EnderChest"));
        sender.sendMessage(entry(
                "/ecsee <player>",
                "Open a player's enderchest.\n"
              + "      §7Works for §boffline §7players too. "
              + (cfg.isEcSeeEditable() ? "§aEditing enabled." : "§cRead-only (config)."),
                "/ecsee "));
        sender.sendMessage(Component.empty());

        // ── Vanish ────────────────────────────────────────────────────────────
        sender.sendMessage(section("👻 Vanish"));
        sender.sendMessage(entry(
                "/vanish on",
                "Become invisible to regular players.\n"
              + "      §7Ops and admins with the vanish permission still see you.\n"
              + "      §7/msg to you returns 'not online' for non-admins.",
                "/vanish on"));
        sender.sendMessage(entry(
                "/vanish off",
                "Reappear for all players.",
                "/vanish off"));
        sender.sendMessage(Component.empty());

        // ── FakePlayer ────────────────────────────────────────────────────────
        sender.sendMessage(section("🎭 FakePlayer"));
        sender.sendMessage(entry(
                "/fakeplayer join <name>",
                "Simulate a player joining the server.\n"
              + "      §7Broadcasts the vanilla join message in the client's language.\n"
              + "      §7" + (cfg.isFakePlayerTabEnabled()
                        ? "§aAppears in the tab list like a real player."
                        : "§cTab list disabled (config) — chat message only."),
                "/fakeplayer join "));
        sender.sendMessage(entry(
                "/fakeplayer leave <name>",
                "Simulate a player leaving the server.",
                "/fakeplayer leave "));
        sender.sendMessage(entry(
                "/fakeplayer list",
                "Show all currently active fake players.",
                "/fakeplayer list"));
        sender.sendMessage(Component.empty());

        // ── AdminTools ────────────────────────────────────────────────────────
        sender.sendMessage(section("⚙️  AdminTools"));
        sender.sendMessage(entry(
                "/admintools help",
                "Show this help page.",
                "/admintools help"));
        sender.sendMessage(entry(
                "/admintools reload",
                "Reload config.yml without restarting the server.\n"
              + "      §7Permissions and feature flags update instantly.",
                "/admintools reload"));
        sender.sendMessage(Component.empty());

        // ── Footer ────────────────────────────────────────────────────────────
        sender.sendMessage(bar());
        sender.sendMessage(Component.text("  §8Data: §7plugins/AdminTools/   §8│   §7Alias: §8/at help"));
        sender.sendMessage(bar());

        return true;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Component bar() {
        return Component.text("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private Component center(String text) {
        return Component.text("  " + text);
    }

    private Component section(String label) {
        return Component.text("  §e" + label);
    }

    /**
     * One command entry: clickable usage line + description below it.
     * Clicking the usage line suggests the command in the chat bar.
     */
    private Component entry(String usage, String description, String suggest) {
        return Component.text()
                .append(
                    Component.text("    §a/" + usage)
                        .decorate(TextDecoration.BOLD)
                        .hoverEvent(HoverEvent.showText(
                                Component.text("§7Click to suggest this command")))
                        .clickEvent(ClickEvent.suggestCommand("/" + suggest))
                )
                .append(Component.text("\n      §7" + description))
                .build();
    }
}
