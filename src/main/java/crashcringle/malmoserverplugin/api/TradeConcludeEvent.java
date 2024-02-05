package crashcringle.malmoserverplugin.api;

import crashcringle.malmoserverplugin.barterkings.BarterKings;
import crashcringle.malmoserverplugin.barterkings.players.Participant;
import crashcringle.malmoserverplugin.barterkings.trades.Trade;
import crashcringle.malmoserverplugin.barterkings.trades.TradeController;
import crashcringle.malmoserverplugin.barterkings.trades.TradeRequest;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TradeConcludeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player concluder;

    private final String reason;

    private final TradeRequest request;

    private boolean isCancelled = false;


    public TradeConcludeEvent(Player concluder, String reason) {
        this.concluder = concluder;
        this.reason = reason;

        switch (reason) {
            case "accept":
                this.request = TradeController.acceptRecentTrade(concluder);
                break;
            case "decline":
                this.request = TradeController.denyRecentTrade(concluder);
                break;
            case "cancel":
                this.request = TradeController.cancelRecentTrade(concluder);
                break;
            default:
                this.request = null;
        }
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

        public Player getConcluder() {
                return concluder;
            }

            public String getReason() {
                return reason;
            }

            public TradeRequest getRequest() {
                return request;
            }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}