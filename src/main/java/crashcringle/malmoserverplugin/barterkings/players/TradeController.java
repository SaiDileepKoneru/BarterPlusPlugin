package crashcringle.malmoserverplugin.barterkings.players;

import crashcringle.malmoserverplugin.MalmoServerPlugin;
import net.md_5.bungee.chat.SelectorComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Level;

public class TradeController {
    public static Map<Player, ArrayList<TradeRequest>> outgoingRequests;
    public static Map<Player, ArrayList<TradeRequest>> incomingRequests;

    public static ArrayList<Map<Player, TradeRequest>> staleRequests;
    public TradeController() {
        outgoingRequests = new HashMap<>();
        incomingRequests = new HashMap<>();
    }

    public static void sendTradeRequest(Player requester, Player requested, Trade trade) {
        if (!hasActiveTradeRequest(requester, requested)) {
            addTradeRequest(new TradeRequest(requester, requested, trade));
            MalmoServerPlugin.inst().getLogger().log(Level.INFO, requester.getName() + " has requested a trade with " + requested.getName());
            requested.sendMessage(ChatColor.GREEN + requester.getName() + " has requested a trade with you");
            requested.sendMessage(ChatColor.GREEN + "They are offering " + trade.getOfferString());
            requested.sendMessage(ChatColor.GREEN + "Type /barter acceptTrade " + requester.getName() + " to accept the trade");
            requested.sendMessage(ChatColor.GREEN + "Type /barter denyTrade " + requester.getName() + " to deny the trade");
            requester.sendMessage(ChatColor.GREEN + "You have requested a trade with " + requested.getName());
            requester.sendMessage(ChatColor.GREEN + "You are offering " + trade.getRequestedString());
            requester.sendMessage(ChatColor.GREEN + "Type /barter cancelTrade " + requested.getName() + " to cancel the trade");
        } else {
            requester.sendMessage(ChatColor.RED + "You have already requested an active trade with " + requested.getName());
        }

    }

    public static void acceptTradeRequest(TradeRequest tradeRequest) {
            tradeRequest.setAccepted(true);
            tradeRequest.getRequested().sendMessage(ChatColor.GREEN + "You have accepted the trade request from " + tradeRequest.getRequester().getName());
            tradeRequest.getRequester().sendMessage(ChatColor.GREEN + "Your trade request has been accepted by " + tradeRequest.getRequested().getName());
    }

    public static void declineTradeRequest(TradeRequest tradeRequest) {
            tradeRequest.setAccepted(false);
            tradeRequest.setCompleted(true);
            tradeRequest.getRequested().sendMessage(ChatColor.RED + "You have declined the trade request from " + tradeRequest.getRequester().getName());
            tradeRequest.getRequester().sendMessage(ChatColor.RED + "Your trade request has been declined by " + tradeRequest.getRequested().getName());
    }

    public static  void completeTradeRequest(TradeRequest tradeRequest) {
            tradeRequest.setCompleted(true);
            tradeRequest.getRequested().sendMessage(ChatColor.GREEN + "You have completed the trade request from " + tradeRequest.getRequester().getName());
            tradeRequest.getRequester().sendMessage(ChatColor.GREEN + "Your trade request has been completed by " + tradeRequest.getRequested().getName());
    }

    public static void cancelTradeRequest(TradeRequest tradeRequest) {
            tradeRequest.setCompleted(true);
            tradeRequest.getRequester().sendMessage(ChatColor.RED + "You have cancelled the trade request to " + tradeRequest.getRequested().getName());
            tradeRequest.getRequested().sendMessage(ChatColor.RED + "Your trade request has been cancelled by " + tradeRequest.getRequester().getName());
    }

    /**
     * Denies the latest trade request to a player
     * @param player
     */
    public static void denyRecentTrade(Player player) {

        if (incomingRequests.containsKey(player)) {
            TradeRequest request = Collections.max(incomingRequests.get(player), Comparator.comparing(TradeRequest::getTimestamp));
            request.setCompleted(true);
            request.setAccepted(false);
            request.getRequester().sendMessage(ChatColor.RED + "Your trade request has been denied by " + request.getRequested().getName());
            request.getRequested().sendMessage(ChatColor.RED + "You have denied the trade request from " + request.getRequester().getName());
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
                    request.setCompleted(true);
                    request.setAccepted(false);
                    request.getRequester().sendMessage(ChatColor.RED + "Your trade request has been denied by " + request.getRequested().getName());
                    request.getRequested().sendMessage(ChatColor.RED + "You have denied the trade request from " + request.getRequester().getName());
                }
            }
        }
    }

    public static void acceptRecentTrade(Player player) {


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



}
