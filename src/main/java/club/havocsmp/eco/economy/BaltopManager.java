package club.havocsmp.eco.economy;

import club.havocsmp.eco.HavocEco;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Money leaderboard. Reads all players from database.yml, sorts by balance, and exposes
 * the top 50 with 1-based placement (shown before names, as requested).
 * Refreshed on a timer so /baltop is instant and doesn't scan the file on every call.
 */
public class BaltopManager {

    private final HavocEco plugin;
    private final List<Entry> top = new ArrayList<>();

    public BaltopManager(HavocEco plugin) {
        this.plugin = plugin;
    }

    public void refresh() {
        List<Entry> all = new ArrayList<>();
        ConfigurationSection players = plugin.database().raw().getConfigurationSection("players");
        if (players != null) {
            for (String uuidStr : players.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    String name = players.getString(uuidStr + ".name", "Unknown");
                    double money = players.getDouble(uuidStr + ".money", 0);
                    all.add(new Entry(uuid, name, money));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        all.sort(Comparator.comparingDouble((Entry e) -> e.money).reversed());
        synchronized (top) {
            top.clear();
            top.addAll(all.subList(0, Math.min(50, all.size())));
        }
    }

    public List<Entry> top() {
        synchronized (top) {
            return new ArrayList<>(top);
        }
    }

    public record Entry(UUID uuid, String name, double money) {}
}
