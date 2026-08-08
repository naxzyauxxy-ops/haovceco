package club.havocsmp.eco.commands;

import club.havocsmp.eco.HavocEco;
import club.havocsmp.eco.util.Numbers;
import club.havocsmp.eco.util.Text;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WorthCommand implements CommandExecutor {
    private final HavocEco plugin;
    public WorthCommand(HavocEco plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        Material mat;
        int amount;
        if (args.length == 0) {
            var hand = p.getInventory().getItemInMainHand();
            if (hand == null || hand.getType() == Material.AIR) {
                p.sendMessage(Text.color("&#f40d0dHold an item or use /worth <item>."));
                return true;
            }
            mat = hand.getType();
            amount = hand.getAmount();
            double each = plugin.worth().priceOf(mat);
            if (each < 0) { p.sendMessage(Text.color("&#f40d0dItem cannot be sold.")); return true; }
            p.sendMessage(Text.color("&#f40d0d" + amount + " " + pretty(mat) + " &#f40d0d| &#f40d0d$" + Numbers.comma(each * amount)));
            p.sendMessage(Text.color("&7(Each: &#f40d0d$" + Numbers.comma(each) + "&7)"));
            return true;
        }
        mat = Material.matchMaterial(args[0]);
        if (mat == null) { p.sendMessage(Text.color("&#f40d0dUnknown item.")); return true; }
        double each = plugin.worth().priceOf(mat);
        if (each < 0) { p.sendMessage(Text.color("&#f40d0dItem cannot be sold.")); return true; }
        p.sendMessage(Text.color("&#f40d0d1 " + pretty(mat) + " &#f40d0d| &#f40d0d$" + Numbers.comma(each)));
        return true;
    }

    private static String pretty(Material mat) {
        String s = mat.name().toLowerCase().replace('_', ' ');
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
