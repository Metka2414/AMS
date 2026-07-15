package de.allinone.ams.gui;

import de.allinone.ams.AmsPlugin;
import de.allinone.ams.data.AmsAccount;
import de.allinone.ams.data.AmsManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class AmsGuiListener implements Listener {

    private final AmsPlugin plugin;
    private final AmsManager amsManager;

    public AmsGuiListener(AmsPlugin plugin, AmsManager amsManager) {
        this.plugin = plugin;
        this.amsManager = amsManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(AmsGui.TITLE)) return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        AmsAccount account = amsManager.getOpenAccount(player.getUniqueId());
        if (account == null) return;

        int upgradeAmount = 1;
        if (event.isRightClick() && event.isShiftClick()) {
            upgradeAmount = 100;
        } else if (event.isRightClick()) {
            upgradeAmount = 15;
        }

        switch (event.getSlot()) {
            case 13 -> collectMoney(player, account);
            case 10 -> buy(player, account, 1);
            case 11 -> buy(player, account, 25);
            case 12 -> buy(player, account, 50);
            case 14 -> buy(player, account, 100);
            case 15 -> buy(player, account, 1000000);
            case 20 -> upgrade(player, account, "money", upgradeAmount);
            case 22 -> upgrade(player, account, "time", upgradeAmount);
            default -> {}
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(AmsGui.TITLE)) return;
        if (!(event.getPlayer() instanceof Player player)) return;
        amsManager.clearOpenAccount(player.getUniqueId());
    }

    private void collectMoney(Player player, AmsAccount account) {
        double amount = account.getStoredMoney();
        if (amount <= 0) {
            player.sendMessage(ChatColor.RED + "Es gibt gerade nichts einzusammeln.");
            return;
        }
        plugin.getEconomy().depositPlayer(player, amount);
        account.clearStoredMoney();
        player.sendMessage(ChatColor.GREEN + "Du hast " + AmsGui.formatMoney(plugin, amount) + " eingesammelt!");
        AmsGui.refresh(player, plugin);
    }

    private void buy(Player player, AmsAccount account, int amount) {
        double cost = amsManager.getPurchasePrice(account.getAmsCount(), amount);

        if (!plugin.getEconomy().has(player, cost)) {
            player.sendMessage(ChatColor.RED + "Du hast nicht genug Geld! Kosten: " + AmsGui.formatMoney(plugin, cost));
            return;
        }

        plugin.getEconomy().withdrawPlayer(player, cost);
        account.addAms(amount);

        player.sendMessage(ChatColor.GREEN + "Du hast " + amount + " AMS gekauft! Du hast jetzt " + account.getAmsCount() + " AMS.");
        AmsGui.refresh(player, plugin);
    }

    private void upgrade(Player player, AmsAccount account, String type, int amountToBuy) {
        int bought = 0;
        double totalCost = 0;

        for (int i = 0; i < amountToBuy; i++) {
            int currentLevel = type.equals("money") ? account.getMoneyLevel() : account.getTimeLevel();
            if (currentLevel >= AmsAccount.MAX_LEVEL) break;

            double cost = AmsGui.upgradeCost(type, currentLevel);
            if (!plugin.getEconomy().has(player, cost)) break;

            plugin.getEconomy().withdrawPlayer(player, cost);
            totalCost += cost;

            if (type.equals("money")) {
                account.setMoneyLevel(currentLevel + 1);
            } else {
                account.setTimeLevel(currentLevel + 1);
            }
            bought++;
        }

        if (bought == 0) {
            int currentLevel = type.equals("money") ? account.getMoneyLevel() : account.getTimeLevel();
            if (currentLevel >= AmsAccount.MAX_LEVEL) {
                player.sendMessage(ChatColor.RED + "Dieses Upgrade ist bereits auf dem Maximum!");
            } else {
                player.sendMessage(ChatColor.RED + "Du hast nicht genug Geld fuer auch nur 1 Level!");
            }
            return;
        }

        player.sendMessage(ChatColor.GREEN + "" + bought + " Level gekauft fuer " + AmsGui.formatMoney(plugin, totalCost) + "!");
        AmsGui.refresh(player, plugin);
    }
}
