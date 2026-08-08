package club.havocsmp.eco.menu;

import club.havocsmp.eco.HavocEco;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/** Routes clicks in any PagedMenu back to that menu instance. */
public class MenuListener implements Listener {

    public MenuListener(HavocEco plugin) {}

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        var holder = e.getInventory().getHolder();
        if (holder instanceof PagedMenu.Holder h) {
            h.menu().handleClick(e);
        }
    }
}
