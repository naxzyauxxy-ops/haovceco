package club.havocsmp.eco.listeners;

import club.havocsmp.eco.HavocEco;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerDataListener implements Listener {

    private final HavocEco plugin;

    public PlayerDataListener(HavocEco plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        plugin.database().ensurePlayer(e.getPlayer().getUniqueId(), e.getPlayer().getName());
        plugin.coordHider().loadFor(e.getPlayer().getUniqueId());
    }
}
