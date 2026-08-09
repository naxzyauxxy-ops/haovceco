package club.havocsmp.eco.tools;

import club.havocsmp.eco.HavocEco;
import club.havocsmp.eco.util.Numbers;
import club.havocsmp.eco.util.Text;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

/**
 * When a player breaks a block with an amethyst tool, break the surrounding cube
 * (NxNxN centered on the block). Depending on the tool, mined blocks are either
 * auto-sold to money or dropped normally.
 *
 * A re-entrancy guard prevents the cube-break from recursively triggering itself.
 */
public class AmethystBreakListener implements Listener {

    private final HavocEco plugin;
    private final AmethystToolManager tools;
    private boolean processing = false;

    // Blocks we never want the area tool to break (bedrock, containers, spawners, etc).
    private static final Set<Material> BLACKLIST = new HashSet<>();
    static {
        BLACKLIST.add(Material.BEDROCK);
        BLACKLIST.add(Material.SPAWNER);
        BLACKLIST.add(Material.CHEST);
        BLACKLIST.add(Material.TRAPPED_CHEST);
        BLACKLIST.add(Material.ENDER_CHEST);
        BLACKLIST.add(Material.BARREL);
        BLACKLIST.add(Material.SHULKER_BOX);
        BLACKLIST.add(Material.OBSIDIAN);
    }

    public AmethystBreakListener(HavocEco plugin) {
        this.plugin = plugin;
        this.tools = plugin.amethystTools();
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (processing) return;

        Player player = e.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        String toolId = tools.toolIdOf(held);
        if (toolId == null) return;

        AmethystToolManager.ToolDef def = tools.get(toolId);
        if (def == null) return;

        int radius = (def.cubeSize - 1) / 2;  // 3->1, 6->2 (even sizes bias -1/+2 below), 9->4, 12->5
        Block center = e.getBlock();

        double earned = 0;
        int broken = 0;

        processing = true;
        try {
            // For even cube sizes, offset the range so total spans cubeSize blocks.
            int lower = -radius;
            int upper = def.cubeSize % 2 == 0 ? radius + 1 : radius;

            for (int dx = lower; dx <= upper; dx++) {
                for (int dy = lower; dy <= upper; dy++) {
                    for (int dz = lower; dz <= upper; dz++) {
                        Block b = center.getWorld().getBlockAt(
                                center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                        if (b.equals(center)) continue;
                        if (b.getType() == Material.AIR) continue;
                        if (BLACKLIST.contains(b.getType())) continue;
                        if (!matchesToolType(def, b.getType())) continue;

                        if (def.autoSell) {
                            double price = plugin.toolsWorth().priceOf(b.getType());
                            if (price > 0) earned += price;
                            b.setType(Material.AIR);
                        } else {
                            b.breakNaturally(held);
                        }
                        broken++;
                    }
                }
            }

            // Handle the center block per the tool's mode.
            if (def.autoSell) {
                double price = plugin.toolsWorth().priceOf(center.getType());
                if (price > 0) earned += price;
                e.setDropItems(false);
            }
        } finally {
            processing = false;
        }

        if (def.autoSell && earned > 0) {
            if (!plugin.economy().isReady()) {
                // Vault/Voyager not available — refund by putting the block back to be safe.
                player.sendActionBar(Text.color("&cAuto-sell disabled (economy unavailable)."));
                return;
            }
            plugin.economy().deposit(player.getUniqueId(), earned);
            player.sendActionBar(Text.color("&aSold " + broken + " blocks for &#f40d0d$" + Numbers.comma(earned)));
        }
    }

    /** Restrict what each tool type breaks (pickaxe -> ores/stone, shovel -> dirt-likes, etc). */
    private boolean matchesToolType(AmethystToolManager.ToolDef def, Material mat) {
        String name = mat.name();
        return switch (def.toolType) {
            case "SHOVEL" -> name.contains("DIRT") || name.contains("SAND") || name.contains("GRAVEL")
                    || name.contains("CLAY") || name.contains("SOUL") || name.equals("GRASS_BLOCK")
                    || name.contains("MUD") || name.contains("SNOW");
            case "AXE", "SELL_AXE" -> name.contains("LOG") || name.contains("WOOD")
                    || name.contains("PLANKS") || name.contains("STEM") || name.contains("HYPHAE");
            // PICKAXE and default: mineable stone/ore family
            default -> name.contains("ORE") || name.contains("STONE") || name.contains("DEEPSLATE")
                    || name.contains("GRANITE") || name.contains("DIORITE") || name.contains("ANDESITE")
                    || name.contains("NETHERRACK") || name.contains("BASALT") || name.contains("TUFF")
                    || name.contains("CALCITE") || name.contains("AMETHYST") || name.contains("BLACKSTONE");
        };
    }
}
