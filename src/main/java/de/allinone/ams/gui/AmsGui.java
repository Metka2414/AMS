package de.allinone.ams.gui;

import de.allinone.ams.AmsPlugin;
import de.allinone.ams.data.AmsAccount;
import de.allinone.ams.data.AmsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class AmsGui {

    public static final String TITLE = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "AMS - Digitale Spawner";

    public static void open(Player player, AmsPlugin plugin) {
        Inventory inv = build(player, plugin);
        player.openInventory(inv);
    }

    /**
     * Aktualisiert ein bereits geoeffnetes AMS-GUI ohne es zu schliessen und neu zu oeffnen.
     */
    public static void refresh(Player player, AmsPlugin plugin) {
        var openInv = player.getOpenInventory();
        if (openInv == null || !openInv.getTitle().equals(TITLE)) {
            open(player, plugin);
            return;
        }
        Inventory fresh = build(player, plugin);
        Inventory top = openInv.getTopInventory();
        for (int i = 0; i < fresh.getSize(); i++) {
            top.setItem(i, fresh.getItem(i));
        }
    }

    private static Inventory build(Player player, AmsPlugin plugin) {
        AmsManager manager = plugin.getAmsManager();
        AmsAccount account = manager.get(player.getUniqueId());
        manager.setOpenSpawner(player.getUniqueId(), account);

        Inventory inv = Bukkit.createInventory(player, 27, TITLE);

        inv.setItem(4, namedItem(Material.NETHER_STAR, ChatColor.LIGHT_PURPLE + "Deine AMS-Uebersicht", List.of(
                ChatColor.GRAY + "AMS: " + ChatColor.WHITE + account.getAmsCount(),
                ChatColor.GRAY + "Intervall: " + ChatColor.WHITE + account.getIntervalSeconds() + "s",
                ChatColor.GRAY + "Geld/Zyklus: " + ChatColor.GREEN + formatMoney(plugin, account.getMoneyPerCycle()),
                "",
                ChatColor.YELLOW + "Angesammelt: " + ChatColor.GOLD + formatMoney(plugin, account.getStoredMoney())
        )));

        inv.setItem(13, namedItem(Material.GOLD_INGOT, ChatColor.GOLD + "Geld einsammeln", List.of(
                ChatColor.GRAY + "Sammelt " + formatMoney(plugin, account.getStoredMoney()) + " ein.",
                ChatColor.YELLOW + "Klick zum Einsammeln"
        )));

        inv.setItem(10, buyItem(plugin, account, 1));
        inv.setItem(11, buyItem(plugin, account, 25));
        inv.setItem(12, buyItem(plugin, account, 50));
        inv.setItem(14, buyItem(plugin, account, 100));
        inv.setItem(15, buyItem(plugin, account, 1000000));

        inv.setItem(20, namedItem(Material.EMERALD, ChatColor.AQUA + "Geld-Upgrade (" + account.getMoneyLevel() + "/100)", List.of(
                ChatColor.GRAY + "+2% Geld pro Level",
                ChatColor.YELLOW + "Kosten: " + formatMoney(plugin, upgradeCost("money", account.getMoneyLevel())),
                ChatColor.GREEN + "Linksklick = 1 Level",
                ChatColor.GREEN + "Rechtsklick = 15 Level",
                ChatColor.GREEN + "Shift-Rechtsklick = 100 Level"
        )));

        inv.setItem(22, namedItem(Material.CLOCK, ChatColor.AQUA + "Zeit-Upgrade (" + account.getTimeLevel() + "/100)", List.of(
                ChatColor.GRAY + "Aktuell: " + account.getIntervalSeconds() + "s pro Zyklus",
                ChatColor.YELLOW + "Kosten: " + formatMoney(plugin, upgradeCost("time", account.getTimeLevel())),
                ChatColor.GREEN + "Linksklick = 1 Level",
                ChatColor.GREEN + "Rechtsklick = 15 Level",
                ChatColor.GREEN + "Shift-Rechtsklick = 100 Level"
        )));

        return inv;
    }

    private static ItemStack buyItem(AmsPlugin plugin, AmsAccount account, int amount) {
        double cost = plugin.getAmsManager().getPurchasePrice(account.getAmsCount(), amount);
        return namedItem(Material.OBSERVER, ChatColor.LIGHT_PURPLE + "" + amount + " AMS kaufen", List.of(
                ChatColor.GRAY + "Kosten: " + ChatColor.WHITE + formatMoney(plugin, cost),
                ChatColor.GREEN + "Klick zum Kaufen"
        ));
    }

    public static double upgradeCost(String type, int currentLevel) {
        double base = type.equals("money") ? 8000 : 7000;
        return base * Math.pow(1.08, currentLevel);
    }

    private static ItemStack namedItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static String formatMoney(AmsPlugin plugin, double amount) {
        if (plugin.getEconomy() != null) {
            return plugin.getEconomy().format(amount);
        }
        return String.format("%.2f", amount);
    }
}
