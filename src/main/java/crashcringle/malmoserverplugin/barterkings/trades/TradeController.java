package crashcringle.malmoserverplugin.barterkings.trades;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class TradeController {
    public static Map<Player, ArrayList<TradeRequest>> outgoingRequests;
    public static Map<Player, ArrayList<TradeRequest>> incomingRequests;

    public static ArrayList<Map<Player, TradeRequest>> staleRequests;
    public enum RequestStatus {
        PENDING,
        ACCEPTED,
        DECLINED,
        CANCELLED
    };
    public TradeController() {
        outgoingRequests = new HashMap<>();
        incomingRequests = new HashMap<>();
    }

    public static void sendTradeRequest(Player requester, Player requested, Trade trade) {
        if (!hasActiveTradeRequest(requester, requested)) {
            addTradeRequest(new TradeRequest(requester, requested, trade));
            requested.sendMessage(ChatColor.AQUA + requester.getName() + " has requested a trade with you");
            requested.sendMessage(ChatColor.GREEN + "They want to trade " + trade.getOfferString());
            requested.sendMessage(ChatColor.AQUA + "Type /barter accept " + ChatColor.BOLD + requester.getName() + ChatColor.AQUA + " to accept the trade");
            requested.sendMessage(ChatColor.RED + "Type /barter deny " + ChatColor.BOLD + requester.getName() + ChatColor.RED + " to deny the trade");
            requester.sendMessage(ChatColor.AQUA + "You have requested a trade with " + ChatColor.BOLD + requested.getName());
            requester.sendMessage(ChatColor.GREEN + "You are trading " + trade.getOfferedString());
            requester.sendMessage(ChatColor.RED + "Type /barter cancel " + ChatColor.BOLD + requested.getName() + ChatColor.RED + " to cancel the trade");
        } else {
            requester.sendMessage(ChatColor.DARK_RED + "You have already requested an active trade with " + requested.getName());
        }

    }

    public static void acceptTradeRequest(TradeRequest tradeRequest) {
            tradeRequest.accept();
    }

    public static void acceptRecentTrade(Player player) {
        if (incomingRequests.containsKey(player)) {
            TradeRequest request = Collections.max(incomingRequests.get(player), Comparator.comparing(TradeRequest::getTimestamp));
            acceptTradeRequest(request);
        } else {
            player.sendMessage(ChatColor.DARK_RED + "You have no recent trade requests");
        }
    }

    public static void acceptTrade(Player requested, Player requester) {
        if (incomingRequests.containsKey(requested)) {
            for (TradeRequest request : incomingRequests.get(requested)) {
                if (request.getRequester().equals(requester) && !request.isCompleted()) {
                    acceptTradeRequest(request);
                } else {
                    requested.sendMessage(ChatColor.DARK_RED + "You have no active trade requests from " + requester.getName());
                }
            }
        } else {
            requested.sendMessage(ChatColor.DARK_RED + "You have no trade requests from " + requester.getName());
        }
    }

    public static void declineTradeRequest(TradeRequest tradeRequest) {
        if (!tradeRequest.isCompleted()) {
            tradeRequest.setAccepted(false);
            tradeRequest.getRequested().sendMessage(ChatColor.DARK_RED + "You have declined the trade request from " + tradeRequest.getRequester().getName());
            tradeRequest.getRequester().sendMessage(ChatColor.DARK_RED + "Your trade request has been declined by " + tradeRequest.getRequested().getName());
        } else {
            tradeRequest.getRequested().sendMessage(ChatColor.GOLD + "You have already accepted or declined this trade request");
        }
    }

    /**
     * Denies the latest trade request to a player
     * @param player
     */
    public static void denyRecentTrade(Player player) {

        if (incomingRequests.containsKey(player)) {
            TradeRequest request = Collections.max(incomingRequests.get(player), Comparator.comparing(TradeRequest::getTimestamp));
            declineTradeRequest(request);
        } else {
            player.sendMessage(ChatColor.DARK_RED + "You have no recent active trade requests");
        }
    }

    /**
     * Denies the trade from a player
     * @param requested
     * @param requester
     */
    public static void denyTrade(Player requested, Player requester) {
        if (incomingRequests.containsKey(requested)) {
            for (TradeRequest request : incomingRequests.get(requested)) {
                if (request.getRequester().equals(requester) && !request.isCompleted()) {
                    declineTradeRequest(request);
                    return;
                } 
            }
            requested.sendMessage(ChatColor.DARK_RED + "You have no active trade requests from " + requester.getName());
        } else {
            requested.sendMessage(ChatColor.DARK_RED + "You have never received a trade from " + requester.getName());
        }
    }
    public static void completeTradeRequest(TradeRequest tradeRequest) {
            tradeRequest.setCompleted(true);
            tradeRequest.getRequested().sendMessage(ChatColor.GREEN + "You have completed the trade request from " + tradeRequest.getRequester().getName());
            tradeRequest.getRequester().sendMessage(ChatColor.GREEN + "Your trade request has been completed by " + tradeRequest.getRequested().getName());
    }

    public static void cancelTradeRequest(TradeRequest tradeRequest) {
        if (!tradeRequest.isCompleted()) {
            tradeRequest.setCancelled(true);
            tradeRequest.getRequester().sendMessage(ChatColor.DARK_RED + "You have cancelled the trade request to " + tradeRequest.getRequested().getName());
            tradeRequest.getRequested().sendMessage(ChatColor.DARK_RED + "Your trade request has been cancelled by " + tradeRequest.getRequester().getName());
       } else {
            tradeRequest.getRequester().sendMessage(ChatColor.GOLD + "You have already completed or cancelled this trade request");
        }
    }

    public static void cancelRecentTrade(Player player) {
        if (outgoingRequests.containsKey(player)) {
            TradeRequest request = Collections.max(outgoingRequests.get(player), Comparator.comparing(TradeRequest::getTimestamp));
            cancelTradeRequest(request);
        } else {
            player.sendMessage(ChatColor.DARK_RED + "You have not made any outgoing trade requests");
        }
    }

    public static void cancelTrade(Player requester, Player requested) {
        if (outgoingRequests.containsKey(requester)) {
            for (TradeRequest request : outgoingRequests.get(requester)) {
                if (request.getRequested().equals(requested) && !request.isCompleted()) {
                    cancelTradeRequest(request);
                    return;
                }
            }
            requester.sendMessage(ChatColor.DARK_RED + "You have no active trade requests to " + requested.getName());
        } else {
            requester.sendMessage(ChatColor.DARK_RED + "You have never sent a trade to " + requested.getName());
        }
    }
    /**
     * Adds the trade request to the list of trade requests
     * @param tradeRequest
     */
    private static void addTradeRequest(TradeRequest tradeRequest) {
        if (outgoingRequests.containsKey(tradeRequest.getRequester())) {
            outgoingRequests.get(tradeRequest.getRequester()).add(tradeRequest);
        } else {
            ArrayList<TradeRequest> requests = new ArrayList<>();
            requests.add(tradeRequest);
            outgoingRequests.put(tradeRequest.getRequester(), requests);
        }

        if (incomingRequests.containsKey(tradeRequest.getRequested())) {
            incomingRequests.get(tradeRequest.getRequested()).add(tradeRequest);
        } else {
            ArrayList<TradeRequest> requests = new ArrayList<>();
            requests.add(tradeRequest);
            incomingRequests.put(tradeRequest.getRequested(), requests);
        }
//        outgoingRequests.computeIfPresent(tradeRequest.getRequester(), (k, v) -> {
//            v.add(tradeRequest);
//            return v;
//        });
//        incomingRequests.computeIfPresent(tradeRequest.getRequested(), (k, v) -> {
//            v.add(tradeRequest);
//            return v;
//        });
//        outgoingRequests.computeIfAbsent(tradeRequest.getRequester(), k -> new ArrayList<>()).add(tradeRequest);
//        incomingRequests.computeIfAbsent(tradeRequest.getRequested(), k -> new ArrayList<>()).add(tradeRequest);


    }

    public static boolean hasActiveTradeRequest(Player requester, Player requested) {
        if (outgoingRequests.containsKey(requester) && incomingRequests.containsKey(requested)) {
            for (TradeRequest tradeRequest : outgoingRequests.get(requester)) {
                if (tradeRequest.getRequested().equals(requested) && !tradeRequest.isCompleted()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static TradeRequest getActiveTradeRequest(Player requester, Player requested) {
        if (outgoingRequests.containsKey(requester) && incomingRequests.containsKey(requested)) {
            for (TradeRequest tradeRequest : outgoingRequests.get(requester)) {
                if (tradeRequest.getRequested().equals(requested) && !tradeRequest.isCompleted()) {
                    return tradeRequest;
                }
            }
        }
        return null;
    }

    public static boolean hasRecentTradeRequest(Player player) {
        return incomingRequests.containsKey(player);
    }

    public static TradeRequest getRecentTradeRequest(Player player) {
        if (incomingRequests.containsKey(player)) {
            return Collections.max(incomingRequests.get(player), Comparator.comparing(TradeRequest::getTimestamp));
        }
        return null;
    }

    //Method to get all the trade requests involving a player
    public static ArrayList<TradeRequest> getAllPlayerTradeRequests(Player player) {
        ArrayList<TradeRequest> requests = new ArrayList<>();
        if (incomingRequests.containsKey(player)) {
            requests.addAll(incomingRequests.get(player));
        }
        if (outgoingRequests.containsKey(player)) {
            requests.addAll(outgoingRequests.get(player));
        }
        return requests;
    }




}
