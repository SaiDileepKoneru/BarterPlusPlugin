package com.crashcringle.barterplus.api;

import com.crashcringle.barterplus.barterkings.BarterKings;
import com.crashcringle.barterplus.barterkings.villagers.MalmoTrader;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class MalmoTraderInteractEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;
    private final MalmoTrader malmoTrader;
    private final PlayerInteractEntityEvent playerInteractEntityEvent;
    private boolean isCancelled = false;

    public MalmoTraderInteractEvent(MalmoTrader trader, PlayerInteractEntityEvent event) {
        this.entity = trader.getLivingEntity();
        this.malmoTrader = trader;
        this.playerInteractEntityEvent = event;
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
        playerInteractEntityEvent.setCancelled(b);
    }

    public Entity getEntity() {
        return entity;
    }

    public PlayerInteractEntityEvent getPlayerInteractEntityEvent() {
        return playerInteractEntityEvent;
    }

    public MalmoTrader getMalmoTrader() {
        return malmoTrader;
    }
    /**
     * @return
     */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public static class MalmoTradingEvents implements Listener {

        @EventHandler(priority = EventPriority.HIGH)
        public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
            if (!(event.getRightClicked() instanceof Villager)) return;
            MalmoTrader malmoTrader = BarterKings.getTraders().get((event.getRightClicked().getEntityId()));
            if (malmoTrader == null) return;
            MalmoTraderInteractEvent malmoTraderInteractEvent = new MalmoTraderInteractEvent(malmoTrader, event);
        }

    }
}
