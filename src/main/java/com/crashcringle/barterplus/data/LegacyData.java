package com.crashcringle.barterplus.data;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.crashcringle.barterplus.BarterPlus;
import com.crashcringle.barterplus.barterkings.BarterKings;
import com.crashcringle.barterplus.barterkings.trades.TradeRequest;
import com.crashcringle.barterplus.barterkings.villagers.MalmoTrader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;


public class LegacyData implements Serializable {
    private static transient final long serialVersionUID = -1681012206529286330L;

    public final Map<Integer, MalmoTrader> traders;
    public static Map<Player, ArrayList<TradeRequest>> outgoingRequests;
    public static Map<Player, ArrayList<TradeRequest>> incomingRequests;
    
    // Can be used for saving
    public LegacyData(Map<Integer, MalmoTrader> traders) {
        this.traders = traders;
    }
    // Can be used for loading
    public LegacyData(LegacyData loadedData) {
        this.traders = loadedData.traders;
    }

    public boolean saveData(String filePath) {
        try {
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(new GZIPOutputStream(new FileOutputStream(BarterPlus.inst().getDataFolder().getPath() + filePath)));
            out.writeObject(this);
            out.close();
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }
    public static LegacyData loadData(String filePath) {
        try {
            BukkitObjectInputStream in = new BukkitObjectInputStream(new GZIPInputStream(new FileInputStream(BarterPlus.inst().getDataFolder().getPath() +filePath)));
            LegacyData data = (LegacyData) in.readObject();
            in.close();
            return data;
        } catch (ClassNotFoundException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
    public static void getTradersAndSave() {
        new LegacyData(BarterKings.getTraders()).saveData("/Traders.data");
        BarterPlus.inst().getLogger().log(Level.INFO, "Data Saved");
    }
    public static void loadTraders() {
        // Load the data from disc using our loadData method.
        File file = new File(BarterPlus.inst().getDataFolder().getPath() +"/Traders.data");
        if (file.exists()) {
            LegacyData data = new LegacyData(LegacyData.loadData("/Traders.data"));
            BarterKings.setTraders(data.traders);
        }
        Bukkit.getServer().getLogger().log(Level.INFO, "Data loaded");
    }
}