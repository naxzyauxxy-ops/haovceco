package club.havocsmp.eco.menu;

import club.havocsmp.eco.HavocEco;
import club.havocsmp.eco.tools.AmethystToolManager;
import club.havocsmp.eco.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * /rubyshop — buy amethyst tools (and any other RUBY-SHOP entries you add) with rubies.
 * Tools are pulled from tools.yml so the shop stays in sync with what tools exist.
 */
public class RubyShopMenu extends PagedMenu {

    public RubyShopMenu(HavocEco plugin, Player player) {
        super(plugin, player);
    }

    @Override
    protected String title() {
        return "&#f40d0dRuby Shop";
    }

    @Override
    protected List<MenuItem> buildItems() {
        List<MenuItem> items = new ArrayList<>();
        for (AmethystToolManager.ToolDef def : plugin.amethystTools().all().values()) {
            items.add(new MenuItem() {
                @Override
                public org.bukkit.inventory.ItemStack icon() {
                    List<String> l = new ArrayList<>();
                    l.add("&7Size: &f" + def.cubeSize + "x" + def.cubeSize + "x" + def.cubeSize);
                    l.add("&7Mode: &f" + (def.autoSell ? "Auto-sell" : "Drop blocks"));
                    l.add("");
                    l.add("&7Price: &#f40d0d" + def.rubyPrice + " " + plugin.settings().rubyName());
                    l.add("&eClick to buy");
                    return RubyShopMenu.this.icon(def.material, def.displayName, l);
                }

                @Override
                public void onClick(Player p, InventoryClickEvent e) {
                    long price = def.rubyPrice;
                    if (!plugin.rubies().has(p.getUniqueId(), price)) {
                        p.sendMessage(Text.color("&#f40d0dYou do not have enough " + plugin.settings().rubyName() + "."));
                        return;
                    }
                    if (p.getInventory().firstEmpty() == -1) {
                        p.sendMessage(Text.color("&#f40d0dYour inventory is full!"));
                        return;
                    }
                    plugin.rubies().take(p.getUniqueId(), price);
                    p.getInventory().addItem(plugin.amethystTools().build(def.id));
                    p.sendMessage(Text.color("&aPurchased &#f40d0d" + Text.color(def.displayName)
                            + " &afor &#f40d0d" + price + " " + plugin.settings().rubyName()));
                }
            });
        }
        return items;
    }
}
