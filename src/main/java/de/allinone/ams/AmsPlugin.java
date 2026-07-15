package de.allinone.ams;

import de.allinone.ams.data.AmsManager;
import de.allinone.ams.gui.AmsGuiListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class AmsPlugin extends JavaPlugin {

    private static AmsPlugin instance;

    private Economy economy;
    private AmsManager amsManager;

    @Override
    public void onEnable() {
        instance = this;

        if (!setupEconomy()) {
            getLogger().severe("Vault/EssentialsX wurde nicht gefunden! Plugin wird deaktiviert.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();

        this.amsManager = new AmsManager(this);
        amsManager.loadAll();

        getServer().getPluginManager().registerEvents(new AmsGuiListener(this, amsManager), this);
        getCommand("ams").setExecutor(new AmsCommand(this, amsManager));

        // Jede Sekunde alle AMS-Konten pruefen und Geld generieren
        Bukkit.getScheduler().runTaskTimer(this, () -> amsManager.tickAll(), 20L, 20L);

        // Autosave alle 5 Minuten
        Bukkit.getScheduler().runTaskTimer(this, amsManager::saveAll, 6000L, 6000L);

        getLogger().info("AmsPlugin wurde aktiviert!");
    }

    @Override
    public void onDisable() {
        if (amsManager != null) {
            amsManager.saveAll();
        }
        getLogger().info("AmsPlugin wurde deaktiviert!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
        if (provider == null) {
            return false;
        }
        this.economy = provider.getProvider();
        return true;
    }

    public Economy getEconomy() {
        return economy;
    }

    public AmsManager getAmsManager() {
        return amsManager;
    }

    public static AmsPlugin getInstance() {
        return instance;
    }
}
