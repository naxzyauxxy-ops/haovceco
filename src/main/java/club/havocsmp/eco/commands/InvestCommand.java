package club.havocsmp.eco.commands;

import club.havocsmp.eco.HavocEco;
import club.havocsmp.eco.util.Numbers;
import club.havocsmp.eco.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class InvestCommand implements CommandExecutor {

    private final HavocEco plugin;

    public InvestCommand(HavocEco plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Players only.");
            return true;
        }
        var inv = plugin.invest();

        if (args.length == 0) {
            if (inv.hasActive(p.getUniqueId())) {
                long remaining = Math.max(0, (inv.maturesAt(p.getUniqueId()) - System.currentTimeMillis()) / 1000);
                p.sendMessage(Text.color("&#f40d0dActive investment: &#f40d0d$" + Numbers.comma(inv.activeAmount(p.getUniqueId()))
                        + " &#f40d0dmatures in &#f40d0d" + remaining + "s"));
            } else {
                p.sendMessage(Text.color("&#f40d0dUsage: /invest <amount>  &7(locks money away, returns more later)"));
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("claim")) {
            double payout = inv.tryClaim(p.getUniqueId());
            if (payout < 0) p.sendMessage(Text.color("&#f40d0dNothing to claim yet."));
            else p.sendMessage(Text.color("&aClaimed " + Numbers.comma(payout) + "!"));
            return true;
        }
        if (!Numbers.isPositiveNumber(args[0])) {
            p.sendMessage(Text.color("&#f40d0dThat number is invalid."));
            return true;
        }
        String error = inv.invest(p.getUniqueId(), Double.parseDouble(args[0]));
        if (error != null) {
            p.sendMessage(Text.color("&#f40d0d" + error));
        } else {
            p.sendMessage(Text.color("&aInvestment placed. It will mature in "
                    + plugin.settings().investMaturityMinutes() + " minutes."));
        }
        return true;
    }
}
