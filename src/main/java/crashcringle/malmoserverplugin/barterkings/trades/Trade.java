package crashcringle.malmoserverplugin.barterkings.trades;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Trade {

    private ItemStack requestedItem;
    private int requestedAmount;
    private ItemStack offeredItem;
    private int offeredAmount;

    private List<ItemStack> requestedItems;
    private List<ItemStack> offeredItems;

    public Trade(ItemStack offeredItem, int offeredAmount, ItemStack requestedItem, int requestedAmount) {
        this.offeredItem = offeredItem;
        this.offeredAmount = offeredAmount;
        this.requestedItem = requestedItem;
        this.requestedAmount = requestedAmount;
    }

    public Trade(List<ItemStack> offeredItems, List<ItemStack> requestedItems) {
        this.requestedItems = requestedItems;
        this.offeredItems = offeredItems;
    }

    public Trade(ItemStack offeredItemStack, int requestedAmount, ItemStack requestedItem) {
        this.offeredItem = offeredItemStack;
        this.offeredAmount = offeredItemStack.getAmount();
        this.requestedItem = requestedItem;
        this.requestedAmount = requestedAmount;
    }

    public ItemStack getRequestedItem() {
        return requestedItem;
    }

    public void setRequestedItem(ItemStack requestedItem) {
        this.requestedItem = requestedItem;
    }

    public int getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(int requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public ItemStack getOfferedItem() {
        return offeredItem;
    }

    public void setOfferedItem(ItemStack offeredItem) {
        this.offeredItem = offeredItem;
    }

    public int getOfferedAmount() {
        return offeredAmount;
    }

    public void setOfferedAmount(int offeredAmount) {
        this.offeredAmount = offeredAmount;
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
    
    /**
     * Returns a string representation of the trade
     * @return
     */
    public String getOfferString() {
        return requestedAmount + " " + ChatColor.YELLOW + requestedItem.getType().toString() + ChatColor.GREEN + " for " + offeredAmount + " " + offeredItem.getType().toString();
    }

    /**
     * Returns a string representation of the trade
     * 
     * @return
     */
    @Override
    public String toString() {
        String str = "";
        str += "Offered: " + getOfferedItemsString() + "\nfor\n" + getRequestedItemsString();
        return str;
    }

    public String getOfferedItemsString() {
        String str = "" + ChatColor.GREEN;
        for (ItemStack item : offeredItems) {
            str += item.getAmount() + " " + fm(item.getType()) + ", ";
        }
        return str;
    }

    public String getRequestedItemsString() {
        String str = "" + ChatColor.BLUE;
        for (ItemStack item : requestedItems) {
            str += item.getAmount() + " " + fm(item.getType()) + ", ";
        }
        return str;
    }

    public List<ItemStack> getRequestedItems() {
        return requestedItems;
    }

    public void setRequestedItems(List<ItemStack> requestedItems) {
        this.requestedItems = requestedItems;
    }

    public List<ItemStack> getOfferedItems() {
        return offeredItems;
    }

    public void setOfferedItems(List<ItemStack> offeredItems) {
        this.offeredItems = offeredItems;
    }

    /**
     * Returns the trade as a string
     * OfferedItem:OfferedAmount:RequestedItem:RequestedAmount
     * @return
     */
    public String getOfferedString() {
        return offeredAmount + " " + ChatColor.YELLOW + offeredItem.getType().toString() + ChatColor.GREEN + " for " + requestedAmount + " " + requestedItem.getType().toString();
    }


}
