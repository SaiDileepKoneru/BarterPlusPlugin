package crashcringle.malmoserverplugin.barterkings.trades;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.hibernate.SessionFactory;

import crashcringle.malmoserverplugin.MalmoServerPlugin;
import crashcringle.malmoserverplugin.TradeMenu;
import crashcringle.malmoserverplugin.barterkings.trades.TradeController.RequestStatus;
import java.sql.Timestamp;
import java.util.logging.Level;


public class TradeRequest {

    private Player requester;
    private Player requested;
    private boolean accepted = false;
    private Trade trade;
    private boolean completed = false;

    private TradeMenu tradeMenu;
    
    private boolean cancelled = false;
    private RequestStatus requestStatus = RequestStatus.PENDING;
    private Timestamp beginTimestamp;
    private int gameID = 0;
    private Timestamp finishedTimestamp;

    String requestID = "";

    public TradeRequest(Player requester, Player requested, Trade trade) {
        this.requester = requester;
        this.requested = requested;
        this.trade = trade;
        this.beginTimestamp = new Timestamp(System.currentTimeMillis());
        this.requestID = requester.getName() + requested.getName() + beginTimestamp.getTime();
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "New Trade via Cmd: " + requestID + "| " + requester.getName() + "---> " + requested.getName() + ": " + trade.getOfferString());
        this.requestID = requester.getName() + requested.getName() + beginTimestamp.toString();
    }

    public TradeRequest(Player requester, Player requested) {
        this.requester = requester;
        this.requested = requested;
        this.beginTimestamp = new Timestamp(System.currentTimeMillis());
        this.requestID = requester.getName() + requested.getName() + beginTimestamp.getTime();
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "New Trade via Menu: " + requestID + "| " + requester.getName() + "---> " + requested.getName());
        this.createTradeMenu();

    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public String getRequestID() {
        return this.requestID;
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

    public int getGameID() {
        return gameID;
    }

    public void setGameID(int id) {
        this.gameID = id;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
        if (accepted) {
            MalmoServerPlugin.inst().getLogger().log(Level.INFO, requested.getName() + " has accepted a tradeRequest: " + requestID +" with " + requester.getName());
        } else {
            MalmoServerPlugin.inst().getLogger().log(Level.INFO, requested.getName() + " has denied a tradeRequest: " + requestID +" with " + requester.getName());
        }
        this.completed = true;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed) {
            requestStatus = accepted ? RequestStatus.ACCEPTED : cancelled ? RequestStatus.CANCELLED : RequestStatus.DECLINED;
            MalmoServerPlugin.inst().getLogger().log(Level.INFO, requester.getName() + " has successfully completed tradeRequest: " + requestID + " with " + requested.getName() + " for: " + trade.getOfferString());
            this.finishedTimestamp = new Timestamp(System.currentTimeMillis());
        }
    }

    public Trade getTrade() {
        return trade;
    }

    public void setTrade(Trade trade) {
        this.trade = trade;
    }


    public Timestamp getBeginTime() {
        return beginTimestamp;
    }

    public void setBeginTime(Timestamp timestamp) {
        this.beginTimestamp = timestamp;
    }

    public Timestamp getFinishTime() {
        return finishedTimestamp;
    }

    public void setFinishTime(Timestamp timestamp) {
        this.finishedTimestamp = timestamp;
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
            if (this.tradeMenu != null) {
                tradeMenu.getMenu().close();
                //tradeMenu.getMenu().close(requester);
                //tradeMenu.getMenu().close(requested);
                // Loop through requested items and give them to the requester
                for (ItemStack item : trade.getRequestedItems()) {
                    requester.getInventory().addItem(item);
                    // Remove the item from the requested player's inventory
                    //requested.getInventory().removeItem(item);
                }
                // Loop through offered items and give them to the requested
                for (ItemStack item : trade.getOfferedItems()) {
                    requested.getInventory().addItem(item);
                    // Remove the item from the requester's inventory
                    //requester.getInventory().removeItem(item);
                }
                this.setAccepted(true);
                sendMessage(ChatColor.GOLD + "Trade completed!");

            // First check if the requested item is valid
            } else if (trade.getRequestedItem() != null) {
                // Then check if the offered item is valid
                if (trade.getOfferedItem() != null) {
                    // Then check if the requesting player has the requested item
                    if (requested.getInventory().containsAtLeast(trade.getRequestedItem(), trade.getRequestedAmount())) {
                        // Then check if the requester has the offered item
                        if (requester.getInventory().containsAtLeast(trade.getOfferedItem(), trade.getOfferedAmount())) {
                            this.setAccepted(true);
                            requested.getInventory().removeItem(new ItemStack(trade.getRequestedItem().getType(), trade.getRequestedAmount()));
                            requester.getInventory().removeItem(new ItemStack(trade.getOfferedItem().getType(), trade.getOfferedAmount()));
                            requested.getInventory().addItem(new ItemStack(trade.getOfferedItem().getType(), trade.getOfferedAmount()));
                            requester.getInventory().addItem(new ItemStack(trade.getRequestedItem().getType(), trade.getRequestedAmount()));
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


    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        this.setCompleted(true);
    }

    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    // Method that returns the time in seconds since the request was made
    public long getSecondsSinceRequest() {
        return (System.currentTimeMillis() - beginTimestamp.getTime()) / 1000;
    }

    // Method that returns the time in minutes since the request was made
    public long getMinutesSinceRequest() {
        return getSecondsSinceRequest() / 60;
    }

    // Method that returns a  string representation of the trade request
    public String toString() {
        String str = requester.getName() + " requested a trade with " + requested.getName() + " at " + beginTimestamp.toString();
        if (hasMenu()) {
            // What the trade was for
            str += " Offering " + trade.getOfferedItemsString() + " for " + trade.getRequestedItemsString();
        } else {
            str += " Offering " + trade.getOfferedAmount() + " " + trade.getOfferedItem().getType().toString() + " for " + trade.getRequestedAmount() + " " + trade.getRequestedItem().getType().toString();
        }

        return str;


    }
    public TradeMenu getTradeMenu() {
        return tradeMenu;
    }
    
    public boolean hasMenu() {
        return tradeMenu != null;
    }

    public void createTradeMenu() {
        this.tradeMenu = new TradeMenu(requester, requested, this);
    }

    public void completeTradeMenu() {
        if (tradeMenu.isPlayer1Ready() && tradeMenu.isPlayer2Ready()) {
            tradeMenu.getPlayer1Items().clear();
            tradeMenu.getPlayer2Items().clear();
            for (int i = 0; i < tradeMenu.getPlayer1Slots().size(); i++) {
                // Check that there is an item in the slot
                if (tradeMenu.getPlayer1Slots().get(i).getRawItem(tradeMenu.getPlayer1()) != null) {
                    tradeMenu.getPlayer1Items().add(tradeMenu.getPlayer1Slots().get(i).getRawItem(tradeMenu.getPlayer1()));
                }
            }
            for (int i = 0; i < tradeMenu.getPlayer2Slots().size(); i++) {
                // Check that there is an item in the slot
                if (tradeMenu.getPlayer2Slots().get(i).getRawItem(tradeMenu.getPlayer2()) != null) {
                    tradeMenu.getPlayer2Items().add(tradeMenu.getPlayer2Slots().get(i).getRawItem(tradeMenu.getPlayer2()));
                }
            }
            if (tradeMenu.getPlayer1Items().size() == 0 || tradeMenu.getPlayer2Items().size() == 0) {
                tradeMenu.getPlayer1().sendMessage(ChatColor.RED + "You must offer at least one item!");
                tradeMenu.getPlayer2().sendMessage(ChatColor.RED + "You must offer at least one item!");
                return;
            } else {
                this.setTrade(new Trade(tradeMenu.getPlayer1Items(), tradeMenu.getPlayer2Items()));
                this.accept();
                this.setCompleted(true);
                tradeMenu.getPlayer1().playSound(tradeMenu.getPlayer1().getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 10, 2F);
                tradeMenu.getPlayer1().sendMessage("You have accepted the trade!");
                tradeMenu.getPlayer2().sendMessage("You have accepted the trade!");
            }
        } else {
            // If neither player is ready play the villager sound at the player's locatiion
            tradeMenu.getPlayer1().playSound(tradeMenu.getPlayer1().getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 0.4F);
            tradeMenu.getPlayer2().playSound(tradeMenu.getPlayer2().getLocation(), Sound.ENTITY_VILLAGER_NO, 10, 0.4F);
        }
    }

    public void setTradeMenu(TradeMenu tradeMenu) {
        this.tradeMenu = tradeMenu;
    }

    public void setRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

}
