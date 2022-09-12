package crashcringle.malmoserverplugin;

import crashcringle.malmoserverplugin.barterkings.BarterKings;
import crashcringle.malmoserverplugin.barterkings.MalmoTrader;
import crashcringle.malmoserverplugin.data.Data;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public final class MalmoServerPlugin extends JavaPlugin {

    private static MalmoServerPlugin instance;

    public static MalmoServerPlugin inst() { return instance; }

    @Override
    public void onEnable() {
        instance = this;
        // Plugin startup logic
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "########################################");
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "            Malmo Server Plugin         ");
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "########################################");
        instance.getServer().getPluginManager().registerEvents(new MalmoServerListener(), MalmoServerPlugin.instance);
        Data.loadTraders();
        new BarterKings(instance);
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Data.getTradersAndSave();
    }
}
