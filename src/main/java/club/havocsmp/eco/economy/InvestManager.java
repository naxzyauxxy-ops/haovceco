package club.havocsmp.eco.economy;

import club.havocsmp.eco.HavocEco;

import java.util.UUID;

/**
 * /invest — a deliberate money sink. Players lock money away; it matures after a delay
 * and returns slightly more than deposited, but a fee is taken up front so the net effect
 * across the economy is money removed (that's the point of a sink). Tuned via config so
 * you can keep it from being overpowered.
 *
 * Data is stored in database.yml under players.<uuid>.invest.
 */
public class InvestManager {

    private final HavocEco plugin;

    public InvestManager(HavocEco plugin) {
        this.plugin = plugin;
    }

    private String base(UUID uuid) {
        return "players." + uuid + ".invest";
    }

    public boolean hasActive(UUID uuid) {
        return plugin.database().raw().getBoolean(base(uuid) + ".active", false);
    }

    public double activeAmount(UUID uuid) {
        return plugin.database().raw().getDouble(base(uuid) + ".principal", 0);
    }

    public long maturesAt(UUID uuid) {
        return plugin.database().raw().getLong(base(uuid) + ".maturesAt", 0);
    }

    /** Returns null on success, or an error message string. */
    public String invest(UUID uuid, double amount) {
        var s = plugin.settings();
        if (!s.investEnabled()) return "Investing is disabled.";
        if (hasActive(uuid)) return "You already have an active investment.";
        if (amount < s.investMin()) return "Minimum investment is " + s.currencySymbol() + s.investMin() + ".";
        if (amount > s.investMaxActive()) return "Maximum investment is " + s.currencySymbol() + s.investMaxActive() + ".";
        if (!plugin.economy().has(uuid, amount)) return "You don't have that much money.";

        // Fee is taken immediately and removed from the economy entirely.
        double fee = amount * (s.investFeePercent() / 100.0);
        double principal = amount - fee;

        plugin.economy().withdraw(uuid, amount);

        var raw = plugin.database().raw();
        raw.set(base(uuid) + ".active", true);
        raw.set(base(uuid) + ".principal", principal);
        raw.set(base(uuid) + ".maturesAt", System.currentTimeMillis() + (long) s.investMaturityMinutes() * 60_000L);
        plugin.database().markDirty();
        return null;
    }

    /** Returns the payout if matured & claimed, or -1 if not ready / none active. */
    public double tryClaim(UUID uuid) {
        if (!hasActive(uuid)) return -1;
        if (System.currentTimeMillis() < maturesAt(uuid)) return -1;
        var s = plugin.settings();
        double principal = activeAmount(uuid);
        double payout = principal * s.investReturnRate();

        plugin.economy().deposit(uuid, payout);

        var raw = plugin.database().raw();
        raw.set(base(uuid) + ".active", false);
        raw.set(base(uuid) + ".principal", 0);
        raw.set(base(uuid) + ".maturesAt", 0);
        plugin.database().markDirty();
        return payout;
    }
}
