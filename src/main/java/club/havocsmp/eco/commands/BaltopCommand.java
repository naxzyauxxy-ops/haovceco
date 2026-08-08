package club.havocsmp.eco.commands;

import club.havocsmp.eco.HavocEco;
import club.havocsmp.eco.economy.BaltopManager;
import club.havocsmp.eco.util.Numbers;
import club.havocsmp.eco.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BaltopCommand implements CommandExecutor {
    private final HavocEco plugin;
    public BaltopCommand(HavocEco plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        List<BaltopManager.Entry> top = plugin.baltop().top();
        sender.sendMessage(Text.color("&#f40d0d&lTOP BALANCES"));
        if (top.isEmpty()) { sender.sendMessage(Text.color("&7No data yet.")); return true; }
        int shown = Math.min(50, top.size());
        for (int i = 0; i < shown; i++) {
            BaltopManager.Entry e = top.get(i);
            // Placement 1-50 shown BEFORE the name, as requested.
            sender.sendMessage(Text.color("&#f40d0d#" + (i + 1) + " &f" + e.name() + " &7- &#f40d0d$" + Numbers.comma(e.money())));
        }
        return true;
    }
}
