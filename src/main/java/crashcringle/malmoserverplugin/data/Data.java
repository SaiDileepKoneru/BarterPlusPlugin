package crashcringle.malmoserverplugin.data;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import crashcringle.malmoserverplugin.MalmoServerPlugin;
import crashcringle.malmoserverplugin.barterkings.BarterKings;

import crashcringle.malmoserverplugin.barterkings.MalmoTrader;
import org.bukkit.Bukkit;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;


public class Data implements Serializable {
    private static transient final long serialVersionUID = -1681012206529286330L;

    public final Map<Integer, MalmoTrader> traders;


    // Can be used for saving
    public Data(Map<Integer, MalmoTrader> traders) {
        this.traders = traders;
    }
    // Can be used for loading
    public Data(Data loadedData) {
        this.traders = loadedData.traders;
    }

    public boolean saveData(String filePath) {
        try {
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(new GZIPOutputStream(new FileOutputStream(MalmoServerPlugin.inst().getDataFolder().getPath() + filePath)));
            out.writeObject(this);
            out.close();
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }
    public static Data loadData(String filePath) {
        try {
            BukkitObjectInputStream in = new BukkitObjectInputStream(new GZIPInputStream(new FileInputStream(MalmoServerPlugin.inst().getDataFolder().getPath() +filePath)));
            Data data = (Data) in.readObject();
            in.close();
            return data;
        } catch (ClassNotFoundException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
    public static void getTradersAndSave() {
        new Data(BarterKings.getTraders()).saveData("/Traders.data");
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Data Saved");
    }
    public static void loadTraders() {
        // Load the data from disc using our loadData method.
        File file = new File(MalmoServerPlugin.inst().getDataFolder().getPath() +"/Traders.data");
        if (file.exists()) {
            Data data = new Data(Data.loadData("/Traders.data"));
            BarterKings.setTraders(data.traders);
        }
        Bukkit.getServer().getLogger().log(Level.INFO, "Data loaded");
    }
}