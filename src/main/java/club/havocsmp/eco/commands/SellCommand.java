package club.havocsmp.eco.commands;

import club.havocsmp.eco.HavocEco;
import club.havocsmp.eco.menu.SellMenu;
import club.havocsmp.eco.util.Numbers;
import club.havocsmp.eco.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SellCommand implements CommandExecutor {
    private final HavocEco plugin;
    public SellCommand(HavocEco plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (args.length >= 1 && args[0].equalsIgnoreCase("all")) {
            double earned = plugin.sell().sellInventory(p);
            if (earned <= 0) p.sendMessage(Text.color("&#f40d0dYou have nothing sellable."));
            else p.sendMessage(Text.color("&aSold your items for &#f40d0d$" + Numbers.comma(earned)));
            return true;
        }
        if (args.length >= 1 && args[0].equalsIgnoreCase("hand")) {
            var item = p.getInventory().getItemInMainHand();
            double value = plugin.sell().valueOf(item);
            if (value <= 0) { p.sendMessage(Text.color("&#f40d0dItem cannot be sold.")); return true; }
            p.getInventory().setItemInMainHand(null);
            plugin.economy().deposit(p.getUniqueId(), value);
            p.sendMessage(Text.color("&aSold for &#f40d0d$" + Numbers.comma(value)));
            return true;
        }
        new SellMenu(plugin, p).open();
        return true;
    }
}
