package club.havocsmp.eco.menu;

import club.havocsmp.eco.HavocEco;
import club.havocsmp.eco.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for paged GUIs. Subclasses provide the list of "content" items; this class
 * handles chunking them across pages and drawing Next/Back navigation, giving you the
 * multi-page support for single GUI categories you asked for.
 */
public abstract class PagedMenu {

    protected final HavocEco plugin;
    protected final Player player;
    protected int page = 0;

    // Content slots (a 54-slot inventory with a bottom nav row => 45 content slots).
    protected static final int SIZE = 54;
    protected static final int CONTENT_SLOTS = 45;
    protected static final int NEXT_SLOT = 53;
    protected static final int BACK_SLOT = 45;

    private List<MenuItem> cached;

    protected PagedMenu(HavocEco plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    /** Provide every item that should appear across all pages. */
    protected abstract List<MenuItem> buildItems();

    /** The inventory title (may include page number). */
    protected abstract String title();

    public void open() {
        if (cached == null) cached = buildItems();
        int totalPages = Math.max(1, (int) Math.ceil(cached.size() / (double) CONTENT_SLOTS));
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        Holder holder = new Holder(this);
        Inventory inv = Bukkit.createInventory(holder, SIZE,
                Text.color(title() + " &7(" + (page + 1) + "/" + totalPages + ")"));
        holder.setInventory(inv);

        int start = page * CONTENT_SLOTS;
        int end = Math.min(start + CONTENT_SLOTS, cached.size());
        for (int i = start; i < end; i++) {
            inv.setItem(i - start, cached.get(i).icon());
        }

        if (page > 0) inv.setItem(BACK_SLOT, navItem(Material.ARROW, "&aBack"));
        if (page < totalPages - 1) inv.setItem(NEXT_SLOT, navItem(Material.ARROW, "&aNext"));

        player.openInventory(inv);
    }

    /** Called by the global listener when a content slot is clicked. */
    public void handleClick(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot == BACK_SLOT && page > 0) { page--; open(); return; }
        if (slot == NEXT_SLOT) { page++; open(); return; }
        if (slot < 0 || slot >= CONTENT_SLOTS) return;

        int index = page * CONTENT_SLOTS + slot;
        if (cached != null && index < cached.size()) {
            cached.get(index).onClick(player, e);
        }
    }

    protected ItemStack navItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Text.color(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    /** A menu entry: an icon plus a click action. */
    public interface MenuItem {
        ItemStack icon();
        void onClick(Player player, InventoryClickEvent e);
    }

    /** Simple InventoryHolder so the listener can find the owning menu. */
    public static class Holder implements org.bukkit.inventory.InventoryHolder {
        private final PagedMenu menu;
        private Inventory inventory;
        public Holder(PagedMenu menu) { this.menu = menu; }
        public PagedMenu menu() { return menu; }
        public void setInventory(Inventory inv) { this.inventory = inv; }
        @Override public Inventory getInventory() { return inventory; }
    }

    protected ItemStack icon(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Text.color(name));
            if (lore != null) meta.setLore(Text.color(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    protected static List<String> lore(String... lines) {
        return new ArrayList<>(List.of(lines));
    }
}
