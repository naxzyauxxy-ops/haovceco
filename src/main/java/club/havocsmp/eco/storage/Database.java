package club.havocsmp.eco.storage;

import club.havocsmp.eco.HavocEco;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Flat-file player data store backed by a manually managed database.yml, as requested.
 *
 * Layout inside database.yml:
 *   players:
 *     <uuid>:
 *       name: Steve
 *       money: 1000.0
 *       rubies: 25
 *       moneySpent: 0.0
 *       moneyMade: 0.0
 *
 * All writes are guarded by a lock and flushed asynchronously by HavocEco's save task.
 * The abstraction is deliberately thin so you can later drop in a MySQL implementation
 * behind the same method names without touching the rest of the plugin.
 */
public class Database {

    private final HavocEco plugin;
    private final File file;
    private YamlConfiguration data;
    private final ReentrantLock lock = new ReentrantLock();
    private volatile boolean dirty = false;

    public Database(HavocEco plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "database.yml");
    }

    public void load() {
        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create database.yml: " + e.getMessage());
            }
        }
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    private String base(UUID uuid) {
        return "players." + uuid;
    }

    public void ensurePlayer(UUID uuid, String name) {
        lock.lock();
        try {
            String base = base(uuid);
            if (!data.contains(base)) {
                data.set(base + ".name", name);
                data.set(base + ".money", plugin.settings().startingMoney());
                data.set(base + ".rubies", 0L);
                data.set(base + ".moneySpent", 0.0);
                data.set(base + ".moneyMade", 0.0);
                dirty = true;
            } else {
                // keep name fresh
                data.set(base + ".name", name);
                dirty = true;
            }
        } finally {
            lock.unlock();
        }
    }

    public double getMoney(UUID uuid) {
        return data.getDouble(base(uuid) + ".money", 0.0);
    }

    public void setMoney(UUID uuid, double amount) {
        lock.lock();
        try {
            data.set(base(uuid) + ".money", Math.max(0, amount));
            dirty = true;
        } finally {
            lock.unlock();
        }
    }

    public long getRubies(UUID uuid) {
        return data.getLong(base(uuid) + ".rubies", 0L);
    }

    public void setRubies(UUID uuid, long amount) {
        lock.lock();
        try {
            data.set(base(uuid) + ".rubies", Math.max(0, amount));
            dirty = true;
        } finally {
            lock.unlock();
        }
    }

    public double getStat(UUID uuid, String stat) {
        return data.getDouble(base(uuid) + "." + stat, 0.0);
    }

    public void addStat(UUID uuid, String stat, double delta) {
        lock.lock();
        try {
            double cur = data.getDouble(base(uuid) + "." + stat, 0.0);
            data.set(base(uuid) + "." + stat, cur + delta);
            dirty = true;
        } finally {
            lock.unlock();
        }
    }

    public YamlConfiguration raw() {
        return data;
    }

    public void markDirty() {
        dirty = true;
    }

    /** Called by the async save task; only writes to disk if something changed. */
    public void saveIfDirty() {
        if (!dirty) return;
        lock.lock();
        try {
            data.save(file);
            dirty = false;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save database.yml: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }
}
