package crashcringle.malmoserverplugin;

import crashcringle.malmoserverplugin.barterkings.BarterKings;
import crashcringle.malmoserverplugin.barterkings.npc.BarterTrait;
import crashcringle.malmoserverplugin.commands.CommandTrade;
import crashcringle.malmoserverplugin.commands.ConstructTabCompleter;
import crashcringle.malmoserverplugin.data.Database;
import crashcringle.malmoserverplugin.data.LegacyData;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.ipvp.canvas.MenuFunctionListener;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;

public final class MalmoServerPlugin extends JavaPlugin {

    private static MalmoServerPlugin instance;

    public static MalmoServerPlugin inst() { return instance; }

    private Connection connection;
    private BarterKings barterKings;

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
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "########################################");
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "            Malmo Server Plugin         ");
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "########################################");
        Bukkit.getPluginManager().registerEvents(new MenuFunctionListener(), this);
        instance.getServer().getPluginManager().registerEvents(new MalmoServerListener(), MalmoServerPlugin.instance);

        LegacyData.loadTraders();
        new Database();
        barterKings = new BarterKings(instance);
        Objects.requireNonNull(this.getCommand("barter")).setExecutor(new CommandTrade());
        Objects.requireNonNull(this.getCommand("barter")).setTabCompleter(new ConstructTabCompleter());


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
