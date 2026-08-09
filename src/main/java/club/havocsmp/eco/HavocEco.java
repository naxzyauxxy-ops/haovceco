package club.havocsmp.eco;

import club.havocsmp.eco.casino.CasinoManager;
import club.havocsmp.eco.commands.*;
import club.havocsmp.eco.config.Settings;
import club.havocsmp.eco.economy.EconomyHook;
import club.havocsmp.eco.economy.InvestManager;
import club.havocsmp.eco.economy.ToolsWorthManager;
import club.havocsmp.eco.listeners.CombatPearlListener;
import club.havocsmp.eco.listeners.PlayerDataListener;
import club.havocsmp.eco.listeners.SpawnEffectsListener;
import club.havocsmp.eco.listeners.TotemListener;
import club.havocsmp.eco.menu.MenuListener;
import club.havocsmp.eco.rubies.RubyManager;
import club.havocsmp.eco.scoreboard.BossBarManager;
import club.havocsmp.eco.storage.Database;
import club.havocsmp.eco.tools.AmethystBreakListener;
import club.havocsmp.eco.tools.AmethystToolManager;
import club.havocsmp.eco.util.CoordHider;
import club.havocsmp.eco.util.Cooldowns;
import club.havocsmp.eco.util.Text;
import club.havocsmp.eco.wager.WagerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class HavocEco extends JavaPlugin {

    private Settings settings;
    private Database database;
    private EconomyHook economy;
    private RubyManager rubies;
    private InvestManager invest;
    private CasinoManager casino;
    private BossBarManager bossBar;
    private AmethystToolManager amethystTools;
    private ToolsWorthManager toolsWorth;
    private WagerManager wagers;
    private CoordHider coordHider;
    private Cooldowns cooldowns;

    // Runtime flag toggled by the scheduled double-ruby event.
    private volatile boolean rubyEventActive = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.settings = new Settings(this);
        this.database = new Database(this);
        this.database.load();

        this.economy = new EconomyHook(this);
        this.economy.setup();
        this.rubies = new RubyManager(this);
        this.invest = new InvestManager(this);
        this.casino = new CasinoManager(this);
        this.bossBar = new BossBarManager(this);
        this.amethystTools = new AmethystToolManager(this);
        this.amethystTools.load();
        this.toolsWorth = new ToolsWorthManager(this);
        this.toolsWorth.load();
        this.wagers = new WagerManager(this);
        this.coordHider = new CoordHider(this);
        this.cooldowns = new Cooldowns();

        registerCommands();
        registerListeners();
        startTasks();
        enableFeatures();

        getLogger().info("HavocEco enabled.");
    }

    private void enableFeatures() {
        if (settings.bossbarEnabled()) bossBar.start();
        scheduleDoubleRubyEvents();
    }

    @Override
    public void onDisable() {
        if (database != null) database.saveIfDirty();
        if (bossBar != null) bossBar.stop();
    }

    private void registerCommands() {
        bind("rubypay", new RubyPayCommand(this));
        bind("invest", new InvestCommand(this));
        bind("slots", new SlotsCommand(this));
        bind("jackpot", new JackpotCommand(this));
        bind("rubyshop", new RubyShopCommand(this));
        bind("live", new LiveCommand(this));
        bind("coords", new CoordsCommand(this));
        bind("coinflip", new CoinflipCommand(this));
        bind("havoceco", new AdminCommand(this));
    }

    private void bind(String name, org.bukkit.command.CommandExecutor exec) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(exec);
            if (exec instanceof org.bukkit.command.TabCompleter tc) cmd.setTabCompleter(tc);
        } else {
            getLogger().warning("Command '" + name + "' missing from plugin.yml");
        }
    }

    private void registerListeners() {
        var pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerDataListener(this), this);
        pm.registerEvents(new CombatPearlListener(this), this);
        pm.registerEvents(new TotemListener(this), this);
        pm.registerEvents(new SpawnEffectsListener(this), this);
        pm.registerEvents(new AmethystBreakListener(this), this);
        pm.registerEvents(new MenuListener(this), this);
    }

    private void startTasks() {
        // Async periodic save.
        long ticks = settings.saveIntervalSeconds() * 20L;
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> database.saveIfDirty(), ticks, ticks);

        // Invest auto-claim check every 30s (on main thread; economy touches player data).
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (var p : Bukkit.getOnlinePlayers()) {
                double payout = invest.tryClaim(p.getUniqueId());
                if (payout > 0) {
                    p.sendMessage(Text.color("&aYour investment matured! You received "
                            + club.havocsmp.eco.util.Numbers.comma(payout) + "."));
                }
            }
        }, 600L, 600L);
    }

    private void scheduleDoubleRubyEvents() {
        // Simple example: read a list of "HH:mm" times and durations from config and
        // flip the runtime flag. For brevity this reads a single interval-based schedule.
        int everyHours = getConfig().getInt("RUBIES.DOUBLE-EVENT-SCHEDULE.EVERY-HOURS", 0);
        int durationMinutes = getConfig().getInt("RUBIES.DOUBLE-EVENT-SCHEDULE.DURATION-MINUTES", 30);
        if (everyHours <= 0) return;
        long period = 20L * 60 * 60 * everyHours;
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            rubyEventActive = true;
            Bukkit.broadcastMessage(Text.color("&#f40d0d&lDOUBLE RUBIES &fis now ACTIVE for " + durationMinutes + " minutes!"));
            Bukkit.getScheduler().runTaskLater(this, () -> {
                rubyEventActive = false;
                Bukkit.broadcastMessage(Text.color("&#f40d0d&lDOUBLE RUBIES &fhas ended."));
            }, 20L * 60 * durationMinutes);
        }, period, period);
    }

    // ---- accessors ----
    public Settings settings() { return settings; }
    public Database database() { return database; }
    public EconomyHook economy() { return economy; }
    public RubyManager rubies() { return rubies; }
    public InvestManager invest() { return invest; }
    public CasinoManager casino() { return casino; }
    public AmethystToolManager amethystTools() { return amethystTools; }
    public ToolsWorthManager toolsWorth() { return toolsWorth; }
    public WagerManager wagers() { return wagers; }
    public CoordHider coordHider() { return coordHider; }
    public Cooldowns cooldowns() { return cooldowns; }
    public boolean rubyEventActive() { return rubyEventActive; }
    public void setRubyEventActive(boolean v) { this.rubyEventActive = v; }
}
