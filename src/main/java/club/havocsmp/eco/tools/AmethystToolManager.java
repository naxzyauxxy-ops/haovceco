package club.havocsmp.eco.tools;

import club.havocsmp.eco.HavocEco;
import club.havocsmp.eco.util.Text;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Amethyst area-mining tools. Each tool has a tier that defines its cube size
 * (3x3x3, 6x6x6, 9x9x9, 12x12x12). Tools are tagged with persistent data so we can
 * identify them in the world regardless of rename/lore.
 *
 * Per-tool config controls whether mined blocks auto-sell to money or drop normally.
 * Defined in tools.yml, copied out on first run.
 */
public class AmethystToolManager {

    private final HavocEco plugin;
    public final NamespacedKey toolKey;      // marks an item as a HavocEco amethyst tool
    public final NamespacedKey tierKey;      // stores which tool id it is

    private final Map<String, ToolDef> tools = new LinkedHashMap<>();

    public AmethystToolManager(HavocEco plugin) {
        this.plugin = plugin;
        this.toolKey = new NamespacedKey(plugin, "amethyst_tool");
        this.tierKey = new NamespacedKey(plugin, "amethyst_tool_id");
    }

    public void load() {
        tools.clear();
        java.io.File file = new java.io.File(plugin.getDataFolder(), "tools.yml");
        if (!file.exists()) plugin.saveResource("tools.yml", false);
        var cfg = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);

        ConfigurationSection sec = cfg.getConfigurationSection("TOOLS");
        if (sec == null) {
            plugin.getLogger().warning("tools.yml has no TOOLS section.");
            return;
        }
        for (String id : sec.getKeys(false)) {
            ConfigurationSection t = sec.getConfigurationSection(id);
            if (t == null) continue;
            ToolDef def = new ToolDef();
            def.id = id;
            def.displayName = t.getString("DISPLAY-NAME", "&dAmethyst Tool");
            def.material = Material.matchMaterial(t.getString("MATERIAL", "DIAMOND_PICKAXE"));
            if (def.material == null) def.material = Material.DIAMOND_PICKAXE;
            def.cubeSize = t.getInt("CUBE-SIZE", 3);       // 3,6,9,12
            def.autoSell = t.getBoolean("AUTO-SELL", false);
            def.rubyPrice = t.getLong("RUBY-PRICE", 500);
            def.lore = t.getStringList("LORE");
            def.toolType = t.getString("TYPE", "PICKAXE").toUpperCase(); // PICKAXE/SHOVEL/AXE/SELL_AXE
            tools.put(id, def);
        }
        plugin.getLogger().info("Loaded " + tools.size() + " amethyst tools.");
    }

    public ToolDef get(String id) {
        return tools.get(id);
    }

    public Map<String, ToolDef> all() {
        return tools;
    }

    /** Build the actual ItemStack for a tool id, tagged with persistent data. */
    public ItemStack build(String id) {
        ToolDef def = tools.get(id);
        if (def == null) return null;
        ItemStack item = new ItemStack(def.material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Text.color(def.displayName));
            List<String> lore = new ArrayList<>(Text.color(def.lore));
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(toolKey, PersistentDataType.BYTE, (byte) 1);
            meta.getPersistentDataContainer().set(tierKey, PersistentDataType.STRING, id);
            item.setItemMeta(meta);
        }
        return item;
    }

    /** Returns the tool id if this item is one of our amethyst tools, else null. */
    public String toolIdOf(ItemStack item) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        var pdc = meta.getPersistentDataContainer();
        if (!pdc.has(toolKey, PersistentDataType.BYTE)) return null;
        return pdc.get(tierKey, PersistentDataType.STRING);
    }

    public static class ToolDef {
        public String id;
        public String displayName;
        public Material material;
        public int cubeSize;       // N in NxNxN
        public boolean autoSell;
        public long rubyPrice;
        public List<String> lore;
        public String toolType;    // PICKAXE, SHOVEL, AXE, SELL_AXE
    }
}
