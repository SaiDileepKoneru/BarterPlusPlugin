package crashcringle.malmoserverplugin.data;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import crashcringle.malmoserverplugin.MalmoServerPlugin;
import crashcringle.malmoserverplugin.barterkings.BarterKings;
import crashcringle.malmoserverplugin.barterkings.trades.TradeRequest;
import crashcringle.malmoserverplugin.barterkings.villagers.MalmoTrader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class Data implements Serializable {
    private static transient final long serialVersionUID = -1681012206529286330L;

    public Map<Integer, MalmoTrader> map;
    public static Map<Player, ArrayList<TradeRequest>> outgoingRequests;
    public static Map<Player, ArrayList<TradeRequest>> incomingRequests;
    public /*static*/ final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private File configFile;

    public void onEnable() {
        configFile = new File(MalmoServerPlugin.inst().getDataFolder(), "config.json");
        if (!configFile.exists()) MalmoServerPlugin.inst().saveResource(configFile.getName(), false);
        try {
            map = gson.fromJson(new FileReader(configFile), new HashMap<String, MalmoTrader>().getClass());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onDisable() {
        final String json = gson.toJson(map); // Remember pretty printing? This is needed here.
        try {
            configFile.delete(); // won't throw an exception, don't worry.
            Files.write(configFile.toPath(), json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE); // java.nio.Files
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}