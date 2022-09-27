package crashcringle.malmoserverplugin;

import crashcringle.malmoserverplugin.barterkings.BarterKings;
import crashcringle.malmoserverplugin.commands.CommandTrade;
import crashcringle.malmoserverplugin.commands.ConstructTabCompleter;
import crashcringle.malmoserverplugin.data.Data;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
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
        Objects.requireNonNull(this.getCommand("barter")).setExecutor(new CommandTrade());
        Objects.requireNonNull(this.getCommand("barter")).setTabCompleter(new ConstructTabCompleter());
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Data.getTradersAndSave();
    }
}
