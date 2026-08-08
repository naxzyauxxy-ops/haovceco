package club.havocsmp.eco.listeners;

import club.havocsmp.eco.HavocEco;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * Two behaviors bundled:
 *  1. Keep Speed, Strength and Night Vision after a totem pop (totem normally clears effects).
 *  2. Keep ender pearls in the inventory on death (they aren't dropped).
 */
public class TotemListener implements Listener {

    private final HavocEco plugin;

    // The effect types you specifically wanted retained after a totem pop.
    private static final List<PotionEffectType> KEEP = List.of(
            PotionEffectType.SPEED,
            PotionEffectType.STRENGTH,
            PotionEffectType.NIGHT_VISION
    );

    public TotemListener(HavocEco plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResurrect(EntityResurrectEvent e) {
        if (!plugin.settings().keepEffectsOnTotem()) return;
        if (!(e.getEntity() instanceof Player player)) return;

        // Snapshot the relevant "max" effects the player currently has.
        List<PotionEffect> saved = new ArrayList<>();
        for (PotionEffectType type : KEEP) {
            PotionEffect eff = player.getPotionEffect(type);
            if (eff != null) saved.add(eff);
        }
        if (saved.isEmpty()) return;

        // The totem clears effects on the same tick; re-apply ours the next tick.
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (PotionEffect eff : saved) {
                player.addPotionEffect(new PotionEffect(
                        eff.getType(), eff.getDuration(), eff.getAmplifier(),
                        eff.isAmbient(), eff.hasParticles(), eff.hasIcon()));
            }
        });
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (!plugin.settings().pearlsKeepOnDeath()) return;

        List<ItemStack> keptPearls = new ArrayList<>();
        e.getDrops().removeIf(drop -> {
            if (drop != null && drop.getType().name().equals("ENDER_PEARL")) {
                keptPearls.add(drop.clone());
                return true; // remove from drops
            }
            return false;
        });

        if (keptPearls.isEmpty()) return;
        Player player = e.getEntity();
        // Give them back after respawn.
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (ItemStack pearl : keptPearls) {
                player.getInventory().addItem(pearl);
            }
        });
    }
}
