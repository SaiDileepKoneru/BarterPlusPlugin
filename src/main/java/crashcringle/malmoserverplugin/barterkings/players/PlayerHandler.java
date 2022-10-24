package crashcringle.malmoserverplugin.barterkings.players;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PlayerHandler {

    private static List<Participant> participants = new ArrayList<>();

    Profession Farmer;
    Profession Blacksmith;
    Profession Fisherman;
    Profession Butcher;
    Profession Leatherworker;
    Profession Mason;
    Profession Shepherd;
    Profession Deceiver;

    public void Setup() {

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

        Farmer = new Profession("Farmer", tier1, tier2, tier3);

        List<ItemStack> tier1Fisher = new ArrayList<>();
        tier1Fisher.add(new ItemStack(Material.COD));
        tier1Fisher.add(new ItemStack(Material.SALMON));
        tier1Fisher.add(new ItemStack(Material.TROPICAL_FISH));

        List<ItemStack> tier2Fisher = new ArrayList<>();
        tier2Fisher.add(new ItemStack(Material.TADPOLE_BUCKET));

        List<ItemStack> tier3Fisher = new ArrayList<>();
        tier3Fisher.add(new ItemStack(Material.OAK_BOAT));

        Fisherman = new Profession("Fisher", tier1Fisher, tier2Fisher, tier3Fisher);

        List<ItemStack> tier1Butcher = new ArrayList<>();
        tier1Butcher.add(new ItemStack(Material.BEEF));
        tier1Butcher.add(new ItemStack(Material.CHICKEN));
        tier1Butcher.add(new ItemStack(Material.PORKCHOP));

        List<ItemStack> tier2Butcher = new ArrayList<>();
        tier2Butcher.add(new ItemStack(Material.COOKED_BEEF));
        tier2Butcher.add(new ItemStack(Material.COOKED_CHICKEN));
        tier2Butcher.add(new ItemStack(Material.COOKED_PORKCHOP));

        List<ItemStack> tier3Butcher = new ArrayList<>();
        tier3Butcher.add(new ItemStack(Material.RABBIT_STEW));

        Butcher = new Profession("Butcher", tier1Butcher, tier2Butcher, tier3Butcher);

        List<ItemStack> tier1Blacksmith = new ArrayList<>();
        tier1Blacksmith.add(new ItemStack(Material.IRON_INGOT));
        tier1Blacksmith.add(new ItemStack(Material.GOLD_INGOT));
        tier1Blacksmith.add(new ItemStack(Material.COAL));

        List<ItemStack> tier2Blacksmith = new ArrayList<>();
        tier2Blacksmith.add(new ItemStack(Material.IRON_SWORD));
        tier2Blacksmith.add(new ItemStack(Material.GOLDEN_SWORD));

        List<ItemStack> tier3Blacksmith = new ArrayList<>();
        tier3Blacksmith.add(new ItemStack(Material.DIAMOND_SWORD));

        Blacksmith = new Profession("Blacksmith", tier1Blacksmith, tier2Blacksmith, tier3Blacksmith);

        List<ItemStack> tier1Leatherworker = new ArrayList<>();
        tier1Leatherworker.add(new ItemStack(Material.LEATHER));
        tier1Leatherworker.add(new ItemStack(Material.RABBIT_HIDE));
        tier1Leatherworker.add(new ItemStack(Material.LEATHER_CHESTPLATE));
        tier1Leatherworker.add(new ItemStack(Material.LEATHER_BOOTS));
        tier1Leatherworker.add(new ItemStack(Material.LEATHER_LEGGINGS));
        tier1Leatherworker.add(new ItemStack(Material.LEATHER_HELMET));

        List<ItemStack> tier2Leatherworker = new ArrayList<>();
        tier2Leatherworker.add(new ItemStack(Material.SADDLE));

        List<ItemStack> tier3Leatherworker = new ArrayList<>();
        tier3Leatherworker.add(new ItemStack(Material.LEATHER_HORSE_ARMOR));

        Leatherworker = new Profession("Leatherworker", tier1Leatherworker, tier2Leatherworker, tier3Leatherworker);

        List<ItemStack> tier1Mason = new ArrayList<>();
        tier1Mason.add(new ItemStack(Material.STONE));
        tier1Mason.add(new ItemStack(Material.TERRACOTTA));
        tier1Mason.add(new ItemStack(Material.GLASS));

        List<ItemStack> tier2Mason = new ArrayList<>();
        tier2Mason.add(new ItemStack(Material.GREEN_GLAZED_TERRACOTTA));
        tier2Mason.add(new ItemStack(Material.RED_GLAZED_TERRACOTTA));
        tier2Mason.add(new ItemStack(Material.BLUE_GLAZED_TERRACOTTA));
        tier2Mason.add(new ItemStack(Material.WHITE_GLAZED_TERRACOTTA));

        List<ItemStack> tier3Mason = new ArrayList<>();
        tier3Mason.add(new ItemStack(Material.QUARTZ));

        Mason = new Profession("Mason", tier1Mason, tier2Mason, tier3Mason);

        List<ItemStack> tier1Shepherd = new ArrayList<>();
        tier1Shepherd.add(new ItemStack(Material.STRING));
        tier1Shepherd.add(new ItemStack(Material.WHITE_WOOL));
        tier1Shepherd.add(new ItemStack(Material.WHITE_DYE));

        List<ItemStack> tier2Shepherd = new ArrayList<>();
        tier2Shepherd.add(new ItemStack(Material.SHEARS));

        List<ItemStack> tier3Shepherd = new ArrayList<>();
        tier3Shepherd.add(new ItemStack(Material.SHEEP_SPAWN_EGG));

        Shepherd = new Profession("Shepherd", tier1Shepherd, tier2Shepherd, tier3Shepherd);





    }


}
