package crashcringle.malmoserverplugin.barterkings.players;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import crashcringle.malmoserverplugin.MalmoServerPlugin;

import java.sql.Timestamp;
import java.util.logging.Level;


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
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, requester.getName() + " has requested a trade with " + requested.getName());

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
        if (accepted) {
            MalmoServerPlugin.inst().getLogger().log(Level.INFO, requested.getName() + " has accepted a trade with " + requester.getName());
        } else {
            MalmoServerPlugin.inst().getLogger().log(Level.INFO, requested.getName() + " has denied a trade with " + requester.getName());
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed) {
            MalmoServerPlugin.inst().getLogger().log(Level.INFO, requester.getName() + " has successfully completed a trade with " + requested.getName());
        }
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
        if (this.isCompleted()) {
            sendMessage(ChatColor.GOLD + "The trade has already been completed");
        } else {
            getRequested().sendMessage(ChatColor.GREEN + "You have accepted the trade request from " + getRequester().getName());
            getRequester().sendMessage(ChatColor.GREEN + "Your trade request has been accepted by " + getRequested().getName());
            this.setAccepted(true);
            // First check if the requested item is valid
            if (trade.getRequestedItem() != null) {
                // Then check if the offered item is valid
                if (trade.getOfferedItem() != null) {
                    // Then check if the requesting player has the requested item
                    if (requested.getInventory().containsAtLeast(trade.getRequestedItem(), trade.getRequestedAmount())) {
                        // Then check if the requester has the offered item
                        if (requester.getInventory().containsAtLeast(trade.getOfferedItem(), trade.getOfferedAmount())) {
                            requested.getInventory().removeItem(new ItemStack(trade.getRequestedItem().getType(), trade.getRequestedAmount()));
                            requester.getInventory().removeItem(new ItemStack(trade.getOfferedItem().getType(), trade.getOfferedAmount()));
                            requested.getInventory().addItem(new ItemStack(trade.getOfferedItem().getType(), trade.getOfferedAmount()));
                            requester.getInventory().addItem(new ItemStack(trade.getRequestedItem().getType(), trade.getRequestedAmount()));
                            this.setCompleted(true);
                            sendMessage(ChatColor.GOLD + "Trade completed!");
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
            if (!isCompleted()) {
                sendMessage(ChatColor.DARK_RED + "Trade failed!");
                MalmoServerPlugin.inst().getLogger().log(Level.INFO, "A trade has failed between requester: " + requester.getName() + " and requestee: " + requested.getName());
            }
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
