package club.havocsmp.eco.casino;

import club.havocsmp.eco.HavocEco;
import club.havocsmp.eco.util.Numbers;
import club.havocsmp.eco.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Casino: /slots (instant) and /jackpot (pooled, timed). Both can use money OR rubies.
 * House edge is configurable so the games stay a net sink rather than a money printer.
 */
public class CasinoManager {

    private final HavocEco plugin;

    // Active jackpot round (only one at a time, server-wide).
    private boolean jackpotRunning = false;
    private String jackpotCurrency = "money";
    private double jackpotPool = 0;
    private final Map<UUID, Double> jackpotEntries = new LinkedHashMap<>();

    public CasinoManager(HavocEco plugin) {
        this.plugin = plugin;
    }

    // ---------------- SLOTS ----------------

    /** currency = "money" or "rubies". Returns a result message already colored. */
    public String playSlots(Player player, double bet, String currency) {
        var s = plugin.settings();
        UUID uuid = player.getUniqueId();
        if (!s.slotsEnabled()) return Text.color("&cSlots are disabled.");
        if (bet < s.slotsMinBet()) return Text.color("&cMinimum bet is " + Numbers.comma(s.slotsMinBet()));
        if (bet > s.slotsMaxBet()) return Text.color("&cMaximum bet is " + Numbers.comma(s.slotsMaxBet()));

        if (!chargeBet(uuid, bet, currency)) {
            return Text.color("&cYou don't have enough " + currency + ".");
        }

        // Three reels. Symbol weights are chosen so the expected payout is below 1.0,
        // with the exact house edge coming from config.
        String[] symbols = {"\uD83C\uDF52", "\uD83C\uDF4B", "\uD83D\uDD14", "\u2B50", "7\uFE0F"};
        int r1 = spin(), r2 = spin(), r3 = spin();

        double multiplier;
        if (r1 == r2 && r2 == r3) {
            // triple: bigger for rarer symbols
            multiplier = switch (r1) {
                case 4 -> 15.0; // 7
                case 3 -> 8.0;  // star
                case 2 -> 5.0;  // bell
                case 1 -> 3.0;  // lemon
                default -> 2.0; // cherry
            };
        } else if (r1 == r2 || r2 == r3 || r1 == r3) {
            multiplier = 1.5;
        } else {
            multiplier = 0.0;
        }

        // Apply house edge to winnings.
        double edge = 1.0 - (s.slotsHouseEdge() / 100.0);
        double payout = bet * multiplier * edge;

        String reels = symbols[r1] + " " + symbols[r2] + " " + symbols[r3];
        if (payout > 0) {
            payBet(uuid, payout, currency);
            return Text.color("&f[ " + reels + " &f] &aYou won " + Numbers.comma(payout) + " " + currency + "!");
        } else {
            return Text.color("&f[ " + reels + " &f] &cNo win. Better luck next spin.");
        }
    }

    private int spin() {
        return ThreadLocalRandom.current().nextInt(5);
    }

    // ---------------- JACKPOT ----------------

    public String startOrJoinJackpot(Player player, double bet, String currency) {
        var s = plugin.settings();
        UUID uuid = player.getUniqueId();
        if (!s.jackpotEnabled()) return Text.color("&cJackpot is disabled.");
        if (bet < s.jackpotMinBet()) return Text.color("&cMinimum entry is " + Numbers.comma(s.jackpotMinBet()));

        if (jackpotRunning && !jackpotCurrency.equals(currency)) {
            return Text.color("&cCurrent jackpot uses " + jackpotCurrency + ". Wait for it to finish.");
        }
        if (!chargeBet(uuid, bet, currency)) {
            return Text.color("&cYou don't have enough " + currency + ".");
        }

        jackpotEntries.merge(uuid, bet, Double::sum);
        jackpotPool += bet;

        if (!jackpotRunning) {
            jackpotRunning = true;
            jackpotCurrency = currency;
            Bukkit.broadcastMessage(Text.color("&#f40d0d&lJACKPOT &fstarted! &7/jackpot " + (long) bet + " " + currency
                    + " &fto enter. Draw in " + s.jackpotDurationSeconds() + "s."));
            Bukkit.getScheduler().runTaskLater(plugin, this::drawJackpot, s.jackpotDurationSeconds() * 20L);
        }
        return Text.color("&aEntered the jackpot with " + Numbers.comma(bet) + " " + currency
                + ". Pool: " + Numbers.comma(jackpotPool));
    }

    private void drawJackpot() {
        if (!jackpotRunning) return;
        var s = plugin.settings();
        if (jackpotEntries.isEmpty()) {
            jackpotRunning = false;
            return;
        }
        // Weighted random winner by contribution.
        double roll = ThreadLocalRandom.current().nextDouble(jackpotPool);
        double cumulative = 0;
        UUID winner = null;
        for (var e : jackpotEntries.entrySet()) {
            cumulative += e.getValue();
            if (roll <= cumulative) {
                winner = e.getKey();
                break;
            }
        }
        if (winner == null) winner = jackpotEntries.keySet().iterator().next();

        double cut = jackpotPool * (s.jackpotHouseCut() / 100.0);
        double prize = jackpotPool - cut;
        payBet(winner, prize, jackpotCurrency);

        String name = Bukkit.getOfflinePlayer(winner).getName();
        Bukkit.broadcastMessage(Text.color("&#f40d0d&lJACKPOT &fwon by &#f40d0d" + name
                + " &ffor " + Numbers.comma(prize) + " " + jackpotCurrency + "!"));

        jackpotRunning = false;
        jackpotPool = 0;
        jackpotEntries.clear();
    }

    // ---------------- shared currency helpers ----------------

    private boolean chargeBet(UUID uuid, double bet, String currency) {
        if (currency.equalsIgnoreCase("rubies")) {
            return plugin.rubies().take(uuid, (long) bet);
        }
        return plugin.economy().withdraw(uuid, bet);
    }

    private void payBet(UUID uuid, double amount, String currency) {
        if (currency.equalsIgnoreCase("rubies")) {
            plugin.rubies().give(uuid, (long) amount);
        } else {
            plugin.economy().deposit(uuid, amount);
        }
    }
}
