package club.havocsmp.eco.commands;

import club.havocsmp.eco.HavocEco;
import club.havocsmp.eco.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LiveCommand implements CommandExecutor {
    private final HavocEco plugin;
    public LiveCommand(HavocEco plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        // Media+ permission gate.
        if (!p.hasPermission("havoceco.live")) {
            p.sendMessage(Text.color("&#f40d0dThis command is for Media+ ranks."));
            return true;
        }
        if (args.length < 1) {
            p.sendMessage(Text.color("&#f40d0dUsage: /live <link or message>"));
            return true;
        }
        long remaining = plugin.cooldowns().remaining("live", p.getUniqueId());
        if (remaining > 0 && !p.hasPermission("havoceco.live.bypass")) {
            long hrs = remaining / 3600, mins = (remaining % 3600) / 60;
            p.sendMessage(Text.color("&#f40d0dYou can use /live again in " + hrs + "h " + mins + "m."));
            return true;
        }
        String msg = String.join(" ", args);
        String format = plugin.getConfig().getString("LIVE.ANNOUNCEMENT",
                "&#f40d0d&lLIVE &f%player% is now live! &7%message%");
        Bukkit.broadcastMessage(Text.color(format.replace("%player%", p.getName()).replace("%message%", msg)));
        plugin.cooldowns().set("live", p.getUniqueId(), plugin.settings().liveCooldownSeconds());
        return true;
    }
}
