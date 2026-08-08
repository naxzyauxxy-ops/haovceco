package club.havocsmp.eco.commands;

import club.havocsmp.eco.HavocEco;
import club.havocsmp.eco.util.Numbers;
import club.havocsmp.eco.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BalanceCommand implements CommandExecutor {

    private final HavocEco plugin;

    public BalanceCommand(HavocEco plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage("Console must specify a player.");
                return true;
            }
            double bal = plugin.economy().getBalance(p.getUniqueId());
            long rub = plugin.rubies().get(p.getUniqueId());
            p.sendMessage(Text.color("&#f40d0dBalance: &#f40d0d$" + Numbers.comma(bal)
                    + " &7| &#f40d0d" + rub + " " + plugin.settings().rubyName()));
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        double bal = plugin.economy().getBalance(target.getUniqueId());
        sender.sendMessage(Text.color("&#f40d0d" + target.getName() + " &#f40d0dBalance: &#f40d0d$" + Numbers.comma(bal)));
        return true;
    }
}
