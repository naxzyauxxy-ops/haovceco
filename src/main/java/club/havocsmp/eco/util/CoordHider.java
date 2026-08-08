package club.havocsmp.eco.util;

import club.havocsmp.eco.HavocEco;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which players have chosen to hide their coordinates.
 *
 * NOTE ON SCOPE: Minecraft's F3/coordinate HUD is client-side. A plugin can't truly blank
 * the vanilla coordinate readout without a client mod or reduced-debug-info. What this manager
 * does is (a) persist the player's preference and (b) enable reduced debug info for them, which
 * hides coordinates in the F3 screen on Java clients. Bedrock players (via Geyser) have their
 * coordinate display controlled by the "Show Coordinates" world/game rule, so for Bedrock this
 * flag is exposed for your Geyser/floodgate setup or an add-on to consume.
 *
 * The preference is stored in database.yml so it survives restarts.
 */
public class CoordHider {

    private final HavocEco plugin;
    private final Set<UUID> hidden = ConcurrentHashMap.newKeySet();

    public CoordHider(HavocEco plugin) {
        this.plugin = plugin;
    }

    public boolean isHidden(UUID uuid) {
        return hidden.contains(uuid);
    }

    /** Toggle; returns the new hidden state. */
    public boolean toggle(UUID uuid) {
        boolean nowHidden;
        if (hidden.contains(uuid)) {
            hidden.remove(uuid);
            nowHidden = false;
        } else {
            hidden.add(uuid);
            nowHidden = true;
        }
        plugin.database().raw().set("players." + uuid + ".hideCoords", nowHidden);
        plugin.database().markDirty();
        applyToPlayer(uuid, nowHidden);
        return nowHidden;
    }

    public void loadFor(UUID uuid) {
        boolean h = plugin.database().raw().getBoolean("players." + uuid + ".hideCoords", false);
        if (h) hidden.add(uuid);
        applyToPlayer(uuid, h);
    }

    private void applyToPlayer(UUID uuid, boolean hide) {
        var player = plugin.getServer().getPlayer(uuid);
        if (player == null) return;
        // Paper exposes a per-player reduced-debug-screen flag. When enabled, the F3 HUD
        // (including coordinates) is hidden on Java clients. Wrapped in a try/catch so the
        // plugin still loads on server forks that lack the method.
        try {
            player.getClass().getMethod("setReducedDebugScreenInfo", boolean.class)
                    .invoke(player, hide);
        } catch (Throwable ignored) {
            // Method not present on this platform; the preference is still stored in
            // database.yml for Geyser/Bedrock or an add-on to consume.
        }
    }
}
