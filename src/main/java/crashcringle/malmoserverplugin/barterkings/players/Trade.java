package crashcringle.malmoserverplugin.barterkings.players;

import org.bukkit.inventory.ItemStack;

public class Trade {

    private ItemStack requestedItem;
    private int requestedAmount;
    private ItemStack offeredItem;
    private int offeredAmount;

    public Trade(ItemStack requestedItem, int requestedAmount, ItemStack offeredItem, int offeredAmount) {
        this.requestedItem = requestedItem;
        this.requestedAmount = requestedAmount;
        this.offeredItem = offeredItem;
        this.offeredAmount = offeredAmount;
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

    public String getRequestedString() {
        return requestedAmount + " " + requestedItem.getType().toString().toLowerCase() + " for " + offeredAmount + " " + offeredItem.getType().toString().toLowerCase();
    }

    public String getOfferString() {
        return offeredAmount + " " + offeredItem.getType().toString().toLowerCase() + " for " + requestedAmount + " " + requestedItem.getType().toString().toLowerCase();
    }


}
