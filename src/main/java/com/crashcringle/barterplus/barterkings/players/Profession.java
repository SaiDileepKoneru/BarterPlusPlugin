package com.crashcringle.barterplus.barterkings.players;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Profession {
    private List<ItemStack> tier1Items;
    private List<ItemStack> tier2Items;
    private List<ItemStack> tier3Items;
    private String name;

    /**
     * Creates a new profession with the specified name and tier 1-3 valued items
     * @param name of the profession
     * @param tier1Items items that are valued at tier 1
     * @param tier2Items items that are valued at tier 2
     * @param tier3Items items that are valued at tier 3
     */
    public Profession(String name, List<ItemStack> tier1Items, List<ItemStack> tier2Items, List<ItemStack> tier3Items) {
        this.name = name;
        this.tier1Items = tier1Items;
        this.tier2Items = tier2Items;
        this.tier3Items = tier3Items;
    }

    /**
     * Gets a list of items that are valued at tier 1
     * @return List of Items
     */
    public List<ItemStack> getTier1Items() {
        return tier1Items;
    }


    public void setTier1Items(List<ItemStack> tier1Items) {
        this.tier1Items = tier1Items;
    }

    public void addTier1Item(ItemStack item) {
        tier1Items.add(item);
    }

    /**
     * Gets a list of items that are valued at tier 2
     * @return List of Items
     */
    public List<ItemStack> getTier2Items() {
        return tier2Items;
    }

    public void setTier2Items(List<ItemStack> tier2Items) {
        this.tier2Items = tier2Items;
    }

    public void addTier2Item(ItemStack item) {
        tier2Items.add(item);
    }

    /**
     * Gets a list of items that are valued at tier 3
     * @return List of Items
     */
    public List<ItemStack> getTier3Items() {
        return tier3Items;
    }

    public void setTier3Items(List<ItemStack> tier3Items) {
        this.tier3Items = tier3Items;
    }

    public void addTier3Item(ItemStack item) {
        tier3Items.add(item);
    }

    /**
     * Gets the name of the profession
     * @return name of the profession
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the profession
     * @param name of the profession
     */
    public void setName(String name) {
        this.name = name;
    }



}
