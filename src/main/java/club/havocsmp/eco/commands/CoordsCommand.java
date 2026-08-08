package club.havocsmp.eco.commands;

import club.havocsmp.eco.HavocEco;
import club.havocsmp.eco.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CoordsCommand implements CommandExecutor {
    private final HavocEco plugin;
    public CoordsCommand(HavocEco plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        if (!plugin.settings().coordHideAllowed()) {
            p.sendMessage(Text.color("&#f40d0dCoordinate hiding is disabled."));
            return true;
        }
        boolean nowHidden = plugin.coordHider().toggle(p.getUniqueId());
        p.sendMessage(Text.color(nowHidden
                ? "&#f40d0dYour coordinates are now HIDDEN."
                : "&#f40d0dYour coordinates are now SHOWN."));
        return true;
    }
}
