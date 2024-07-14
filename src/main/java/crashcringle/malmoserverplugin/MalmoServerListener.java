package crashcringle.malmoserverplugin;

import com.cjcrafter.openai.chat.ChatMessage;
import crashcringle.malmoserverplugin.api.MalmoTraderInteractEvent;
import crashcringle.malmoserverplugin.barterkings.BarterKings;
import crashcringle.malmoserverplugin.barterkings.ai.NPCMessageEvent;
import crashcringle.malmoserverplugin.barterkings.players.NpcParticipant;
import crashcringle.malmoserverplugin.barterkings.players.Participant;
import crashcringle.malmoserverplugin.barterkings.trades.TradeController;
import crashcringle.malmoserverplugin.barterkings.villagers.MalmoTrader;
import crashcringle.malmoserverplugin.data.Database;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.slot.ClickOptions;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.type.ChestMenu;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
public class MalmoServerListener implements Listener {

    TradeMenu tradeMenu;
    @EventHandler
    public void openVillager (MalmoTraderInteractEvent event) {
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Recipes!");
        event.getMalmoTrader().printRecipes();
    }

    @EventHandler
    public void openVillager (PlayerInteractEntityEvent event) {
//        if (!(event.getRightClicked() instanceof Villager)) return;
//        if (!BarterKings.getTraders().containsKey(event.getRightClicked().getEntityId()))
//            new MalmoTrader((Villager) event.getRightClicked());
//        else
//            BarterKings.traders.get(event.getRightClicked().getEntityId()).printRecipes();
    }

    @EventHandler
    
    public void broadcastTradeEvent (AsyncPlayerChatEvent event) {

        if (BarterKings.barterGame.isParticipant(event.getPlayer())) {
            Participant participant = BarterKings.barterGame.getParticipant(event.getPlayer());
            event.setMessage(participant.getColor() + event.getMessage());
        }

    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Player joined!");
        event.getPlayer().sendMessage("Welcome to the BarterPlus Environment!");
        event.getPlayer().sendMessage("Type /trade to open the trade menu!");
        try {
            Database.createPlayer(event.getPlayer());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    /** Event that fires when a player drops an item. */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        // Get the player and the item they dropped.
        Player player = event.getPlayer();
        if (BarterKings.barterGame.isParticipant(player))
            event.setCancelled(true);
    }

   /**
    * @author CrashCringle
    Listens for when a player right clicks another player
    */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Player interacted with " + event.getRightClicked().getName());
        if (event.getRightClicked() instanceof Player) {
            if (event.getRightClicked().hasMetadata("NPC"))
                return;
            Player player = event.getPlayer();
            Player target = (Player) event.getRightClicked();

            if (BarterKings.barterGame.isParticipant(player)) {
                Participant participant = BarterKings.barterGame.getParticipant(player);
                Participant otherParticipant = BarterKings.barterGame.getParticipant(target);
                participant.setClickedPlayer(target);
                if (participant.getClickedPlayer() == otherParticipant.getPlayer() && otherParticipant.getClickedPlayer() == participant.getPlayer()) {
                    BarterKings.controller.attemptTradeRequestViaMenu(player, target);
                } else {
                    player.sendMessage(ChatColor.AQUA + "You asked to trade with " + target.getDisplayName());
                    target.sendMessage(ChatColor.AQUA + player.getDisplayName() + " wants to trade with you. Click them to initiate!");
                }
            } else {
                player.sendMessage("You're not in a game right now!");
            }
        }
    }
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        //  Add the chat to the npc's chat messages
        // Get all participants that are instances of an npcParticipant
        // Format the chat message
        String time = String.valueOf(System.currentTimeMillis());
        String message = "["+time+"] "+event.getPlayer().getName() + ": " + event.getMessage();
        for (Participant participant : BarterKings.barterGame.getParticipants()) {
            if (participant instanceof NpcParticipant) {
                // Check if the npc is currently generating a message
                NpcParticipant npcParticipant = (NpcParticipant) participant;
                if (npcParticipant.isGenerating()) {
                    // Add the chat message to the npc's chat messages
                    MalmoServerPlugin.inst().getLogger().info("Adding message to npc's chat messages");
                    npcParticipant.chunkMessage(message);

                } else {
                    MalmoServerPlugin.inst().getLogger().info("Processing message");
                    npcParticipant.processMessage(message);
                }
            }
        }
    }

    @EventHandler
    public void onNPCMessage(NPCMessageEvent event) {
//        NpcParticipant npcParticipant = event.getNpcParticipant();
//        String message = event.getMessage();
//        String chat = event.getChat();
//        Bukkit.broadcastMessage(chat);
//        MalmoServerPlugin.inst().getLogger().info("NPC Message Event: "+chat);
//        for (Participant participant : BarterKings.barterGame.getParticipants()) {
//            if (participant instanceof NpcParticipant) {
//                NpcParticipant npc = (NpcParticipant) participant;
//                if (npc != npcParticipant) {
//                    if (npc.isGenerating()) {
//                        npc.chunkMessage(chat);
//                    } else {
//                        npc.processMessage(chat);
//                    }
//                }
//            }
//        }
    }
    @EventHandler
    public void onEatEvent(PlayerItemConsumeEvent event) {
        event.setCancelled(true);
        event.getPlayer().sendMessage("You can't eat in this game!");
    }


}
