package crashcringle.malmoserverplugin;

import crashcringle.malmoserverplugin.barterkings.BarterKings;
import crashcringle.malmoserverplugin.barterkings.players.PlayerHandler;
import crashcringle.malmoserverplugin.commands.CommandTrade;
import crashcringle.malmoserverplugin.commands.ConstructTabCompleter;
import crashcringle.malmoserverplugin.data.LegacyData;
import org.bukkit.Bukkit;
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

    final String SQL_INSERT_TRADE_REQUEST ="INSERT INTO trade_request(requester, requested, status, time_created, time_finished, game) VALUES (?, ?, ?, ?, ?, ?)";
    final String SQL_INSERT_TRADE_EXCHANGE = "INSERT INTO trade(requestID, material, amount, offerred) VALUES (?, ?, ?, ?)";

    public Connection getConnection() throws SQLException {

        if(connection != null){
            return connection;
        }

        //Try to connect to my MySQL database running locally
        String url = "jdbc:mysql://localhost/barterplus";
        String user = "root";
        String password = "";

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
        new BarterKings(instance);
        Objects.requireNonNull(this.getCommand("barter")).setExecutor(new CommandTrade());
        Objects.requireNonNull(this.getCommand("barter")).setTabCompleter(new ConstructTabCompleter());
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        LegacyData.getTradersAndSave();
    }
}
