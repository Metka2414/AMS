package de.allinone.ams.data;

import de.allinone.ams.AmsPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AmsManager {

    private final AmsPlugin plugin;
    private final Map<UUID, AmsAccount> accounts = new HashMap<>();
    private final Map<UUID, AmsAccount> openGuiSessions = new HashMap<>();
    private final File file;

    // Feste Preise pro AMS-Paket - bleiben immer gleich, unabhaengig vom Bestand
    private static final double PRICE_PER_AMS_SINGLE = 8000;
    private static final double PRICE_25 = 180000;
    private static final double PRICE_50 = 340000;
    private static final double PRICE_100 = 700000;
    private static final double PRICE_1000000 = 7000000000.0;

    public AmsManager(AmsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "ams.yml");
    }

    public AmsAccount get(UUID uuid) {
        return accounts.computeIfAbsent(uuid, AmsAccount::new);
    }

    public void setOpenSpawner(UUID playerId, AmsAccount account) {
        openGuiSessions.put(playerId, account);
    }

    public AmsAccount getOpenAccount(UUID playerId) {
        return openGuiSessions.get(playerId);
    }

    public void clearOpenAccount(UUID playerId) {
        openGuiSessions.remove(playerId);
    }

    /**
     * Fester Preis fuer das jeweilige Kauf-Paket. Bleibt immer gleich, egal wie viele AMS
     * der Spieler bereits besitzt - keine Preissteigerung beim Kauf.
     */
    public double getPurchasePrice(int currentAmsCount, int amount) {
        return switch (amount) {
            case 1 -> PRICE_PER_AMS_SINGLE;
            case 25 -> PRICE_25;
            case 50 -> PRICE_50;
            case 100 -> PRICE_100;
            case 1000000 -> PRICE_1000000;
            default -> PRICE_PER_AMS_SINGLE * amount;
        };
    }

    public void tickAll() {
        long now = System.currentTimeMillis();
        for (AmsAccount account : accounts.values()) {
            if (account.getAmsCount() <= 0) continue;
            long elapsedSeconds = (now - account.getLastTick()) / 1000L;
            if (elapsedSeconds >= account.getIntervalSeconds()) {
                account.addStoredMoney(account.getMoneyPerCycle());
                account.setLastTick(now);
            }
        }
    }

    public void saveAll() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, AmsAccount> entry : accounts.entrySet()) {
            String path = "accounts." + entry.getKey() + ".";
            AmsAccount acc = entry.getValue();
            config.set(path + "ams-count", acc.getAmsCount());
            config.set(path + "money-level", acc.getMoneyLevel());
            config.set(path + "time-level", acc.getTimeLevel());
            config.set(path + "stored-money", acc.getStoredMoney());
            config.set(path + "last-tick", acc.getLastTick());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Konnte ams.yml nicht speichern: " + e.getMessage());
        }
    }

    public void loadAll() {
        if (!file.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.isConfigurationSection("accounts")) return;

        for (String key : config.getConfigurationSection("accounts").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                AmsAccount acc = new AmsAccount(uuid);
                String path = "accounts." + key + ".";

                acc.setAmsCount(config.getInt(path + "ams-count", 0));
                acc.setMoneyLevel(config.getInt(path + "money-level", 0));
                acc.setTimeLevel(config.getInt(path + "time-level", 0));
                acc.addStoredMoney(config.getDouble(path + "stored-money", 0));
                acc.setLastTick(config.getLong(path + "last-tick", System.currentTimeMillis()));

                accounts.put(uuid, acc);
            } catch (Exception e) {
                plugin.getLogger().warning("Konnte AMS-Konto '" + key + "' nicht laden: " + e.getMessage());
            }
        }
        plugin.getLogger().info(accounts.size() + " AMS-Konten geladen.");
    }
}
