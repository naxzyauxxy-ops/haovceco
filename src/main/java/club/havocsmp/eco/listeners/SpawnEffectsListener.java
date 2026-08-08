package club.havocsmp.eco.listeners;

import club.havocsmp.eco.HavocEco;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Applies a jump boost while players are inside the configured spawn region.
 * The region is a simple radius around a center point defined in config.yml under
 * SPAWN.JUMP-BOOST. Refreshes the effect as players move so it never expires while in-zone,
 * and strips it when they leave.
 */
public class SpawnEffectsListener implements Listener {

    private final HavocEco plugin;

    public SpawnEffectsListener(HavocEco plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!plugin.settings().spawnJumpBoost()) return;
        if (e.getTo() == null) return;
        // Only run when the block actually changes to keep it cheap.
        if (e.getFrom().getBlockX() == e.getTo().getBlockX()
                && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

        Player p = e.getPlayer();
        var cfg = plugin.getConfig();
        String world = cfg.getString("SPAWN.JUMP-BOOST.WORLD", "world");
        double cx = cfg.getDouble("SPAWN.JUMP-BOOST.CENTER-X", 0);
        double cz = cfg.getDouble("SPAWN.JUMP-BOOST.CENTER-Z", 0);
        double radius = cfg.getDouble("SPAWN.JUMP-BOOST.RADIUS", 50);

        boolean inZone = p.getWorld().getName().equalsIgnoreCase(world)
                && p.getLocation().distanceSquared(
                        new org.bukkit.Location(p.getWorld(), cx, p.getLocation().getY(), cz)) <= radius * radius;

        if (inZone) {
            int level = Math.max(0, plugin.settings().spawnJumpBoostLevel() - 1);
            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, level, true, false, false));
        } else if (p.hasPotionEffect(PotionEffectType.JUMP_BOOST)) {
            // Only strip the one we manage; a naive removeIf is fine for most servers.
            p.removePotionEffect(PotionEffectType.JUMP_BOOST);
        }
    }
}
