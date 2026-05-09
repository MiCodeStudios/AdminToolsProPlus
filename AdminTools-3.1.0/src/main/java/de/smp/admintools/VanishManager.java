package de.smp.admintools;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishManager {

    private final JavaPlugin plugin;
    private final Set<UUID> vanishedPlayers = new HashSet<>();

    public VanishManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void vanish(Player player) {
        vanishedPlayers.add(player.getUniqueId());
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.hasPermission("admintools.vanish")) {
                other.hidePlayer(plugin, player);
            }
        }
        player.sendMessage("§aVanish §2ON §a– You are now invisible.");
    }

    public void unvanish(Player player) {
        vanishedPlayers.remove(player.getUniqueId());
        for (Player other : Bukkit.getOnlinePlayers()) {
            other.showPlayer(plugin, player);
        }
        player.sendMessage("§cVanish §4OFF §c– You are now visible.");
    }

    public boolean isVanished(Player player) { return vanishedPlayers.contains(player.getUniqueId()); }
    public boolean isVanished(UUID uuid)     { return vanishedPlayers.contains(uuid); }
    public Set<UUID> getVanishedPlayers()    { return vanishedPlayers; }

    public void applyVanishToNewPlayer(Player newPlayer) {
        if (newPlayer.hasPermission("admintools.vanish")) return;
        for (UUID uuid : vanishedPlayers) {
            Player vanished = Bukkit.getPlayer(uuid);
            if (vanished != null && vanished.isOnline()) {
                newPlayer.hidePlayer(plugin, vanished);
            }
        }
    }
}
