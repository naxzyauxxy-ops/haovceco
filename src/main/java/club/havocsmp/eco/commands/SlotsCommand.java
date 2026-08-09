package club.havocsmp.eco.commands;

import club.havocsmp.eco.HavocEco;
import club.havocsmp.eco.util.Numbers;
import club.havocsmp.eco.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SlotsCommand implements CommandExecutor {

    private final HavocEco plugin;

    public SlotsCommand(HavocEco plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Players only.");
            return true;
        }
        // Rubies-only bets still work if Vault is missing; money bets need Vault.
        String currencyArg = args.length >= 2 ? args[1].toLowerCase() : "money";
        if (!plugin.economy().isReady() && !currencyArg.equals("rubies")) {
            p.sendMessage(Text.color("&cMoney slots unavailable: economy plugin not detected. Try /slots <amount> rubies"));
            return true;
        }
        if (args.length < 1) {
            p.sendMessage(Text.color("&#f40d0dUsage: /slots <amount> [money|rubies]"));
            return true;
        }
        if (!Numbers.isPositiveNumber(args[0])) {
            p.sendMessage(Text.color("&#f40d0dThat number is invalid."));
            return true;
        }
        double bet = Double.parseDouble(args[0]);
        String currency = args.length >= 2 ? args[1].toLowerCase() : "money";
        if (!currency.equals("money") && !currency.equals("rubies")) currency = "money";

        p.sendMessage(plugin.casino().playSlots(p, bet, currency));
        return true;
    }
}
