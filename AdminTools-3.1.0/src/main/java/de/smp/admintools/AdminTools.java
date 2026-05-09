package de.smp.admintools;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class AdminTools extends JavaPlugin {

    private PluginConfig pluginConfig;
    private VanishManager vanishManager;
    private FakePlayerManager fakePlayerManager;
    private OfflineInventoryUtil invUtil;
    private OfflineEnderChestUtil ecUtil;

    @Override
    public void onEnable() {
        pluginConfig      = new PluginConfig(this);
        vanishManager     = new VanishManager(this);
        fakePlayerManager = new FakePlayerManager(this, pluginConfig);
        invUtil           = new OfflineInventoryUtil(this);
        ecUtil            = new OfflineEnderChestUtil(this);

        Objects.requireNonNull(getCommand("admintools")) .setExecutor(new HelpCommand(pluginConfig));
        Objects.requireNonNull(getCommand("invsee"))     .setExecutor(new InvSeeCommand(this, invUtil, pluginConfig));
        Objects.requireNonNull(getCommand("ecsee"))      .setExecutor(new EnderChestCommand(this, ecUtil, pluginConfig));
        Objects.requireNonNull(getCommand("vanish"))     .setExecutor(new VanishCommand(vanishManager, pluginConfig));
        Objects.requireNonNull(getCommand("fakeplayer")) .setExecutor(new FakePlayerCommand(fakePlayerManager, pluginConfig));

        getServer().getPluginManager().registerEvents(new InvSeeListener(invUtil, pluginConfig),               this);
        getServer().getPluginManager().registerEvents(new EnderChestListener(ecUtil, pluginConfig),            this);
        getServer().getPluginManager().registerEvents(new PlayerQuitDataSaver(invUtil, ecUtil),                 this);
        // Applies admin-edited data to the player on their next login (1-tick delay)
        getServer().getPluginManager().registerEvents(new PlayerJoinDataApplier(this, invUtil, ecUtil),        this);
        getServer().getPluginManager().registerEvents(new VanishListener(vanishManager, pluginConfig),         this);
        getServer().getPluginManager().registerEvents(new FakePlayerListener(fakePlayerManager, pluginConfig), this);

        getLogger().info("AdminTools enabled.");
    }

    @Override
    public void onDisable() {
        fakePlayerManager.cleanup();
        getLogger().info("AdminTools disabled.");
    }

    public PluginConfig getPluginConfig2()          { return pluginConfig;      }
    public VanishManager getVanishManager()         { return vanishManager;     }
    public FakePlayerManager getFakePlayerManager() { return fakePlayerManager; }
    public OfflineInventoryUtil getInvUtil()        { return invUtil;           }
    public OfflineEnderChestUtil getEcUtil()        { return ecUtil;            }
}