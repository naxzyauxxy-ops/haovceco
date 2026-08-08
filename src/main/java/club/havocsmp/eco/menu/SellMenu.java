package club.havocsmp.eco.menu;

import club.havocsmp.eco.HavocEco;
import club.havocsmp.eco.util.Numbers;
import club.havocsmp.eco.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Bigger Sell menu. Lists every sellable material (from worth.yml) across as many pages as
 * needed. Clicking an item sells all of that material in the player's inventory.
 * A "Sell All" style action lives on the /sell command; this menu is the browsable version.
 */
public class SellMenu extends PagedMenu {

    public SellMenu(HavocEco plugin, Player player) {
        super(plugin, player);
    }

    @Override
    protected String title() {
        return "&#f40d0dSell Menu";
    }

    @Override
    protected List<MenuItem> buildItems() {
        List<MenuItem> items = new ArrayList<>();
        for (Map.Entry<Material, Double> entry : plugin.worth().all().entrySet()) {
            Material mat = entry.getKey();
            double price = entry.getValue();
            items.add(new MenuItem() {
                @Override
                public org.bukkit.inventory.ItemStack icon() {
                    return SellMenu.this.icon(mat, "&f" + pretty(mat),
                            lore("&7Price each: &#f40d0d$" + Numbers.comma(price),
                                 "",
                                 "&eClick to sell all of this item"));
                }

                @Override
                public void onClick(Player p, InventoryClickEvent e) {
                    double earned = plugin.sell().sellMaterial(p, mat);
                    if (earned <= 0) {
                        p.sendMessage(Text.color("&#f40d0dYou have none of that to sell."));
                    } else {
                        p.sendMessage(Text.color("&aSold for &#f40d0d$" + Numbers.comma(earned)));
                    }
                }
            });
        }
        return items;
    }

    private static String pretty(Material mat) {
        String s = mat.name().toLowerCase().replace('_', ' ');
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
