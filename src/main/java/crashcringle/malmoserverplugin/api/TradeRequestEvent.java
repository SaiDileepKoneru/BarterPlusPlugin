package crashcringle.malmoserverplugin.api;

import crashcringle.malmoserverplugin.MalmoServerPlugin;
import crashcringle.malmoserverplugin.barterkings.BarterKings;
import crashcringle.malmoserverplugin.barterkings.players.Participant;
import crashcringle.malmoserverplugin.barterkings.trades.Trade;
import crashcringle.malmoserverplugin.barterkings.trades.TradeController;
import crashcringle.malmoserverplugin.barterkings.trades.TradeRequest;
import crashcringle.malmoserverplugin.barterkings.villagers.MalmoTrader;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class TradeRequestEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Trade trade;

    private final Player requester;

    private final Player requested;

    private final Participant requesterParticipant;

    private final Participant requestedParticipant;

    private final TradeRequest tradeRequest;

    private boolean isCancelled = false;


    public TradeRequestEvent(Trade trade, Player requester, Player requested) {
        this.trade = trade;
        this.requester = requester;
        this.requested = requested;
        this.tradeRequest = TradeController.sendTradeRequest(requester, requested, trade);
        this.requestedParticipant = BarterKings.barterGame.getParticipant(requested);
        this.requesterParticipant = BarterKings.barterGame.getParticipant(requester);
    }
    /**
     * Gets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins
     *
     * @return true if this event is cancelled
     */
    @Override
    public boolean isCancelled() {
        return this.isCancelled();
    }

    /**
     * Sets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins.
     *
     * @param b true if you wish to cancel this event
     */
    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }

      public Trade getTrade() {
            return trade;
        }

        public Player getRequester() {
            return requester;
        }

        public Player getRequested() {
            return requested;
        }

        public Participant getRequesterParticipant() {
            return requesterParticipant;
        }

        public Participant getRequestedParticipant() {
            return requestedParticipant;
        }

        public TradeRequest getTradeRequest() {
            return tradeRequest;
        }
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
