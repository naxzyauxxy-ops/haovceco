package club.havocsmp.eco.commands;

import club.havocsmp.eco.HavocEco;
import club.havocsmp.eco.util.Numbers;
import club.havocsmp.eco.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class JackpotCommand implements CommandExecutor {

    private final HavocEco plugin;

    public JackpotCommand(HavocEco plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length < 1) {
            p.sendMessage(Text.color("&#f40d0dUsage: /jackpot <amount> [money|rubies]"));
            return true;
        }
        if (!Numbers.isPositiveNumber(args[0])) {
            p.sendMessage(Text.color("&#f40d0dThat number is invalid."));
            return true;
        }
        double bet = Double.parseDouble(args[0]);
        String currency = args.length >= 2 ? args[1].toLowerCase() : "money";
        if (!currency.equals("money") && !currency.equals("rubies")) currency = "money";

        p.sendMessage(plugin.casino().startOrJoinJackpot(p, bet, currency));
        return true;
    }
}
