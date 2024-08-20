package com.crashcringle.barterplus.data;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;

import com.crashcringle.barterplus.BarterPlus;
import com.crashcringle.barterplus.barterkings.trades.TradeRequest;
import com.crashcringle.barterplus.barterkings.villagers.MalmoTrader;
import org.bukkit.entity.Player;

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
        configFile = new File(BarterPlus.inst().getDataFolder(), "config.json");
//        //JDBC - Java Database Connectivity API
//        this.database = new Database();
//        try {
//            this.database.initializeDatabase();
//        } catch (SQLException e) {
//            e.printStackTrace();
//            System.out.println("Could not initialize database.");
//        }


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