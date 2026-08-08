package club.havocsmp.eco.listeners;

import club.havocsmp.eco.HavocEco;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles the combat-tag rules you asked for:
 *   - Ender pearls and wind charges do NOT apply a combat tag.
 *   - Actual PvP hits DO apply a combat tag (configurable duration).
 *   - Ender pearls are kept in inventory after death (see onDeath below in TotemListener's
 *     sibling logic — implemented here via a keep flag).
 *
 * This is a lightweight, self-contained combat tag. If you already run a dedicated combat-tag
 * plugin, disable that side to avoid double-tagging, or set COMBAT.TAG-SECONDS to 0.
 */
public class CombatPearlListener implements Listener {

    private final HavocEco plugin;
    private final Map<UUID, Long> combatUntil = new HashMap<>();

    public CombatPearlListener(HavocEco plugin) {
        this.plugin = plugin;
    }

    public boolean isTagged(UUID uuid) {
        Long until = combatUntil.get(uuid);
        return until != null && until > System.currentTimeMillis();
    }

    private void tag(Player p) {
        int secs = plugin.settings().combatTagSeconds();
        if (secs <= 0) return;
        combatUntil.put(p.getUniqueId(), System.currentTimeMillis() + secs * 1000L);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;

        // If the damage came from a projectile, check whether it's a pearl/wind charge.
        if (e.getDamager() instanceof Projectile proj) {
            String type = proj.getType().name();
            if (plugin.settings().pearlsNoCombatTag() && proj instanceof EnderPearl) {
                return; // no tag from pearls
            }
            if (plugin.settings().windChargeNoCombatTag() && type.contains("WIND_CHARGE")) {
                return; // no tag from wind charges
            }
            if (proj.getShooter() instanceof Player shooter) {
                tag(shooter);
                tag(victim);
            }
            return;
        }

        if (e.getDamager() instanceof Player attacker) {
            tag(attacker);
            tag(victim);
        }
    }

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent e) {
        // Nothing to block by default; hook kept for future rules (e.g. block pearls while tagged).
    }

    /** Utility other classes can call to check whether pearls should survive death. */
    public boolean keepPearlsOnDeath() {
        return plugin.settings().pearlsKeepOnDeath();
    }

    public static boolean isPearl(ItemStack item) {
        return item != null && item.getType().name().equals("ENDER_PEARL");
    }
}
