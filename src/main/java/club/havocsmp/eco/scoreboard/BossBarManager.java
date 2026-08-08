package club.havocsmp.eco.scoreboard;

import club.havocsmp.eco.HavocEco;
import club.havocsmp.eco.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A single cycling boss bar shown to all players (e.g. "Havocsmp.club | /store | /discord").
 * Messages and cycle speed come from config. Players can toggle it off; toggled-off players
 * are tracked in a set (persist this to database.yml if you want it to survive restarts).
 */
public class BossBarManager {

    private final HavocEco plugin;
    private BossBar bar;
    private BukkitTask task;
    private int index = 0;
    private final Set<UUID> hidden = new HashSet<>();

    public BossBarManager(HavocEco plugin) {
        this.plugin = plugin;
    }

    public void start() {
        var cfg = plugin.getConfig();
        BarColor color = BarColor.valueOf(cfg.getString("BOSSBAR.COLOR", "RED"));
        BarStyle style = BarStyle.valueOf(cfg.getString("BOSSBAR.STYLE", "SOLID"));
        bar = Bukkit.createBossBar("", color, style);
        bar.setProgress(1.0);

        List<String> messages = cfg.getStringList("BOSSBAR.MESSAGES");
        if (messages.isEmpty()) messages = List.of("&#f40d0dHavocsmp.club", "&f/store", "&f/discord");

        List<String> finalMessages = messages;
        long ticks = plugin.settings().bossbarCycleSeconds() * 20L;
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            String msg = finalMessages.get(index % finalMessages.size());
            index++;
            bar.setTitle(Text.color(msg));
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (hidden.contains(p.getUniqueId())) {
                    bar.removePlayer(p);
                } else {
                    bar.addPlayer(p);
                }
            }
        }, 0L, ticks);
    }

    public void toggle(Player p) {
        UUID id = p.getUniqueId();
        if (hidden.contains(id)) {
            hidden.remove(id);
            if (bar != null) bar.addPlayer(p);
        } else {
            hidden.add(id);
            if (bar != null) bar.removePlayer(p);
        }
    }

    public boolean isHidden(UUID id) {
        return hidden.contains(id);
    }

    public void stop() {
        if (task != null) task.cancel();
        if (bar != null) bar.removeAll();
    }
}
