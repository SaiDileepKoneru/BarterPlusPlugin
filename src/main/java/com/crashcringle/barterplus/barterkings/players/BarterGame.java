package com.crashcringle.barterplus.barterkings.players;

import com.crashcringle.barterplus.BarterPlus;
import com.crashcringle.barterplus.barterkings.npc.BarterTrait;
import com.crashcringle.barterplus.barterkings.trades.TradeController;
import com.crashcringle.barterplus.barterkings.trades.TradeRequest;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

public class BarterGame {

    private static List<Participant> participants;

    private static List<NPC> npcs;
    private Profession Farmer;
    private Profession Blacksmith;
    private Profession Fisherman;
    private Profession Butcher;
    private Profession Leatherworker;
    private Profession Mason;
    private Profession Shepherd;
    private Profession Lumberjack;

    private Profession Librarian;
    private Profession Deceiver;

    private ArrayList<ItemStack> tier1Items = new ArrayList<>();
    private ArrayList<ItemStack> tier2Items = new ArrayList<>();
    private ArrayList<ItemStack> tier3Items = new ArrayList<>();
    Map<Participant, Integer> gameScores = new HashMap<>();

    Participant winner;
    boolean inprogress = false;
    int id = 0;

    public void setParticipants(List<Participant> participants) {
        BarterGame.participants = participants;
    }

