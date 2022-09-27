package crashcringle.malmoserverplugin.barterkings.villagers;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/**
 * Created by MagmaGuy on 26/04/2017.
 */
public class MetadataHandler implements Listener {

    /*
    EliteMobs has ceased using metadata as its primary get/set way of cheating associating data to mobs
    The remaining metadata are here purely for third party compatibility.
    EntityTracker and EliteMobEntity expose pretty much everything you could want API-wise
     */

    //plugin name
    public final static String MALMO_SERVER = "MalmoServerPlugin";
    //plugin getter
    public static Plugin PLUGIN = Bukkit.getPluginManager().getPlugin(MetadataHandler.MALMO_SERVER);


    public static int signatureID = 31173;

}
