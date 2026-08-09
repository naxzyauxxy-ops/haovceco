package club.havocsmp.eco.wager;

import club.havocsmp.eco.HavocEco;
import club.havocsmp.eco.util.Numbers;
import club.havocsmp.eco.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Coinflip wagers. A player opens a coinflip with a stake (money or rubies); another player
 * accepts, and a 50/50 flip decides the winner who takes both stakes minus an optional house tax.
 *
 * One open coinflip per player. Stakes are held (removed on open, refunded on cancel/timeout).
 */
public class WagerManager {

    private final HavocEco plugin;
    private final Map<UUID, Coinflip> open = new HashMap<>();

    public WagerManager(HavocEco plugin) {
        this.plugin = plugin;
    }

    public String openCoinflip(Player host, double amount, String currency) {
        var cfg = plugin.getConfig();
        if (!cfg.getBoolean("WAGER.ENABLED", true)) return Text.color("&cWagers are disabled.");
        if (amount < cfg.getDouble("WAGER.MIN-BET", 100)) {
            return Text.color("&cMinimum wager is " + Numbers.comma(cfg.getDouble("WAGER.MIN-BET", 100)));
        }
        if (open.containsKey(host.getUniqueId())) return Text.color("&cYou already have an open coinflip. /cf cancel");
        if (!charge(host.getUniqueId(), amount, currency)) {
            return Text.color("&cYou don't have enough " + currency + ".");
        }
        Coinflip cf = new Coinflip(host.getUniqueId(), amount, currency);
        open.put(host.getUniqueId(), cf);
        Bukkit.broadcastMessage(Text.color("&#f40d0d&lCOINFLIP &f" + host.getName() + " opened a "
                + Numbers.comma(amount) + " " + currency + " coinflip! &7/cf accept " + host.getName()));
        return Text.color("&aCoinflip opened. Waiting for someone to accept.");
    }

    public String accept(Player challenger, String hostName) {
        Player host = Bukkit.getPlayerExact(hostName);
        if (host == null) return Text.color("&cThat player isn't online.");
        Coinflip cf = open.get(host.getUniqueId());
        if (cf == null) return Text.color("&cThat player has no open coinflip.");
        if (host.getUniqueId().equals(challenger.getUniqueId())) return Text.color("&cYou can't accept your own coinflip.");
        if (!charge(challenger.getUniqueId(), cf.amount, cf.currency)) {
            return Text.color("&cYou don't have enough " + cf.currency + ".");
        }
        open.remove(host.getUniqueId());

        // Flip.
        boolean hostWins = ThreadLocalRandom.current().nextBoolean();
        UUID winner = hostWins ? host.getUniqueId() : challenger.getUniqueId();
        UUID loser = hostWins ? challenger.getUniqueId() : host.getUniqueId();

        double pot = cf.amount * 2;
        double tax = pot * (plugin.getConfig().getDouble("WAGER.HOUSE-TAX-PERCENT", 5.0) / 100.0);
        double prize = pot - tax;
        pay(winner, prize, cf.currency);

        String winnerName = Bukkit.getOfflinePlayer(winner).getName();
        String loserName = Bukkit.getOfflinePlayer(loser).getName();
        Bukkit.broadcastMessage(Text.color("&#f40d0d&lCOINFLIP &f" + winnerName + " beat " + loserName
                + " and won " + Numbers.comma(prize) + " " + cf.currency + "!"));
        return null;
    }

    public String cancel(Player host) {
        Coinflip cf = open.remove(host.getUniqueId());
        if (cf == null) return Text.color("&cYou have no open coinflip.");
        pay(host.getUniqueId(), cf.amount, cf.currency); // refund
        return Text.color("&aCoinflip cancelled and stake refunded.");
    }

    private boolean charge(UUID uuid, double amount, String currency) {
        if (currency.equalsIgnoreCase("rubies")) return plugin.rubies().take(uuid, (long) amount);
        if (!plugin.economy().isReady()) return false;
        return plugin.economy().withdraw(uuid, amount);
    }

    private void pay(UUID uuid, double amount, String currency) {
        if (currency.equalsIgnoreCase("rubies")) plugin.rubies().give(uuid, (long) amount);
        else plugin.economy().deposit(uuid, amount);
    }

    private static class Coinflip {
        final UUID host;
        final double amount;
        final String currency;
        Coinflip(UUID host, double amount, String currency) {
            this.host = host; this.amount = amount; this.currency = currency;
        }
    }
}
