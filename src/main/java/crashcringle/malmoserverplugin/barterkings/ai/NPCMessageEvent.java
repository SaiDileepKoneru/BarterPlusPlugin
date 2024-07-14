package crashcringle.malmoserverplugin.barterkings.ai;

import crashcringle.malmoserverplugin.barterkings.BarterKings;
import crashcringle.malmoserverplugin.barterkings.players.NpcParticipant;
import crashcringle.malmoserverplugin.barterkings.players.Participant;
import crashcringle.malmoserverplugin.barterkings.trades.Trade;
import crashcringle.malmoserverplugin.barterkings.trades.TradeController;
import crashcringle.malmoserverplugin.barterkings.trades.TradeRequest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NPCMessageEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final NpcParticipant npcParticipant;
    private final String message;

    private final String chat;

    private boolean cancel = false;


    public NPCMessageEvent (NpcParticipant npcParticipant, String message) {
        super(true);
        this.npcParticipant = npcParticipant;
        this.message = message;
        this.chat = "<" + npcParticipant.getNPC().getName() + "> " + message;
    }


    public NpcParticipant getNpcParticipant() {
        return npcParticipant;
    }

    public String getMessage() {
        return message;
    }

    public String getChat() {
        return chat;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
