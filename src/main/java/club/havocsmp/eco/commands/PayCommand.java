package club.havocsmp.eco.commands;

import club.havocsmp.eco.HavocEco;
import club.havocsmp.eco.util.Numbers;
import club.havocsmp.eco.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PayCommand implements CommandExecutor {

    private final HavocEco plugin;

    public PayCommand(HavocEco plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length < 2) {
            p.sendMessage(Text.color("&#f40d0dUsage: /pay <player> <amount>"));
            return true;
        }
        Player target = plugin.getServer().getPlayerExact(args[0]);
        if (target == null) {
            p.sendMessage(Text.color("&#f40d0dPlayer not found."));
            return true;
        }
        if (target.getUniqueId().equals(p.getUniqueId())) {
            p.sendMessage(Text.color("&#f40d0dYou cannot pay yourself."));
            return true;
        }
        if (!Numbers.isPositiveNumber(args[1])) {
            p.sendMessage(Text.color("&#f40d0dThat number is invalid."));
            return true;
        }
        double amount = Double.parseDouble(args[1]);
        if (!plugin.economy().transfer(p.getUniqueId(), target.getUniqueId(), amount)) {
            p.sendMessage(Text.color("&#f40d0dYou do not have enough money."));
            return true;
        }
        p.sendMessage(Text.color("&#f40d0dPaid &#f40d0d" + target.getName() + " &#f40d0d$" + Numbers.comma(amount)));
        target.sendMessage(Text.color("&#f40d0dReceived &#f40d0d$" + Numbers.comma(amount) + " &#f40d0dfrom &#f40d0d" + p.getName()));
        return true;
    }
}
