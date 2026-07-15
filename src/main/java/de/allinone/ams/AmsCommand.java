package de.allinone.ams;

import de.allinone.ams.data.AmsManager;
import de.allinone.ams.gui.AmsGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AmsCommand implements CommandExecutor {

    private final AmsPlugin plugin;
    private final AmsManager amsManager;

    public AmsCommand(AmsPlugin plugin, AmsManager amsManager) {
        this.plugin = plugin;
        this.amsManager = amsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dieser Befehl kann nur von Spielern genutzt werden.");
            return true;
        }

        AmsGui.open(player, plugin);
        return true;
    }
}
