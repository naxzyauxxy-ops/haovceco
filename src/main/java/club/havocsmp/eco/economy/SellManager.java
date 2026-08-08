package club.havocsmp.eco.economy;

import club.havocsmp.eco.HavocEco;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Central place for turning items into money, used by /sell, the sell menu, and amethyst auto-sell. */
public class SellManager {

    private final HavocEco plugin;

    public SellManager(HavocEco plugin) {
        this.plugin = plugin;
    }

    /** Value of a single stack (price per item * amount), or 0 if unsellable. */
    public double valueOf(ItemStack item) {
        if (item == null) return 0;
        double each = plugin.worth().priceOf(item.getType());
        if (each < 0) return 0;
        return each * item.getAmount();
    }

    /**
     * Sell everything sellable in the player's inventory. Returns total earned.
     * Removes the sold items and deposits the money.
     */
    public double sellInventory(Player player) {
        double total = 0;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() == Material.AIR) continue;
            double value = valueOf(item);
            if (value <= 0) continue;
            total += value;
            player.getInventory().setItem(i, null);
        }
        if (total > 0) {
            plugin.economy().deposit(player.getUniqueId(), total);
        }
        return total;
    }

    /** Sell a specific material only (used by sell-menu category buttons). */
    public double sellMaterial(Player player, Material material) {
        double total = 0;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() != material) continue;
            double value = valueOf(item);
            if (value <= 0) continue;
            total += value;
            player.getInventory().setItem(i, null);
        }
        if (total > 0) plugin.economy().deposit(player.getUniqueId(), total);
        return total;
    }
}
