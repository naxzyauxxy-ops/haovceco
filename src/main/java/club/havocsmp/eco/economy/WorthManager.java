package club.havocsmp.eco.economy;

import club.havocsmp.eco.HavocEco;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads sell prices from worth.yml. Your file nests prices under TYPE.<CATEGORY>.<MATERIAL>,
 * so we flatten every category into one Material->price map. A material listed in multiple
 * categories keeps the last value loaded (shouldn't happen in a clean file).
 *
 * Falls back to SETTINGS.WORTH-DEFAULT-VALUE from config.yml for unlisted-but-sellable items
 * only if you enable that behavior; by default unlisted items are NOT sellable.
 */
public class WorthManager {

    private final HavocEco plugin;
    private final Map<Material, Double> prices = new HashMap<>();

    public WorthManager(HavocEco plugin) {
        this.plugin = plugin;
    }

    public void load() {
        prices.clear();
        File file = new File(plugin.getDataFolder(), "worth.yml");
        if (!file.exists()) {
            // Ship your worth.yml in resources so it's copied out; if missing, warn.
            plugin.saveResource("worth.yml", false);
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection type = cfg.getConfigurationSection("TYPE");
        if (type != null) {
            for (String category : type.getKeys(false)) {
                ConfigurationSection catSec = type.getConfigurationSection(category);
                if (catSec == null) continue;
                for (String matName : catSec.getKeys(false)) {
                    register(matName, catSec.getDouble(matName));
                }
            }
        }

        // Optional flat BLOCK-ITEMS section support.
        ConfigurationSection blockItems = cfg.getConfigurationSection("BLOCK-ITEMS");
        if (blockItems != null) {
            for (String matName : blockItems.getKeys(false)) {
                register(matName, blockItems.getDouble(matName));
            }
        }

        plugin.getLogger().info("Loaded " + prices.size() + " worth entries.");
    }

    private void register(String matName, double price) {
        Material mat = Material.matchMaterial(matName);
        if (mat != null) prices.put(mat, price);
    }

    /** Price for one item, or -1 if the item cannot be sold. */
    public double priceOf(Material material) {
        return prices.getOrDefault(material, -1.0);
    }

    public boolean isSellable(Material material) {
        return prices.containsKey(material);
    }

    public Map<Material, Double> all() {
        return prices;
    }
}
