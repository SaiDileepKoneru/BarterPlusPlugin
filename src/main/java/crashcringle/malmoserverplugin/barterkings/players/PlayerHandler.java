package crashcringle.malmoserverplugin.barterkings.players;

import crashcringle.malmoserverplugin.MalmoServerPlugin;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayerHandler {

    private static List<Participant> participants;

    private Profession Farmer;
    private Profession Blacksmith;
    private Profession Fisherman;
    private Profession Butcher;
    private Profession Leatherworker;
    private Profession Mason;
    private Profession Shepherd;
    private Profession Deceiver;

    private ArrayList<ItemStack> tier1Items = new ArrayList<>();
    private ArrayList<ItemStack> tier2Items = new ArrayList<>();
    private ArrayList<ItemStack> tier3Items = new ArrayList<>();

    public void setParticipants(List<Participant> participants) {
        PlayerHandler.participants = participants;
    }

    public void Checkup() {
        if (allReady()) {
            setUpProfessions();
            setUpParticipants();
            findAllActiveTierItems();
            distributeItems();
            teleportPlayers();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "bbt begin 45m barterKings white &6&lBarter Kings! &e&l<minutes> &6minutes and &e&l<seconds> &6seconds left!");
        } else {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Not all players are ready!");
        }

    }
    public PlayerHandler() {
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
        Score score1 = objective.getScore(ChatColor.GOLD + "Trading Goals + Amounts in World!");
        score1.setScore(1);
        // Score score2 = objective.getScore(ChatColor.GOLD + "Collect them all!");

        for (ItemStack item : profession.getTier1Items()) {
            Score score = objective.getScore(ChatColor.GREEN + fm(item.getType()) + " " + ChatColor.YELLOW + "x20" );
            score.setScore(0);

        }
        for (ItemStack item : profession.getTier2Items()) {
            Score score = objective.getScore(ChatColor.DARK_GREEN + fm(item.getType()) + " " + ChatColor.YELLOW + "x10" );
            score.setScore(0);
        }
        for (ItemStack item : profession.getTier3Items()) {
            Score score = objective.getScore(ChatColor.AQUA + fm(item.getType()) + " " + ChatColor.YELLOW + "x3" );
            score.setScore(0);
        }
        return scoreboard;
    }
    public void teleportPlayers() {
        for (Participant participant : participants) {
            participant.getPlayer().setScoreboard(createScoreboard(participant.getProfession()));
            participant.getPlayer().teleport(new Location(Bukkit.getWorld("world"), -704 + Math.random()+5, 73, 71 + Math.random()+5));
        }
    }



    public void addParticipant(Player player) {
        getParticipants().add(new Participant(player));
    }

    public void attemptStart() {
        Checkup();
    }
    public void removeParticipant(Player player) {
        getParticipants().remove(new Participant(player));
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
            MalmoServerPlugin.inst().getLogger().info("Player " + player.getName() + " has been assigned the profession " + participant.getProfession().getName());
            getParticipants().add(participant);
        }
    }

    public void setUpParticipants() {
        for (Participant participant : getParticipants()) {
            participant.setProfession(getRandomProfession());
            MalmoServerPlugin.inst().getLogger().info("Player " + participant.getPlayer().getName() + " has been assigned the profession " + participant.getProfession().getName());
        }
    }

    public void distributeItems() {
        distributeTier1Items();
        distributeTier2Items();
        distributeTier3Items();

    }
    public void distributeTier1Items() {
        int randomIndex;
        for (ItemStack item : getTier1Items()) {
            int amt = 20;
            while (amt > 0) {
                int amtToGive = amt >= 2 ? ((int) (Math.random() * amt)) : 1;
                randomIndex =  (int) (Math.random() * getParticipants().size());
                item.setAmount(amtToGive);
                getParticipants().get(randomIndex).getPlayer().getInventory().addItem(item);
                MalmoServerPlugin.inst().getLogger().info("Tier 1 | Giving " + getParticipants().get(randomIndex).getPlayer().getName() + " " + amtToGive + " " + item.getType());
                amt = amt - amtToGive;
            }
        }
    }

    public void distributeTier2Items() {
        int randomIndex;
        for (ItemStack item : getTier2Items()) {
            int amt = 10;
            while (amt > 0) {
                int amtToGive = amt >= 2 ? ((int) (Math.random() * amt)) : 1;
                randomIndex =  (int) (Math.random() * getParticipants().size());
                item.setAmount(amtToGive);
                getParticipants().get(randomIndex).getPlayer().getInventory().addItem(item);
                MalmoServerPlugin.inst().getLogger().info("Tier 2 | Giving " + getParticipants().get(randomIndex).getPlayer().getName() + " " + amtToGive + " " + item.getType());
                amt = amt - amtToGive;
            }
        }
    }

    public void distributeTier3Items() {
        int randomIndex;
        for (ItemStack item : getTier3Items()) {
            int amt = 3;
            while (amt > 0) {
                int amtToGive = amt != 1 ? ((int) (Math.random() * amt)) : 1;
                randomIndex =  (int) (Math.random() * getParticipants().size());
                item.setAmount(amtToGive);
                getParticipants().get(randomIndex).getPlayer().getInventory().addItem(item);
                MalmoServerPlugin.inst().getLogger().info("Tier 3 | Giving " + getParticipants().get(randomIndex).getPlayer().getName() + " " + amtToGive + " " + item.getType());
                amt = amt - amtToGive;
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

        List<ItemStack> tier3 = new ArrayList<>();
        tier3.add(new ItemStack(Material.MUSHROOM_STEW));

        setFarmer(new Profession("Farmer", tier1, tier2, tier3));

        List<ItemStack> tier1Fisher = new ArrayList<>();
        tier1Fisher.add(new ItemStack(Material.COD));
        tier1Fisher.add(new ItemStack(Material.SALMON));
        tier1Fisher.add(new ItemStack(Material.TROPICAL_FISH));

        List<ItemStack> tier2Fisher = new ArrayList<>();
        tier2Fisher.add(new ItemStack(Material.TADPOLE_BUCKET));

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
        tier2Blacksmith.add(new ItemStack(Material.IRON_SWORD));
        tier2Blacksmith.add(new ItemStack(Material.GOLDEN_PICKAXE));

        List<ItemStack> tier3Blacksmith = new ArrayList<>();
        tier3Blacksmith.add(new ItemStack(Material.DIAMOND_SWORD));

        setBlacksmith(new Profession("Blacksmith", tier1Blacksmith, tier2Blacksmith, tier3Blacksmith));

        List<ItemStack> tier1Leatherworker = new ArrayList<>();
        tier1Leatherworker.add(new ItemStack(Material.LEATHER));
        tier1Leatherworker.add(new ItemStack(Material.LEATHER_CHESTPLATE));
        tier1Leatherworker.add(new ItemStack(Material.LEATHER_BOOTS));
        //tier1Leatherworker.add(new ItemStack(Material.LEATHER_LEGGINGS));
        //tier1Leatherworker.add(new ItemStack(Material.LEATHER_HELMET));

        List<ItemStack> tier2Leatherworker = new ArrayList<>();
        tier2Leatherworker.add(new ItemStack(Material.SADDLE));
        tier2Leatherworker.add(new ItemStack(Material.RABBIT_HIDE));

        List<ItemStack> tier3Leatherworker = new ArrayList<>();
        tier3Leatherworker.add(new ItemStack(Material.LEATHER_HORSE_ARMOR));

        setLeatherworker(new Profession("Leatherworker", tier1Leatherworker, tier2Leatherworker, tier3Leatherworker));

        List<ItemStack> tier1Mason = new ArrayList<>();
        tier1Mason.add(new ItemStack(Material.STONE));
        tier1Mason.add(new ItemStack(Material.TERRACOTTA));
        tier1Mason.add(new ItemStack(Material.GLASS));

        List<ItemStack> tier2Mason = new ArrayList<>();
        //tier2Mason.add(new ItemStack(Material.GREEN_GLAZED_TERRACOTTA));
        tier2Mason.add(new ItemStack(Material.RED_GLAZED_TERRACOTTA));
        tier2Mason.add(new ItemStack(Material.BLUE_GLAZED_TERRACOTTA));
        //tier2Mason.add(new ItemStack(Material.WHITE_GLAZED_TERRACOTTA));

        List<ItemStack> tier3Mason = new ArrayList<>();
        tier3Mason.add(new ItemStack(Material.QUARTZ));

        setMason(new Profession("Mason", tier1Mason, tier2Mason, tier3Mason));

        List<ItemStack> tier1Shepherd = new ArrayList<>();
        tier1Shepherd.add(new ItemStack(Material.STRING));
        tier1Shepherd.add(new ItemStack(Material.WHITE_WOOL));
        tier1Shepherd.add(new ItemStack(Material.BLUE_DYE));

        List<ItemStack> tier2Shepherd = new ArrayList<>();
        tier2Shepherd.add(new ItemStack(Material.SHEARS));
        tier2Shepherd.add(new ItemStack(Material.LOOM));

        List<ItemStack> tier3Shepherd = new ArrayList<>();
        tier3Shepherd.add(new ItemStack(Material.SHEEP_SPAWN_EGG));

        setShepherd(new Profession("Shepherd", tier1Shepherd, tier2Shepherd, tier3Shepherd));

    }
    List<Integer> used = new ArrayList<>();
    public Profession getRandomProfession() {
        int random = (int) (Math.random() * 7);
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
            default:
                return getFarmer();
        }
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
}