    public void Checkup() {
        if (allReady()) {

            inprogress = true;
            id = (int) System.currentTimeMillis();
            Bukkit.broadcastMessage(ChatColor.YELLOW + "The Barter Game has begun!");
            setUpProfessions();
            setUpParticipants();
            findAllActiveTierItems();
            distributeItems();
            teleportPlayers();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "bbt begin 30m barterKings white &6&lBarter Plus! &e&l<minutes> &6minutes and &e&l<seconds> &6seconds left!");
        } else {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Not all players are ready!");
        }

    }
    public void Checkup(int minutes) {
        if (allReady()) {
            inprogress = true;
            id = (int) System.currentTimeMillis();
            Bukkit.broadcastMessage(ChatColor.YELLOW + "The Barter Game has begun!");
            setUpProfessions();
            setUpParticipants();
            findAllActiveTierItems();
            distributeItems();
            teleportPlayers();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "bbt begin "+minutes+"m barterKings white &6&lBarter Kings! &e&l<minutes> &6minutes and &e&l<seconds> &6seconds left!");
        } else {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Not all players are ready!");
        }
    }

    public int getId() {
        return id;
    }

    public BarterGame() {
        participants = new ArrayList<>();

    }
    public final static String fm( Material material ) {
        if ( material == null ) {
            return null;
        }
        StringBuilder friendlyName = new StringBuilder();
        for ( String word : material.name().split( "_" ) ) {
            friendlyName.append( word.substring( 0, 1 ).toUpperCase() + word.substring( 1 ).toLowerCase() + " " );
        }
        return friendlyName.toString().trim();
    }


    public Scoreboard createScoreboard(Profession profession) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("Trading Goals", "dummy");
        objective.setDisplayName(ChatColor.GOLD + "Role: " + ChatColor.YELLOW + profession.getName());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        Score score1 = objective.getScore(ChatColor.GOLD + "" + ChatColor.BOLD + "Trading Goals");
        score1.setScore(150);
        // Score score2 = objective.getScore(ChatColor.GOLD + "Collect them all!");
        for (ItemStack item : profession.getTier1Items()) {
            Score score = objective.getScore(ChatColor.GREEN + fm(item.getType()) + " " + ChatColor.YELLOW );
            score.setScore(1);

        }

        for (ItemStack item : profession.getTier2Items()) {
            Score score = objective.getScore(ChatColor.DARK_GREEN + fm(item.getType()) + " " + ChatColor.YELLOW);
            score.setScore(3);
        }
        for (ItemStack item : profession.getTier3Items()) {
            Score score = objective.getScore(ChatColor.AQUA + fm(item.getType()) + " " + ChatColor.YELLOW  );
            score.setScore(10);
        }
        return scoreboard;
    }
    public void teleportPlayers() {
        for (Participant participant : participants) {
            participant.getPlayer().setScoreboard(createScoreboard(participant.getProfession()));
            participant.getPlayer().teleport(new Location(Bukkit.getWorld("world"), -704 + Math.random()*5, 73, 71 + Math.random()*5));
        }

    }

    public boolean inProgress() {
        return inprogress;
    }
    public void addParticipant(Player player) {
        Participant participant = new Participant(player);
        switch(getParticipants().size()) {
            case 0:
                participant.setColor(net.md_5.bungee.api.ChatColor.of("#ffc7bb"));
                break;
            case 1:
                participant.setColor(net.md_5.bungee.api.ChatColor.of("#ffe9bb"));
                break;
            case 2:
                participant.setColor(net.md_5.bungee.api.ChatColor.of("#f3ffbb"));
                break;
            case 3:
                participant.setColor(net.md_5.bungee.api.ChatColor.of("#d1ffbb"));
                break;
            case 4:
                participant.setColor(net.md_5.bungee.api.ChatColor.of("#bbffe9"));
                break;
            case 5:
                participant.setColor(net.md_5.bungee.api.ChatColor.of("#c8bbff"));
                break;
            case 6:
                participant.setColor(net.md_5.bungee.api.ChatColor.of("#eabbff"));
                break;
            case 7:
                participant.setColor(net.md_5.bungee.api.ChatColor.of("#ffbbf2"));
                break;
        }
        getParticipants().add(participant);
    }

    public void attemptStart() {
        if (!inprogress) {
            getParticipants().clear();
            Checkup();
        }
        else
            Bukkit.broadcastMessage(ChatColor.YELLOW + "The Barter Game is already in progress!");
    }
    public void attemptStart(int minutes) {
        if (!inprogress) {
            getParticipants().clear();
            Checkup(minutes);
        }
        else
            Bukkit.broadcastMessage(ChatColor.YELLOW + "The Barter Game is already in progress!");
    }
    public void attemptEnd() {

        Bukkit.broadcastMessage(ChatColor.GOLD + "Barter Game has ended!");
        calculateScores();
        Bukkit.broadcastMessage(ChatColor.GOLD + "The winner is " + ChatColor.YELLOW + winner.getPlayer().getName() + ChatColor.GOLD + " with a score of " + ChatColor.YELLOW + winner.getScore());
        // The participant list should be sorted by score so we can print out the rankings
        for (int i = 1; i < getParticipants().size(); i++) {
            ChatColor color;
            Participant participant = getParticipants().get(i);
            if (participant.getScore() > 120) {
                color = ChatColor.GREEN;
            } else {
                color = ChatColor.YELLOW;
            }
            Bukkit.broadcastMessage(ChatColor.GOLD + "Rank " + ChatColor.YELLOW + (i + 1) + ChatColor.GOLD + " is " + ChatColor.YELLOW + participant.getPlayer().getName() + ChatColor.GOLD + " with a score of " + color + participant.getScore());
        }
        // Broadcast the total increase in score for each participant
        for (Participant participant : getParticipants()) {
            int increase = participant.getScore() - participant.starterScore;
            ChatColor color;
            if (increase > 0) {
                color = ChatColor.GREEN;
            } else {
                color = ChatColor.RED;
            }
            Bukkit.broadcastMessage(ChatColor.GOLD + participant.getPlayer().getName() + " has increased their score by " + color + increase);
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "bbt end barterKings");
        for (Participant participant : getParticipants()) {
            try {
                participant.getPlayer().getScoreboard().getObjective("Trading Goals").unregister();;
                participant.getPlayer().getInventory().clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        JSONObject data = new JSONObject();
        JSONArray barterGames = new JSONArray();
        JSONObject barterGame = new JSONObject();
        barterGame.put("seed", BarterPlus.inst().seed);
        barterGame.put("model", BarterPlus.inst().model);
        barterGame.put("temperature", BarterPlus.inst().temperature);
        barterGame.put("topP", BarterPlus.inst().topP);
        barterGame.put("id", id);
        barterGame.put("winner", winner.getPlayer().getName());
        barterGame.put("score", winner.getScore());
        barterGame.put("participants", getParticipants().size());
        barterGame.put("date", new Date().toString());
        JSONArray participants = new JSONArray();
        for (Participant participant : getParticipants()) {
            JSONObject participantJSON = new JSONObject();
            participantJSON.put("name", participant.getPlayer().getName());
            participantJSON.put("score", participant.getScore());
            participantJSON.put("profession", participant.getProfession().getName());
            participantJSON.put("uuid", participant.getPlayer().getUniqueId().toString());
            participants.add(participantJSON);
        }
        barterGame.put("participants", participants);
        JSONArray trades = new JSONArray();
        for (TradeRequest tradeRequest : TradeController.getAllRequests()) {
            trades.add(tradeRequest.toJSON());
        }
        barterGame.put("trades", trades);
        try {
            File file = new File(BarterPlus.inst().getDataFolder().getPath() + "/barterGame.json");
            if (file.exists()) {
                // Read the json to a JSON object
                JSONParser parser = new JSONParser();
                try {
                    String content = new String(Files.readAllBytes(Path.of(BarterPlus.inst().getDataFolder().getPath() + "/barterGame.json")));
                  //  BarterPlus.inst().getLogger().log(Level.INFO, content);
                    // Log the contents of the file
                    Object p = parser.parse(content);
                    if(p instanceof JSONArray){
                        BarterPlus.inst().getLogger().log(Level.INFO, "JSONArray");
                        org.json.simple.JSONArray object = (JSONArray)p;
                        barterGames = object;
                    }
                    else if(p instanceof JSONObject){
                        BarterPlus.inst().getLogger().log(Level.INFO, "JSONObject");
                        org.json.simple.JSONObject object = (JSONObject)p;
                        barterGames = (JSONArray) object.get("barterGames");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            barterGames.add(barterGame);
            data.put("barterGames", barterGames);
            Writer fileW = new BufferedWriter(new FileWriter(file));
            fileW.write(data.toJSONString());
            fileW.flush();
            fileW.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        inprogress = false;

    }
    
    public void removeParticipant(Player player) {
        getParticipants().remove(getParticipant(player));
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public  Participant getParticipant(Player player) {
        for (Participant participant : getParticipants()) {
            if (participant.getPlayer().equals(player)) {
                return participant;
            }
        }
        return null;
    }

    public  Participant getParticipant(String name) {
        for (Participant participant : getParticipants()) {
            if (participant.getName().equalsIgnoreCase(name)) {
                return participant;
            }
        }
        return null;
    }

    public  boolean isParticipant(Player player) {
        for (Participant participant : getParticipants()) {
            if (participant.getPlayer().equals(player)) {
                return true;
            }
        }
        return false;
    }

    public  void readyUp(Player player) {
        for (Participant participant : getParticipants()) {
            if (participant.getPlayer().equals(player)) {
                participant.readyUp();
                return;
            }
        }
        player.sendMessage(ChatColor.RED + "You are not a participant!");

    }

    public  void unready(Player player) {
        for (Participant participant : getParticipants()) {
            if (participant.getPlayer().equals(player)) {
                participant.unready();
                return;
            }
        }
        player.sendMessage(ChatColor.RED + "You are not a participant!");
    }

    public boolean isReady(Player player) {
        for (Participant participant : getParticipants()) {
            if (participant.getPlayer().equals(player)) {
                return participant.isReady();
            }
        }
        return false;
    }

    public boolean allReady() {
        for (Participant participant : getParticipants()) {
            if (!participant.isReady()) {
                return false;
            }
        }
        return true;
    }
    public void setUpEveryoneAsParticipants() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Participant participant = new Participant(player);
            participant.setProfession(getRandomProfession());
            BarterPlus.inst().getLogger().info("Player " + player.getName() + " has been assigned the profession " + participant.getProfession().getName());
            getParticipants().add(participant);
        }
    }

    public void setUpParticipants() {
        int counter = 0;
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (npc.hasTrait(BarterTrait.class)) {
                BarterPlus.inst().getLogger().info("Adding NPC " + npc.getName());
                Player player = (Player) npc.getEntity();
                BarterPlus.inst().getLogger().info("Player " + player.getName());
                participants.add(counter % 2 == 0 ? new GeminiNPC(npc) : new NpcParticipant(npc));
                counter++;
                // Clear their inventory
                ((Player) npc.getEntity()).getInventory().clear();
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            Participant participant = new Participant(player);
           // participant.setProfession(getRandomProfession());
            //BarterPlus.inst().getLogger().info("Player " + player.getName() + " has been assigned the profession " + participant.getProfession().getName());
            participants.add(participant);
        }
        for (Participant participant : getParticipants()) {
//            if (Objects.equals(participant.name, "JOHN")) {
//                participant.setProfession(getFarmer());
//            } else if (Objects.equals(participant.name, "BOBBY")) {
//                participant.setProfession(getBlacksmith());
//            } else {
//                participant.setProfession(getRandomProfession());
//            }
            participant.setProfession(getRandomProfession());

            BarterPlus.inst().getLogger().info("Player " + participant.getPlayer().getName() + " has been assigned the profession " + participant.getProfession().getName());
        }
    }

    public void distributeItems() {

        distributeTier1Items();
        distributeTier2Items();
        distributeTier3Items();

        // Log the scores of all players
        for (Participant participant : getParticipants()) {
            participant.calculateTrueSilentScore();
            BarterPlus.inst().getLogger().info(participant.getPlayer().getName() + " has a score of " + participant.getScore());
            participant.starterScore = participant.getScore();
        }
    }
    public void distributeTier1Items() {
        int randomIndex;
        for (ItemStack item : getTier1Items()) {
            int amt = 20;
            while (amt > 0) {
                int amtToGive = amt >= 2 ? ((int) (Math.random() * amt)) : 1;
                if (item.getType().toString().toUpperCase().contains("SWORD") || item.getType().toString().toUpperCase().contains("AXE") || item.getType().toString().toUpperCase().contains("BOW")
                || item.getType().toString().toUpperCase().contains("HELMET") || item.getType().toString().toUpperCase().contains("CHESTPLATE") || item.getType().toString().toUpperCase().contains("LEGGINGS")
                || item.getType().toString().toUpperCase().contains("BOOT") || item.getType().toString().toUpperCase().contains("TRIDENT") || item.getType().toString().toUpperCase().contains("ELYTRA") || item.getType().toString().toUpperCase().contains("SHIELD")
                || item.getType().toString().toUpperCase().contains("HOE") || item.getType().toString().toUpperCase().contains("PICKAXE") || item.getType().toString().toUpperCase().contains("SHOVEL") || item.getType().toString().toUpperCase().contains("FISHING_ROD")
                || item.getType().toString().toUpperCase().contains("CROSSBOW") || item.getType().toString().toUpperCase().contains("CARROT_ON_A_STICK") || item.getType().toString().toUpperCase().contains("FLINT_AND_STEEL") || item.getType().toString().toUpperCase().contains("SHEARS")
                || item.getType().toString().toUpperCase().contains("SPADE") || item.getType().toString().toUpperCase().contains("HORSE_ARMOR") || item.getType().toString().toUpperCase().contains("STEW") || item.getType().toString().toUpperCase().contains("BUCKET") || item.getType().toString().toUpperCase().contains("BOAT")
                ) {
                    amtToGive = 1;
                }
                randomIndex =  (int) (Math.random() * getParticipants().size());
                // Check if that participant is kalyaniplays
                if (getParticipants().get(randomIndex).getPlayer().getName().equals("kalyaniplays") || getParticipants().get(randomIndex).getPlayer().getName().contains("kalyani") || getParticipants().get(randomIndex).getPlayer().getName().contains("Air")) {
                    continue;
                } else {
                    item.setAmount(amtToGive);
                    getParticipants().get(randomIndex).getPlayer().getInventory().addItem(item);
                    BarterPlus.inst().getLogger().info("Tier 1 | Giving " + getParticipants().get(randomIndex).getPlayer().getName() + " " + amtToGive + " " + item.getType());
                    amt = amt - amtToGive;
                }

            }
        }
    }

    public void distributeTier2Items() {
        int randomIndex;
        for (ItemStack item : getTier2Items()) {
            int amt = 10;
            while (amt > 0) {
                int amtToGive = amt >= 2 ? ((int) (Math.random() * amt)) : 1;
                if (item.getType().toString().toUpperCase().contains("SWORD") || item.getType().toString().toUpperCase().contains("AXE") || item.getType().toString().toUpperCase().contains("BOW")
                || item.getType().toString().toUpperCase().contains("HELMET") || item.getType().toString().toUpperCase().contains("CHESTPLATE") || item.getType().toString().toUpperCase().contains("LEGGINGS")
                || item.getType().toString().toUpperCase().contains("BOOT") || item.getType().toString().toUpperCase().contains("TRIDENT") || item.getType().toString().toUpperCase().contains("ELYTRA") || item.getType().toString().toUpperCase().contains("SHIELD")
                || item.getType().toString().toUpperCase().contains("HOE") || item.getType().toString().toUpperCase().contains("PICKAXE") || item.getType().toString().toUpperCase().contains("SHOVEL") || item.getType().toString().toUpperCase().contains("FISHING_ROD")
                || item.getType().toString().toUpperCase().contains("CROSSBOW") || item.getType().toString().toUpperCase().contains("CARROT_ON_A_STICK") || item.getType().toString().toUpperCase().contains("FLINT_AND_STEEL") || item.getType().toString().toUpperCase().contains("SHEARS")
                || item.getType().toString().toUpperCase().contains("SPADE") || item.getType().toString().toUpperCase().contains("HORSE_ARMOR") || item.getType().toString().toUpperCase().contains("STEW") || item.getType().toString().toUpperCase().contains("BUCKET") || item.getType().toString().toUpperCase().contains("BOAT")
            ) {
                amtToGive = 1;
            }
                // Check if that participant is kalyaniplays
                randomIndex = (int) (Math.random() * getParticipants().size());
                if (getParticipants().get(randomIndex).getPlayer().getName().equals("kalyaniplays") || getParticipants().get(randomIndex).getPlayer().getName().contains("kalyani") || getParticipants().get(randomIndex).getPlayer().getName().contains("Air")) {
                    continue;
                } else {
                    item.setAmount(amtToGive);
                    getParticipants().get(randomIndex).getPlayer().getInventory().addItem(item);
                    BarterPlus.inst().getLogger().info("Tier 2 | Giving " + getParticipants().get(randomIndex).getPlayer().getName() + " " + amtToGive + " " + item.getType());
                    amt = amt - amtToGive;
                }
            }
        }
    }

    public void distributeTier3Items() {
        int randomIndex;
        for (ItemStack item : getTier3Items()) {
            int amt = 3;
            while (amt > 0) {
                int amtToGive = amt >= 2 ? ((int) (Math.random() * amt)) : 1;
                if (item.getType().toString().toUpperCase().contains("SWORD") || item.getType().toString().toUpperCase().contains("AXE") || item.getType().toString().toUpperCase().contains("BOW")
                        || item.getType().toString().toUpperCase().contains("HELMET") || item.getType().toString().toUpperCase().contains("CHESTPLATE") || item.getType().toString().toUpperCase().contains("LEGGINGS")
                        || item.getType().toString().toUpperCase().contains("BOOT") || item.getType().toString().toUpperCase().contains("TRIDENT") || item.getType().toString().toUpperCase().contains("ELYTRA") || item.getType().toString().toUpperCase().contains("SHIELD")
                        || item.getType().toString().toUpperCase().contains("HOE") || item.getType().toString().toUpperCase().contains("PICKAXE") || item.getType().toString().toUpperCase().contains("SHOVEL") || item.getType().toString().toUpperCase().contains("FISHING_ROD")
                        || item.getType().toString().toUpperCase().contains("CROSSBOW") || item.getType().toString().toUpperCase().contains("CARROT_ON_A_STICK") || item.getType().toString().toUpperCase().contains("FLINT_AND_STEEL") || item.getType().toString().toUpperCase().contains("SHEARS")
                        || item.getType().toString().toUpperCase().contains("SPADE") || item.getType().toString().toUpperCase().contains("HORSE_ARMOR") || item.getType().toString().toUpperCase().contains("STEW") || item.getType().toString().toUpperCase().contains("BUCKET") || item.getType().toString().toUpperCase().contains("BOAT")
                ) {
                    amtToGive = 1;
                }
                randomIndex =  (int) (Math.random() * getParticipants().size());
                // Check if that participant is kalyaniplays
                if (getParticipants().get(randomIndex).getPlayer().getName().equals("kalyaniplays") || getParticipants().get(randomIndex).getPlayer().getName().contains("kalyani") || getParticipants().get(randomIndex).getPlayer().getName().contains("Air")) {
                    continue;
                } else {
                    item.setAmount(amtToGive);
                    getParticipants().get(randomIndex).getPlayer().getInventory().addItem(item);
                    BarterPlus.inst().getLogger().info("Tier 3 | Giving " + getParticipants().get(randomIndex).getPlayer().getName() + " " + amtToGive + " " + item.getType());
                    amt = amt - amtToGive;
                }

            }
        }
    }


    public void findAllActiveTierItems() {
        for (Participant participant : getParticipants()) {
            getTier1Items().addAll(participant.getProfession().getTier1Items());
            getTier2Items().addAll(participant.getProfession().getTier2Items());
            getTier3Items().addAll(participant.getProfession().getTier3Items());
        }
    }


    public void setUpProfessions() {

        List<ItemStack> tier1 = new ArrayList<>();
        tier1.add(new ItemStack(Material.POTATO));
        tier1.add(new ItemStack(Material.CARROT));
        tier1.add(new ItemStack(Material.WHEAT));

        List<ItemStack> tier2 = new ArrayList<>();
        tier2.add(new ItemStack(Material.BREAD));
        tier2.add(new ItemStack(Material.PUMPKIN));

        List<ItemStack> tier3 = new ArrayList<>();
        tier3.add(new ItemStack(Material.MUSHROOM_STEW));

        setFarmer(new Profession("Farmer", tier1, tier2, tier3));

        List<ItemStack> tier1Fisher = new ArrayList<>();
        tier1Fisher.add(new ItemStack(Material.COD));
        tier1Fisher.add(new ItemStack(Material.SALMON));
        tier1Fisher.add(new ItemStack(Material.TROPICAL_FISH));

        List<ItemStack> tier2Fisher = new ArrayList<>();
        tier2Fisher.add(new ItemStack(Material.PUFFERFISH));
        tier2Fisher.add(new ItemStack(Material.TURTLE_EGG));

        List<ItemStack> tier3Fisher = new ArrayList<>();
        tier3Fisher.add(new ItemStack(Material.OAK_BOAT));

        setFisherman(new Profession("Fisher", tier1Fisher, tier2Fisher, tier3Fisher));

        List<ItemStack> tier1Butcher = new ArrayList<>();
        tier1Butcher.add(new ItemStack(Material.BEEF));
        tier1Butcher.add(new ItemStack(Material.CHICKEN));
        tier1Butcher.add(new ItemStack(Material.PORKCHOP));

        List<ItemStack> tier2Butcher = new ArrayList<>();
        tier2Butcher.add(new ItemStack(Material.COOKED_BEEF));
        //tier2Butcher.add(new ItemStack(Material.COOKED_CHICKEN));
        tier2Butcher.add(new ItemStack(Material.COOKED_PORKCHOP));

        List<ItemStack> tier3Butcher = new ArrayList<>();
        tier3Butcher.add(new ItemStack(Material.RABBIT_STEW));

        setButcher(new Profession("Butcher", tier1Butcher, tier2Butcher, tier3Butcher));

        List<ItemStack> tier1Blacksmith = new ArrayList<>();
        tier1Blacksmith.add(new ItemStack(Material.IRON_INGOT));
        tier1Blacksmith.add(new ItemStack(Material.GOLD_INGOT));
        tier1Blacksmith.add(new ItemStack(Material.COAL));

        List<ItemStack> tier2Blacksmith = new ArrayList<>();
        tier2Blacksmith.add(new ItemStack(Material.FLETCHING_TABLE));
        tier2Blacksmith.add(new ItemStack(Material.SMITHING_TABLE));

        List<ItemStack> tier3Blacksmith = new ArrayList<>();
        tier3Blacksmith.add(new ItemStack(Material.DIAMOND_SWORD));

        setBlacksmith(new Profession("Blacksmith", tier1Blacksmith, tier2Blacksmith, tier3Blacksmith));

        List<ItemStack> tier1Leatherworker = new ArrayList<>();
        tier1Leatherworker.add(new ItemStack(Material.LEATHER));
        tier1Leatherworker.add(new ItemStack(Material.STICK));
        tier1Leatherworker.add(new ItemStack(Material.LEAD));

        List<ItemStack> tier2Leatherworker = new ArrayList<>();
        tier2Leatherworker.add(new ItemStack(Material.COW_SPAWN_EGG));
        tier2Leatherworker.add(new ItemStack(Material.RABBIT_HIDE));

        List<ItemStack> tier3Leatherworker = new ArrayList<>();
        tier3Leatherworker.add(new ItemStack(Material.SADDLE));

        setLeatherworker(new Profession("Leatherworker", tier1Leatherworker, tier2Leatherworker, tier3Leatherworker));

        List<ItemStack> tier1Mason = new ArrayList<>();
        tier1Mason.add(new ItemStack(Material.STONE));
        tier1Mason.add(new ItemStack(Material.QUARTZ_BLOCK));
        tier1Mason.add(new ItemStack(Material.GLASS));

        List<ItemStack> tier2Mason = new ArrayList<>();
        tier2Mason.add(new ItemStack(Material.RED_GLAZED_TERRACOTTA));
        tier2Mason.add(new ItemStack(Material.BLUE_GLAZED_TERRACOTTA));

        List<ItemStack> tier3Mason = new ArrayList<>();
        tier3Mason.add(new ItemStack(Material.STONE_PICKAXE));

        setMason(new Profession("Mason", tier1Mason, tier2Mason, tier3Mason));

        List<ItemStack> tier1Shepherd = new ArrayList<>();
        tier1Shepherd.add(new ItemStack(Material.STRING));
        tier1Shepherd.add(new ItemStack(Material.WHITE_WOOL));
        tier1Shepherd.add(new ItemStack(Material.BLUE_DYE));

        List<ItemStack> tier2Shepherd = new ArrayList<>();
        tier2Shepherd.add(new ItemStack(Material.SHEEP_SPAWN_EGG));
        tier2Shepherd.add(new ItemStack(Material.LOOM));

        List<ItemStack> tier3Shepherd = new ArrayList<>();
        tier3Shepherd.add(new ItemStack(Material.SHEARS));

        setShepherd(new Profession("Shepherd", tier1Shepherd, tier2Shepherd, tier3Shepherd));

        List<ItemStack> tier1Lumberjack = new ArrayList<>();
        tier1Lumberjack.add(new ItemStack(Material.OAK_LOG));
        tier1Lumberjack.add(new ItemStack(Material.SPRUCE_LOG));
        tier1Lumberjack.add(new ItemStack(Material.BIRCH_LOG));

        List<ItemStack> tier2Lumberjack = new ArrayList<>();
        tier2Lumberjack.add(new ItemStack(Material.SPRUCE_PLANKS));
        tier2Lumberjack.add(new ItemStack(Material.OAK_PLANKS));

        List<ItemStack> tier3lumberjack = new ArrayList<>();
        tier3lumberjack.add(new ItemStack(Material.DIAMOND_AXE));

        setLumberjack(new Profession("Lumberjack", tier1Lumberjack, tier2Lumberjack, tier3lumberjack));


    }
    List<Integer> used = new ArrayList<>();
    public Profession getRandomProfession() {
        int random = (int) (Math.random() * 8);
        if (used.contains(random)) {
            return getRandomProfession();
        } else {
            used.add(random);
        }
        switch (random) {
            case 0:
                return getFarmer();
            case 1:
                return getFisherman();
            case 2:
                return getButcher();
            case 3:
                return getBlacksmith();
            case 4:
                return getLeatherworker();
            case 5:
                return getMason();
            case 6:
                return getShepherd();
            case 7:
                return getLumberjack();
            default:
                return getFarmer();
        }
    }

    /**
     * This method will calculate the scores of the participants
     * and will return the winner
     * @return
     */
    public void calculateScores() {

        for (Participant user : getParticipants()) {
            user.calculateScore();
        }
        // Compare the scores of the participants to find the winner
        // Participant winner = getParticipants().get(0);
        // // for (Participant user : getParticipants()) {
        // //     if (user.getScore() > winner.getScore()) {
        // //         winner = user;
        // //     }
        // // }
        // Create a sorted list of participants based on their score

        getParticipants().sort(Comparator.comparing(Participant::getScore).reversed());
        
        winner = getParticipants().get(0);
    }

    public Profession getFarmer() {
        return Farmer;
    }

    public void setFarmer(Profession farmer) {
        Farmer = farmer;
    }

    public Profession getBlacksmith() {
        return Blacksmith;
    }

    public void setBlacksmith(Profession blacksmith) {
        Blacksmith = blacksmith;
    }

    public Profession getFisherman() {
        return Fisherman;
    }

    public void setFisherman(Profession fisherman) {
        Fisherman = fisherman;
    }

    public Profession getButcher() {
        return Butcher;
    }

    public void setButcher(Profession butcher) {
        Butcher = butcher;
    }

    public Profession getLeatherworker() {
        return Leatherworker;
    }

    public void setLeatherworker(Profession leatherworker) {
        Leatherworker = leatherworker;
    }

    public Profession getMason() {
        return Mason;
    }

    public void setMason(Profession mason) {
        Mason = mason;
    }

    public Profession getShepherd() {
        return Shepherd;
    }

    public void setShepherd(Profession shepherd) {
        Shepherd = shepherd;
    }

    public Profession getDeceiver() {
        return Deceiver;
    }

    public void setDeceiver(Profession deceiver) {
        Deceiver = deceiver;
    }

    public ArrayList<ItemStack> getTier1Items() {
        return tier1Items;
    }

    public void setTier1Items(ArrayList<ItemStack> tier1Items) {
        this.tier1Items = tier1Items;
    }

    public ArrayList<ItemStack> getTier2Items() {
        return tier2Items;
    }

    public void setTier2Items(ArrayList<ItemStack> tier2Items) {
        this.tier2Items = tier2Items;
    }

    public ArrayList<ItemStack> getTier3Items() {
        return tier3Items;
    }

    public void setTier3Items(ArrayList<ItemStack> tier3Items) {
        this.tier3Items = tier3Items;
    }

    public Profession getLumberjack() {
        return Lumberjack;
    }

    public void setLumberjack(Profession lumberjack) {
        Lumberjack = lumberjack;
    }
}
