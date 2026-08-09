package club.havocsmp.eco.economy;

import club.havocsmp.eco.HavocEco;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

/**
 * Vault economy hook. Voyager registers itself as the Vault economy provider (because it
 * `depends on Vault` in its plugin.yml), so every read and write we do here goes against
 * Voyager's balance. That means /invest, /slots, /jackpot, and coinflips all draw from the
 * SAME money the player sees with /balance in Voyager — no split balances, no reconciliation.
 *
 * If Vault or an economy provider isn't installed, the hook stays in a not-ready state and
 * every feature that touches money refuses cleanly with a clear log message.
 */
public class EconomyHook {

    private final HavocEco plugin;
    private Economy economy;
    private boolean ready = false;

    public EconomyHook(HavocEco plugin) {
        this.plugin = plugin;
    }

    /** Called from onEnable. Returns true if Vault + a provider were found. */
    public boolean setup() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault not found. Money features will be disabled.");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("No Vault economy provider found (Voyager should provide one). Money features disabled.");
            return false;
        }
        economy = rsp.getProvider();
        ready = true;
        plugin.getLogger().info("Hooked into Vault economy: " + economy.getName());
        return true;
    }

    public boolean isReady() { return ready; }

    public String currencyName() {
        return ready ? economy.currencyNamePlural() : "money";
    }

    // ---- balance ops ----

    public double getBalance(UUID uuid) {
        if (!ready) return 0;
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        return economy.getBalance(p);
    }

    public boolean has(UUID uuid, double amount) {
        if (!ready) return false;
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        return economy.has(p, amount);
    }

    public boolean withdraw(UUID uuid, double amount) {
        if (!ready) return false;
        if (amount <= 0) return true;
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        return economy.withdrawPlayer(p, amount).transactionSuccess();
    }

    public boolean deposit(UUID uuid, double amount) {
        if (!ready) return false;
        if (amount <= 0) return true;
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        return economy.depositPlayer(p, amount).transactionSuccess();
    }

    public boolean transfer(UUID from, UUID to, double amount) {
        if (!ready) return false;
        if (amount <= 0) return false;
        if (!withdraw(from, amount)) return false;
        if (!deposit(to, amount)) {
            // rollback if the deposit failed
            deposit(from, amount);
            return false;
        }
        return true;
    }
}
