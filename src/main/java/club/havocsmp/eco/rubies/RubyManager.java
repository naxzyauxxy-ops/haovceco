package club.havocsmp.eco.rubies;

import club.havocsmp.eco.HavocEco;
import club.havocsmp.eco.storage.Database;

import java.util.UUID;

/** Rubies (your premium/shard currency). Honors the double-ruby event multiplier on grants. */
public class RubyManager {

    private final HavocEco plugin;
    private final Database db;

    public RubyManager(HavocEco plugin) {
        this.plugin = plugin;
        this.db = plugin.database();
    }

    public long get(UUID uuid) {
        return db.getRubies(uuid);
    }

    public boolean has(UUID uuid, long amount) {
        return db.getRubies(uuid) >= amount;
    }

    /**
     * Grant rubies from a "reward" source (playing on the server, events, etc).
     * The double-ruby event multiplier is applied here so all reward paths benefit.
     */
    public long grantReward(UUID uuid, long baseAmount) {
        long amount = baseAmount;
        if (plugin.settings().doubleRubyEventOrScheduler()) {
            amount = baseAmount * plugin.settings().doubleRubyMultiplier();
        }
        db.setRubies(uuid, db.getRubies(uuid) + amount);
        return amount;
    }

    /** Plain grant with no multiplier (e.g. purchases, admin give). */
    public void give(UUID uuid, long amount) {
        db.setRubies(uuid, db.getRubies(uuid) + amount);
    }

    public boolean take(UUID uuid, long amount) {
        long cur = db.getRubies(uuid);
        if (cur < amount) return false;
        db.setRubies(uuid, cur - amount);
        return true;
    }

    public void set(UUID uuid, long amount) {
        db.setRubies(uuid, amount);
    }

    public boolean transfer(UUID from, UUID to, long amount) {
        if (amount <= 0) return false;
        if (!take(from, amount)) return false;
        give(to, amount);
        return true;
    }
}
