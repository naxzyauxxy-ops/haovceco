package club.havocsmp.eco.economy;

import club.havocsmp.eco.HavocEco;
import club.havocsmp.eco.storage.Database;

import java.util.UUID;

/** All money operations go through here so stats (moneySpent/moneyMade) stay consistent. */
public class EconomyManager {

    private final HavocEco plugin;
    private final Database db;

    public EconomyManager(HavocEco plugin) {
        this.plugin = plugin;
        this.db = plugin.database();
    }

    public double getBalance(UUID uuid) {
        return db.getMoney(uuid);
    }

    public boolean has(UUID uuid, double amount) {
        return db.getMoney(uuid) >= amount;
    }

    /** Adds money and records it toward the moneyMade stat. */
    public void deposit(UUID uuid, double amount) {
        if (amount <= 0) return;
        db.setMoney(uuid, db.getMoney(uuid) + amount);
        db.addStat(uuid, "moneyMade", amount);
    }

    /** Removes money and records it toward the moneySpent stat. Returns false if insufficient. */
    public boolean withdraw(UUID uuid, double amount) {
        if (amount <= 0) return true;
        double bal = db.getMoney(uuid);
        if (bal < amount) return false;
        db.setMoney(uuid, bal - amount);
        db.addStat(uuid, "moneySpent", amount);
        return true;
    }

    /** Direct set, no stat tracking (admin use). */
    public void set(UUID uuid, double amount) {
        db.setMoney(uuid, amount);
    }

    public boolean transfer(UUID from, UUID to, double amount) {
        if (amount <= 0) return false;
        if (!withdraw(from, amount)) return false;
        deposit(to, amount);
        return true;
    }
}
