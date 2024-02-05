package crashcringle.malmoserverplugin.barterkings.trades;

import crashcringle.malmoserverplugin.MalmoServerPlugin;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import crashcringle.malmoserverplugin.data.Database;

import java.sql.SQLException;
import java.util.*;

public class TradeController {
    public static Map<String, ArrayList<TradeRequest>> outgoingRequests;
    public static Map<String, ArrayList<TradeRequest>> incomingRequests;

    private static ArrayList<TradeRequest> allRequests;

    public static ArrayList<TradeRequest> getAllRequests() {
        return allRequests;
    }

    public static void setAllRequests(ArrayList<TradeRequest> allRequests) {
        TradeController.allRequests = allRequests;
    }

    public enum RequestStatus {
        PENDING,
        ACCEPTED,
        DECLINED,
        CANCELLED
    };
    public TradeController() {
        outgoingRequests = new HashMap<>();
        incomingRequests = new HashMap<>();
        allRequests = new ArrayList<>();
    }

    public void attemptTradeRequestViaMenu(Player requester, Player requested) {
        if (!hasActiveTradeRequest(requester, requested)) {
            addTradeRequest(new TradeRequest(requester, requested));
            requested.sendMessage(ChatColor.AQUA + requester.getName() + " has requested a trade with you");
        } else {
            if (getActiveTradeRequest(requester, requested).hasMenu()) {
                getActiveTradeRequest(requester, requested).getTradeMenu().displayMenu();
            } else {
                requester.sendMessage(ChatColor.DARK_RED + "You have already requested an active trade with " + requested.getName());
            }
        }
    }
    public static TradeRequest sendTradeRequest(Player requester, Player requested, Trade trade) {
        if (!hasActiveTradeRequest(requester, requested)) {
            TradeRequest request = new TradeRequest(requester, requested, trade);
            addTradeRequest(request);
            HoverEvent acceptHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + "Click to Accept!").create());
            HoverEvent denyHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.RED + "Click to Deny").create());
            HoverEvent cancelHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.RED + "Click to Cancel").create());
            ClickEvent acceptClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/barter accept "+requester.getName());
            ClickEvent denyClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/barter deny "+requester.getName());
            ClickEvent cancelClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/barter cancel "+requested.getName());

            TextComponent message = new TextComponent(ChatColor.AQUA + requester.getName() + " has requested a trade with you");
            message.setHoverEvent(acceptHover);
            message.setClickEvent(acceptClick);
            requested.spigot().sendMessage(message);
            requested.sendMessage(ChatColor.GREEN + "They want to trade " + trade.getOfferString());

            message = new TextComponent(ChatColor.GREEN + "" + ChatColor.UNDERLINE + "Click Here" + ChatColor.GREEN  + " or Type /barter accept " + ChatColor.BOLD + requester.getName() + ChatColor.GREEN + " to accept the trade");
            message.setHoverEvent(acceptHover);
            message.setClickEvent(acceptClick);
            requested.spigot().sendMessage(message);

            message = new TextComponent(ChatColor.RED + "" + ChatColor.UNDERLINE + "Click Here" + ChatColor.RED + " or Type /barter deny " + ChatColor.BOLD + requester.getName() + ChatColor.RED + " to deny the trade");
            message.setHoverEvent(denyHover);
            message.setClickEvent(denyClick);
            requested.spigot().sendMessage(message);

            requester.sendMessage(ChatColor.AQUA + "You have requested a trade with " + ChatColor.BOLD + requested.getName());
            requester.sendMessage(ChatColor.GREEN + "You are trading " + trade.getOfferedString());
            message = new TextComponent(ChatColor.RED + "" + ChatColor.UNDERLINE + "Click Here " + ChatColor.RED +  "or type /barter cancel " + ChatColor.BOLD + requested.getName() + ChatColor.RED + " to cancel the trade");
            message.setHoverEvent(cancelHover);
            message.setClickEvent(cancelClick);
            requester.spigot().sendMessage(message);
            return request;
        } else {
            requester.sendMessage(ChatColor.DARK_RED + "You have already requested an active trade with " + requested.getName());
            return null;
        }
    }

    public static TradeRequest sendTradeRequest(String requesterStr, String requestedStr, Trade trade) {
        Player requester = null, requested = null;
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (npc.getName().equalsIgnoreCase(requestedStr))
                requested = (Player) npc.getEntity();
            if (npc.getName().equalsIgnoreCase(requesterStr))
                requester = (Player) npc.getEntity();
        }
        if (requester == null || requested == null) {
            MalmoServerPlugin.inst().getLogger().info("NPCs not found");
            return null;
        }
        if (!hasActiveTradeRequest(requester, requested)) {
            TradeRequest request = new TradeRequest(requester, requested, trade);
            addTradeRequest(request);
            HoverEvent acceptHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + "Click to Accept!").create());
            HoverEvent denyHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.RED + "Click to Deny").create());
            HoverEvent cancelHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.RED + "Click to Cancel").create());
            ClickEvent acceptClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/barter accept "+requester.getName());
            ClickEvent denyClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/barter deny "+requester.getName());
            ClickEvent cancelClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/barter cancel "+requested.getName());

            TextComponent message = new TextComponent(ChatColor.AQUA + requester.getName() + " has requested a trade with you");
            message.setHoverEvent(acceptHover);
            message.setClickEvent(acceptClick);
            requested.spigot().sendMessage(message);
            requested.sendMessage(ChatColor.GREEN + "They want to trade " + trade.getOfferString());

            message = new TextComponent(ChatColor.GREEN + "" + ChatColor.UNDERLINE + "Click Here" + ChatColor.GREEN  + " or Type /barter accept " + ChatColor.BOLD + requester.getName() + ChatColor.GREEN + " to accept the trade");
            message.setHoverEvent(acceptHover);
            message.setClickEvent(acceptClick);
            requested.spigot().sendMessage(message);

            message = new TextComponent(ChatColor.RED + "" + ChatColor.UNDERLINE + "Click Here" + ChatColor.RED + " or Type /barter deny " + ChatColor.BOLD + requester.getName() + ChatColor.RED + " to deny the trade");
            message.setHoverEvent(denyHover);
            message.setClickEvent(denyClick);
            requested.spigot().sendMessage(message);

            requester.sendMessage(ChatColor.AQUA + "You have requested a trade with " + ChatColor.BOLD + requested.getName());
            requester.sendMessage(ChatColor.GREEN + "You are trading " + trade.getOfferedString());
            message = new TextComponent(ChatColor.RED + "" + ChatColor.UNDERLINE + "Click Here " + ChatColor.RED +  "or type /barter cancel " + ChatColor.BOLD + requested.getName() + ChatColor.RED + " to cancel the trade");
            message.setHoverEvent(cancelHover);
            message.setClickEvent(cancelClick);
            requester.spigot().sendMessage(message);
            return request;
        } else {
            requester.sendMessage(ChatColor.DARK_RED + "You have already requested an active trade with " + requested.getName());
            return null;
        }
    }

    public static void acceptTradeRequest(TradeRequest tradeRequest) {
            tradeRequest.accept();
    }

    public static TradeRequest acceptRecentTrade(Player player) {
        if (incomingRequests.containsKey(player.getName())) {
            TradeRequest request = Collections.max(incomingRequests.get(player.getName()), Comparator.comparing(TradeRequest::getBeginTime));
            acceptTradeRequest(request);
            return request;
        } else {
            player.sendMessage(ChatColor.DARK_RED + "You have no recent trade requests");
            return null;
        }
    }

    public static TradeRequest acceptRecentTrade(String player) {
        if (incomingRequests.containsKey(player)) {
            TradeRequest request = Collections.max(incomingRequests.get(player), Comparator.comparing(TradeRequest::getBeginTime));
            acceptTradeRequest(request);
            return request;
        } else {
            return null;
        }
    }

    public static void acceptTrade(Player requested, Player requester) {
        if (incomingRequests.containsKey(requested.getName())) {
            for (TradeRequest request : incomingRequests.get(requested.getName())) {
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

    public static void acceptTrade(String requested, String requester) {
        if (incomingRequests.containsKey(requested)) {
            for (TradeRequest request : incomingRequests.get(requested)) {
                if (request.getRequester().equals(requester) && !request.isCompleted()) {
                    acceptTradeRequest(request);
                }
            }
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
    public static TradeRequest denyRecentTrade(Player player) {

        if (incomingRequests.containsKey(player.getName())) {
            TradeRequest request = Collections.max(incomingRequests.get(player.getName()), Comparator.comparing(TradeRequest::getBeginTime));
            declineTradeRequest(request);
            return request;
        } else {
            player.sendMessage(ChatColor.DARK_RED + "You have no recent active trade requests");
            return null;
        }
    }

    public static TradeRequest denyRecentTrade(String player) {

        if (incomingRequests.containsKey(player)) {
            TradeRequest request = Collections.max(incomingRequests.get(player), Comparator.comparing(TradeRequest::getBeginTime));
            declineTradeRequest(request);
            return request;
        }
        else {
            return null;
        }
    }

    /**
     * Denies the trade from a player
     * @param requested
     * @param requester
     */
    public void denyTrade(Player requested, Player requester) {
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

    public void denyTrade(String requested, String requester) {
        if (incomingRequests.containsKey(requested)) {
            for (TradeRequest request : incomingRequests.get(requested)) {
                if (request.getRequester().equals(requester) && !request.isCompleted()) {
                    declineTradeRequest(request);
                    return;
                }
            }
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

    public static TradeRequest cancelRecentTrade(Player player) {
        if (outgoingRequests.containsKey(player.getName())) {
            TradeRequest request = Collections.max(outgoingRequests.get(player.getName()), Comparator.comparing(TradeRequest::getBeginTime));
            cancelTradeRequest(request);
            return request;
        } else {
            player.sendMessage(ChatColor.DARK_RED + "You have not made any outgoing trade requests");
            return null;
        }
    }

    public static TradeRequest cancelRecentTrade(String player) {
        if (outgoingRequests.containsKey(player)) {
            TradeRequest request = Collections.max(outgoingRequests.get(player), Comparator.comparing(TradeRequest::getBeginTime));
            cancelTradeRequest(request);
            return request;
        } else {
            return null;
        }
    }

    public void cancelTrade(Player requester, Player requested) {
        if (outgoingRequests.containsKey(requester.getName())) {
            for (TradeRequest request : outgoingRequests.get(requester.getName())) {
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
        try {
            Database.createTradeRequest(tradeRequest);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        getAllRequests().add(tradeRequest);
        if (outgoingRequests.containsKey(tradeRequest.getRequester().getName())) {
            outgoingRequests.get(tradeRequest.getRequester().getName()).add(tradeRequest);
        } else {
            ArrayList<TradeRequest> requests = new ArrayList<>();
            requests.add(tradeRequest);
            outgoingRequests.put(tradeRequest.getRequester().getName(), requests);
        }

        if (incomingRequests.containsKey(tradeRequest.getRequested().getName())) {
            incomingRequests.get(tradeRequest.getRequested().getName()).add(tradeRequest);
        } else {
            ArrayList<TradeRequest> requests = new ArrayList<>();
            requests.add(tradeRequest);
            incomingRequests.put(tradeRequest.getRequested().getName(), requests);
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
        if (outgoingRequests.containsKey(requester.getName()) && incomingRequests.containsKey(requested.getName())) {
            for (TradeRequest tradeRequest : outgoingRequests.get(requester.getName())) {
                if (tradeRequest.getRequested().equals(requested) && !tradeRequest.isCompleted()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static TradeRequest getActiveTradeRequest(Player requester, Player requested) {
        if (outgoingRequests.containsKey(requester.getName()) && incomingRequests.containsKey(requested.getName())) {
            for (TradeRequest tradeRequest : outgoingRequests.get(requester.getName())) {
                if (tradeRequest.getRequested().equals(requested) && !tradeRequest.isCompleted()) {
                    return tradeRequest;
                }
            }
        }
        return null;
    }

    public static boolean hasRecentTradeRequest(Player player) {
        return incomingRequests.containsKey(player.getName());
    }

    public static TradeRequest getRecentTradeRequest(Player player) {
        if (incomingRequests.containsKey(player.getName())) {
            return Collections.max(incomingRequests.get(player.getName()), Comparator.comparing(TradeRequest::getBeginTime));
        }
        return null;
    }

    //Method to get all the trade requests involving a player
    public ArrayList<TradeRequest> getAllPlayerTradeRequests(Player player) {
        ArrayList<TradeRequest> requests = new ArrayList<>();
        if (incomingRequests.containsKey(player.getName())) {
            requests.addAll(incomingRequests.get(player.getName()));
        }
        if (outgoingRequests.containsKey(player.getName())) {
            requests.addAll(outgoingRequests.get(player.getName()));
        }
        return requests;
    }




}
