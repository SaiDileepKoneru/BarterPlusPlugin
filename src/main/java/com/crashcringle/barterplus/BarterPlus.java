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

    public int globalBufferTime = 1;

    // Get the time
    public long lastTime = (System.currentTimeMillis());

    private Connection connection;
    private BarterKings barterKings;
    public String model = "gpt-4o";
    public float temperature = 0f;
    public float topP = 0.3f;
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
        // Plugin startup logic
        BarterPlus.inst().getLogger().log(Level.INFO, "########################################");
        BarterPlus.inst().getLogger().log(Level.INFO, "                BarterPlus              ");
        BarterPlus.inst().getLogger().log(Level.INFO, "########################################");
        Bukkit.getPluginManager().registerEvents(new MenuFunctionListener(), this);
        instance.getServer().getPluginManager().registerEvents(new BarterPlusListener(), BarterPlus.instance);

        //LegacyData.loadTraders();
        //new Database();
        barterKings = new BarterKings(instance);
        Objects.requireNonNull(this.getCommand("barter")).setExecutor(new CommandTrade());
        Objects.requireNonNull(this.getCommand("barter")).setTabCompleter(new ConstructTabCompleter());
        // set model, temperature, topP, seed from config
        Configuration config = this.getConfig();
        model = config.getString("openai-model");
        temperature = (float) config.getDouble("openai-temperature");
        topP = (float) config.getDouble("openai-topP");
        seed = config.getInt("openai-seed");

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
