package com.crashcringle.barterplus.api;

import com.crashcringle.barterplus.barterkings.BarterKings;
import com.crashcringle.barterplus.barterkings.players.Participant;
import com.crashcringle.barterplus.barterkings.trades.Trade;
import com.crashcringle.barterplus.barterkings.trades.TradeController;
import com.crashcringle.barterplus.barterkings.trades.TradeRequest;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

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
