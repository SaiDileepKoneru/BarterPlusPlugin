package crashcringle.malmoserverplugin.barterkings.trades;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class Trade {

    private ItemStack requestedItem;
    private int requestedAmount;
    private ItemStack offeredItem;
    private int offeredAmount;

    public Trade(ItemStack offeredItem, int offeredAmount, ItemStack requestedItem, int requestedAmount) {
        this.offeredItem = offeredItem;
        this.offeredAmount = offeredAmount;
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

    /**
     * Returns a string representation of the trade
     * @return
     */
    public String getOfferString() {
        return requestedAmount + " " + ChatColor.YELLOW + requestedItem.getType().toString() + ChatColor.GREEN + " for " + offeredAmount + " " + offeredItem.getType().toString();
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
