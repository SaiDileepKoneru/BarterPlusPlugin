package crashcringle.malmoserverplugin.barterkings.players;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Timestamp;


public class TradeRequest {

    private Player requester;
    private Player requested;
    private boolean accepted;
    private Trade trade;
    private boolean completed;

    private Timestamp timestamp;
    public TradeRequest(Player requester, Player requested, Trade trade) {
        this.requester = requester;
        this.requested = requested;
        this.trade = trade;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    public Player getRequester() {
        return requester;
    }

    public void setRequester(Player requester) {
        this.requester = requester;
    }

    public Player getRequested() {
        return requested;
    }

    public void setRequested(Player requested) {
        this.requested = requested;
    }


    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Trade getTrade() {
        return trade;
    }

    public void setTrade(Trade trade) {
        this.trade = trade;
    }


    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
    public void sendMessage(String message) {
        requester.sendMessage(message);
        requested.sendMessage(message);
    }

    public void accept() {
        // First check if the requested item is valid
        if (trade.getRequestedItem() != null) {
            // Then check if the offered item is valid
            if (trade.getOfferedItem() != null) {
                // Then check if the requested player has the requested item
                if (requested.getInventory().containsAtLeast(trade.getOfferedItem(), trade.getOfferedAmount())) {
                    // Then check if the requester has the offered item
                    if (requester.getInventory().containsAtLeast(trade.getRequestedItem(), trade.getRequestedAmount())) {
                        requester.getInventory().removeItem(new ItemStack(trade.getRequestedItem().getType(), trade.getRequestedAmount()));
                        requested.getInventory().removeItem(new ItemStack(trade.getOfferedItem().getType(), trade.getOfferedAmount()));
                        requester.getInventory().addItem(new ItemStack(trade.getOfferedItem().getType(), trade.getOfferedAmount()));
                        requested.getInventory().addItem(new ItemStack(trade.getRequestedItem().getType(), trade.getRequestedAmount()));
                        completed = true;
                        sendMessage(ChatColor.GREEN + "Trade completed!");

                    } else {
                        sendMessage(ChatColor.DARK_RED + "The requester does not have the requested item!");
                    }
                } else {
                    sendMessage(ChatColor.DARK_RED + "The requested player does not have the offered item!");
                }
            } else {
                sendMessage(ChatColor.DARK_RED + "The offered item is not valid!");
            }
        } else {
            sendMessage(ChatColor.DARK_RED + "The requested item is not valid!");
        }
    }

    private void takeItem(Player requester) {
        for (ItemStack itemStack : requester.getInventory().getContents()) {
            if (itemStack != null && itemStack.isSimilar(trade.getRequestedItem())) {
                if (itemStack.getAmount() >= trade.getRequestedAmount()) {
                    itemStack.setAmount(itemStack.getAmount() - trade.getRequestedAmount());
                    break;
                }
            }
        }
    }


}
