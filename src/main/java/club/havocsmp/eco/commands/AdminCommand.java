package club.havocsmp.eco.commands;

import club.havocsmp.eco.HavocEco;
import club.havocsmp.eco.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class AdminCommand implements CommandExecutor, TabCompleter {

    private final HavocEco plugin;

    public AdminCommand(HavocEco plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!sender.hasPermission("havoceco.admin")) {
            sender.sendMessage(Text.color("&cNo permission."));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Text.color("&#f40d0dHavocEco &7admin: reload | givemoney | giverubies | rubyevent"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage(Text.color("&aConfig reloaded."));
            }
            case "givemoney" -> {
                if (args.length < 3) { sender.sendMessage(Text.color("&cUsage: /havoceco givemoney <player> <amount>")); return true; }
                OfflinePlayer t = Bukkit.getOfflinePlayer(args[1]);
                plugin.economy().deposit(t.getUniqueId(), Double.parseDouble(args[2]));
                sender.sendMessage(Text.color("&aGave money to " + args[1] + "."));
            }
            case "giverubies" -> {
                if (args.length < 3) { sender.sendMessage(Text.color("&cUsage: /havoceco giverubies <player> <amount>")); return true; }
                OfflinePlayer t = Bukkit.getOfflinePlayer(args[1]);
                plugin.rubies().give(t.getUniqueId(), Long.parseLong(args[2]));
                sender.sendMessage(Text.color("&aGave rubies to " + args[1] + "."));
            }
            case "rubyevent" -> {
                boolean on = args.length >= 2 && args[1].equalsIgnoreCase("on");
                plugin.setRubyEventActive(on);
                sender.sendMessage(Text.color("&aDouble ruby event " + (on ? "ENABLED" : "DISABLED") + "."));
            }
            default -> sender.sendMessage(Text.color("&cUnknown subcommand."));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload", "givemoney", "giverubies", "rubyevent");
        }
        return List.of();
    }
}
