package club.havocsmp.eco.economy;

import club.havocsmp.eco.HavocEco;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Prices used by amethyst auto-sell tools.
 *
 * IMPORTANT: this is INTENTIONALLY separate from Voyager's /worth prices. Voyager owns
 * player-facing /worth and /sell against its own worth.yml, and its API isn't public. To avoid
 * fighting Voyager, HavocEco ships its OWN small tools-worth.yml with amethyst auto-sell rates.
 * If a material has no entry here, an auto-sell tool simply drops the block instead.
 *
 * You can tune tools-worth.yml so amethyst tools pay slightly less than /sell would — that keeps
 * them a convenience upgrade rather than a way to bypass Voyager's economy tuning.
 */
public class ToolsWorthManager {

    private final HavocEco plugin;
    private final Map<Material, Double> prices = new HashMap<>();

    public ToolsWorthManager(HavocEco plugin) {
        this.plugin = plugin;
    }

    public void load() {
        prices.clear();
        File file = new File(plugin.getDataFolder(), "tools-worth.yml");
        if (!file.exists()) plugin.saveResource("tools-worth.yml", false);
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection sec = cfg.getConfigurationSection("PRICES");
        if (sec == null) {
            plugin.getLogger().warning("tools-worth.yml has no PRICES section.");
            return;
        }
        for (String key : sec.getKeys(false)) {
            Material mat = Material.matchMaterial(key);
            if (mat != null) prices.put(mat, sec.getDouble(key));
        }
        plugin.getLogger().info("Loaded " + prices.size() + " amethyst tool prices.");
    }

    /** Returns per-block payout, or 0 if the block isn't auto-sellable. */
    public double priceOf(Material material) {
        return prices.getOrDefault(material, 0.0);
    }
}
