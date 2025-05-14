package com.crashcringle.barterplus;

import com.crashcringle.barterplus.barterkings.BarterKings;
import com.crashcringle.barterplus.commands.ConstructTabCompleter;
import com.crashcringle.barterplus.data.Database;
import com.crashcringle.barterplus.data.LegacyData;
import com.crashcringle.barterplus.barterkings.npc.BarterTrait;
import com.crashcringle.barterplus.commands.CommandTrade;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.ipvp.canvas.MenuFunctionListener;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import org.bukkit.configuration.Configuration;

public final class BarterPlus extends JavaPlugin {

    private static BarterPlus instance;

    public static BarterPlus inst() { return instance; }
    public enum Scenario {
        COOPERATIVE, COMPETITIVE, BASELINE
    }

    public int globalBufferTime = 1;

    // Get the time
    public long lastTime = (System.currentTimeMillis());

    private Connection connection;
    private BarterKings barterKings;
    public String model = "gpt-4o";
    public String gptModel = "gpt-4o";
    public String geminiModel = "gemini-1.5-pro";
    public Scenario currentScenario = Scenario.BASELINE;
    public float temperature = 0.7f;
    public float topP = 0.8f;

    public String geminiKey = "";

    public String gptKey = "";
    public int[] maxRuns; // Number of times to check if the game is in progress before starting a new game

    public int numberOfGeminiAgents = 0;
    // Use time for seed
    public int seed = (int) System.currentTimeMillis();
    final String SQL_INSERT_TRADE_REQUEST ="INSERT INTO trade_request(requester, requested, status, time_created, time_finished, game) VALUES (?, ?, ?, ?, ?, ?)";
    final String SQL_INSERT_TRADE_EXCHANGE = "INSERT INTO trade(requestID, material, amount, offerred) VALUES (?, ?, ?, ?)";

    public Connection getConnection() throws SQLException {

        if(connection != null){
            return connection;
        }

        //Try to connect to my MySQL database running locally
        String url = "jdbc:mysql://localhost/barterplus";
        String user = "root";
        String password = "root";

        Connection connection = DriverManager.getConnection(url, user, password);

        this.connection = connection;

        System.out.println("Connected to database.");

        return connection;
    }
    
    @Override
    public void onEnable() {
        instance = this;
        maxRuns = new int[Scenario.values().length]; // Initialize maxRuns array based on the number of scenarios
        // Plugin startup logic
        BarterPlus.inst().getLogger().log(Level.INFO, "########################################");
        BarterPlus.inst().getLogger().log(Level.INFO, "                BarterPlus              ");
        BarterPlus.inst().getLogger().log(Level.INFO, "########################################");
        Bukkit.getPluginManager().registerEvents(new MenuFunctionListener(), this);
        instance.getServer().getPluginManager().registerEvents(new BarterPlusListener(), BarterPlus.instance);
        Configuration config = this.getConfig();
        temperature = (float) config.getDouble("temperature");
        topP = (float) config.getDouble("topP");
        //seed = config.getInt("openai-seed");
        config.addDefault("numberOfGeminiAgents", 0);
        config.addDefault("openai-model", "gpt-4o");
        config.addDefault("gemini-model", "gemini-1.5-turbo");
        config.addDefault("scenario", "baseline");
        config.addDefault("cooperative-message-five-min-text", "Remember, all of you will win the bonus only if each one of you scores at least 120 points individually.");
        config.addDefault("cooperative-message-all-fail-text", "Since all of you failed to reach 120 points individually, you all lost the session. You all do not receive the bonus.");
        config.addDefault("cooperative-message-all-pass-text", "Since all of you reached 120 points individually, You all receive the bonus.");

        config.addDefault("competitive-message-five-min-text", "Remember, only the player with the highest score will win the bonus.");
        config.addDefault("competitive-message-winner-text", "Since %s scored the highest points, they will receive the bonus and others will not.");
        config.addDefault("competitive-message-tie-text", "Since everyone tied, all players will receive the bonus.");

        config.addDefault("baseline-message-five-min-text", "Remember, anyone who scores at least 100 points will win the bonus.");
        config.addDefault("baseline-message-all-fail-text", "Since all of you failed to reach 100 points individually, you all lost the session. You all do not receive the bonus.");
        config.addDefault("baseline-message-all-pass-text", "Since all of you reached 100 points individually, You all receive the bonus.");
        config.addDefault("baseline-message-some-pass-text", " Since %s scored at least 100 points, they will receive the bonus and others will not.");
        config.addDefault("maxRuns.cooperative", 5);
        config.addDefault("maxRuns.competitive", 5);
        config.addDefault("maxRuns.baseline", 5);
        currentScenario = Scenario.valueOf(config.getString("scenario").toUpperCase());
        maxRuns[Scenario.COOPERATIVE.ordinal()] = config.getInt("maxRuns.cooperative");
        maxRuns[Scenario.COMPETITIVE.ordinal()] = config.getInt("maxRuns.competitive");
        maxRuns[Scenario.BASELINE.ordinal()] = config.getInt("maxRuns.baseline");
        numberOfGeminiAgents = config.getInt("numberOfGeminiAgents");
        gptModel = config.getString("openai-model");
        geminiModel = config.getString("gemini-model");

        if (numberOfGeminiAgents >= 5) {
            model = geminiModel;
        } else if (numberOfGeminiAgents == 0) {
            model = gptModel;
        } else {
            model = "Mixed - " + config.getString("openai-model") + " and " + config.getString("gemini-model");
        }

        geminiKey = config.getString("gemini-key");
        gptKey = config.getString("openai-key");

        //LegacyData.loadTraders();
        //new Database();
        barterKings = new BarterKings(instance);
        Objects.requireNonNull(this.getCommand("barter")).setExecutor(new CommandTrade());
        Objects.requireNonNull(this.getCommand("barter")).setTabCompleter(new ConstructTabCompleter());
        // set model, temperature, topP, seed from config

        //check if Citizens is present and enabled.

        if(getServer().getPluginManager().getPlugin("Citizens") == null || !getServer().getPluginManager().getPlugin("Citizens").isEnabled()) {
            getLogger().log(Level.SEVERE, "Citizens 2.0 not found or not enabled");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        //Register your trait with Citizens.
        net.citizensnpcs.api.CitizensAPI.getTraitFactory().registerTrait(net.citizensnpcs.api.trait.TraitInfo.create(BarterTrait.class));

    }

    @EventHandler
    public void onCitizensEnable(CitizensEnableEvent ev) {
       // BarterKings.npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "TraderPrototype1");
       // BarterKings.npc.setName("Joe2");
        //BarterKings.npc.spawn(new Location(Bukkit.getWorld("world"), -704 + Math.random()+5, 73, 71 + Math.random()+5));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (BarterKings.barterGame != null)
            if (BarterKings.barterGame.inProgress())
                BarterKings.barterGame.attemptEnd();
        LegacyData.getTradersAndSave();
    }


}
