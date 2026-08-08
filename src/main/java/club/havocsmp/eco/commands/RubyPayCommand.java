package club.havocsmp.eco.commands;

import club.havocsmp.eco.HavocEco;
import club.havocsmp.eco.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RubyPayCommand implements CommandExecutor {

    private final HavocEco plugin;

    public RubyPayCommand(HavocEco plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!plugin.settings().rubyPayEnabled()) {
            sender.sendMessage(Text.color("&#f40d0dRuby transfers are disabled."));
            return true;
        }
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length < 2) {
            p.sendMessage(Text.color("&#f40d0dUsage: /rubypay <player> <amount>"));
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
        long amount;
        try {
            amount = Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            p.sendMessage(Text.color("&#f40d0dThat number is invalid."));
            return true;
        }
        if (amount <= 0) {
            p.sendMessage(Text.color("&#f40d0dThat number is invalid."));
            return true;
        }
        if (!plugin.rubies().transfer(p.getUniqueId(), target.getUniqueId(), amount)) {
            p.sendMessage(Text.color("&#f40d0dYou do not have enough " + plugin.settings().rubyName() + "."));
            return true;
        }
        String r = plugin.settings().rubyName();
        p.sendMessage(Text.color("&#f40d0dSent &#f40d0d" + amount + " " + r + " &#f40d0dto &#f40d0d" + target.getName()));
        target.sendMessage(Text.color("&#f40d0dReceived &#f40d0d" + amount + " " + r + " &#f40d0dfrom &#f40d0d" + p.getName()));
        return true;
    }
}
