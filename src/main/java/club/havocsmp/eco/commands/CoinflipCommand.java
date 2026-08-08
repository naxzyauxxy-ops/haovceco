package club.havocsmp.eco.commands;

import club.havocsmp.eco.HavocEco;
import club.havocsmp.eco.util.Numbers;
import club.havocsmp.eco.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CoinflipCommand implements CommandExecutor {
    private final HavocEco plugin;
    public CoinflipCommand(HavocEco plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (args.length == 0) {
            p.sendMessage(Text.color("&#f40d0d/cf <amount> [money|rubies]  &7- open a coinflip"));
            p.sendMessage(Text.color("&#f40d0d/cf accept <player>  &7- accept a coinflip"));
            p.sendMessage(Text.color("&#f40d0d/cf cancel  &7- cancel & refund your coinflip"));
            return true;
        }
        if (args[0].equalsIgnoreCase("accept")) {
            if (args.length < 2) { p.sendMessage(Text.color("&#f40d0dUsage: /cf accept <player>")); return true; }
            String err = plugin.wagers().accept(p, args[1]);
            if (err != null) p.sendMessage(err);
            return true;
        }
        if (args[0].equalsIgnoreCase("cancel")) {
            p.sendMessage(plugin.wagers().cancel(p));
            return true;
        }
        if (!Numbers.isPositiveNumber(args[0])) { p.sendMessage(Text.color("&#f40d0dThat number is invalid.")); return true; }
        double amount = Double.parseDouble(args[0]);
        String currency = args.length >= 2 ? args[1].toLowerCase() : "money";
        if (!currency.equals("money") && !currency.equals("rubies")) currency = "money";
        p.sendMessage(plugin.wagers().openCoinflip(p, amount, currency));
        return true;
    }
}
