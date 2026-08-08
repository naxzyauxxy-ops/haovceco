package club.havocsmp.eco.config;

import club.havocsmp.eco.HavocEco;
import org.bukkit.configuration.file.FileConfiguration;

/** Typed accessors over config.yml so the rest of the plugin never touches raw config paths. */
public class Settings {

    private final HavocEco plugin;

    public Settings(HavocEco plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration c() {
        return plugin.getConfig();
    }

    // ---- economy ----
    public double startingMoney() { return c().getDouble("ECONOMY.STARTING-MONEY", 0.0); }
    public String currencySymbol() { return c().getString("ECONOMY.CURRENCY-SYMBOL", "$"); }
    public String rubyName() { return c().getString("ECONOMY.RUBY-NAME", "Rubies"); }

    // ---- invest (money sink) ----
    public boolean investEnabled() { return c().getBoolean("INVEST.ENABLED", true); }
    public double investMin() { return c().getDouble("INVEST.MIN-AMOUNT", 100); }
    public double investReturnRate() { return c().getDouble("INVEST.RETURN-RATE", 1.05); }
    public double investFeePercent() { return c().getDouble("INVEST.FEE-PERCENT", 5.0); }
    public int investMaturityMinutes() { return c().getInt("INVEST.MATURITY-MINUTES", 60); }
    public double investMaxActive() { return c().getDouble("INVEST.MAX-ACTIVE-AMOUNT", 1_000_000); }

    // ---- casino ----
    public boolean slotsEnabled() { return c().getBoolean("CASINO.SLOTS.ENABLED", true); }
    public double slotsMinBet() { return c().getDouble("CASINO.SLOTS.MIN-BET", 100); }
    public double slotsMaxBet() { return c().getDouble("CASINO.SLOTS.MAX-BET", 100000); }
    public double slotsHouseEdge() { return c().getDouble("CASINO.SLOTS.HOUSE-EDGE-PERCENT", 8.0); }
    public boolean jackpotEnabled() { return c().getBoolean("CASINO.JACKPOT.ENABLED", true); }
    public double jackpotMinBet() { return c().getDouble("CASINO.JACKPOT.MIN-BET", 500); }
    public int jackpotDurationSeconds() { return c().getInt("CASINO.JACKPOT.DURATION-SECONDS", 60); }
    public double jackpotHouseCut() { return c().getDouble("CASINO.JACKPOT.HOUSE-CUT-PERCENT", 5.0); }

    // ---- rubies ----
    public boolean rubyPayEnabled() { return c().getBoolean("RUBIES.PAY-ENABLED", true); }
    public boolean doubleRubyEvent() { return c().getBoolean("RUBIES.DOUBLE-EVENT-ACTIVE", false); }
    public int doubleRubyMultiplier() { return c().getInt("RUBIES.DOUBLE-EVENT-MULTIPLIER", 2); }
    /** True if the manual flag is on OR the runtime scheduler flag is active. */
    public boolean doubleRubyEventOrScheduler() {
        return doubleRubyEvent() || plugin.rubyEventActive();
    }

    // ---- combat / pearls ----
    public boolean pearlsKeepOnDeath() { return c().getBoolean("PEARLS.KEEP-ON-DEATH", true); }
    public boolean pearlsNoCombatTag() { return c().getBoolean("PEARLS.NO-COMBAT-TAG", true); }
    public boolean windChargeNoCombatTag() { return c().getBoolean("PEARLS.WIND-CHARGE-NO-COMBAT-TAG", true); }
    public int combatTagSeconds() { return c().getInt("COMBAT.TAG-SECONDS", 15); }

    // ---- totem retention ----
    public boolean keepEffectsOnTotem() { return c().getBoolean("TOTEM.KEEP-EFFECTS", true); }

    // ---- spawn jump boost ----
    public boolean spawnJumpBoost() { return c().getBoolean("SPAWN.JUMP-BOOST.ENABLED", true); }
    public int spawnJumpBoostLevel() { return c().getInt("SPAWN.JUMP-BOOST.LEVEL", 2); }

    // ---- coordinate hiding ----
    public boolean coordHideAllowed() { return c().getBoolean("COORDS.HIDE-ALLOWED", true); }

    // ---- bossbar ----
    public boolean bossbarEnabled() { return c().getBoolean("BOSSBAR.ENABLED", true); }
    public int bossbarCycleSeconds() { return c().getInt("BOSSBAR.CYCLE-SECONDS", 6); }

    // ---- /live ----
    public int liveCooldownSeconds() { return c().getInt("LIVE.COOLDOWN-SECONDS", 10800); }

    // ---- save interval ----
    public int saveIntervalSeconds() { return c().getInt("STORAGE.SAVE-INTERVAL-SECONDS", 60); }
}
