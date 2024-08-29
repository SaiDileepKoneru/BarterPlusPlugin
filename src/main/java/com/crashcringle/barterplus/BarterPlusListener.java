package com.crashcringle.barterplus;

import com.crashcringle.barterplus.api.MalmoTraderInteractEvent;
import com.crashcringle.barterplus.barterkings.BarterKings;
import com.crashcringle.barterplus.barterkings.ai.NPCMessageEvent;
import com.crashcringle.barterplus.barterkings.players.Participant;
import com.crashcringle.barterplus.data.Database;
import com.crashcringle.barterplus.barterkings.players.NpcParticipant;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;


import java.sql.SQLException;
import java.util.logging.Level;
public class BarterPlusListener implements Listener {

    TradeMenu tradeMenu;
    @EventHandler
    public void openVillager (MalmoTraderInteractEvent event) {
        BarterPlus.inst().getLogger().log(Level.INFO, "Recipes!");
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
        BarterPlus.inst().getLogger().log(Level.INFO, "Player joined!");
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
        BarterPlus.inst().getLogger().log(Level.INFO, "Player interacted with " + event.getRightClicked().getName());
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
//            if (event.getPlayer().hasMetadata("NPC")) {
//            if (BarterKings.barterGame.isParticipant(event.getPlayer())) {
//                Participant participant = BarterKings.barterGame.getParticipant(event.getPlayer());
//                if (participant instanceof NpcParticipant) {
//                    NpcParticipant npc = (NpcParticipant) participant;
//                    org.bukkit.ChatColor color = switch (npc.getProfession().getName()) {
//                        case "Farmer" -> org.bukkit.ChatColor.GREEN;
//                        case "Fisherman" -> org.bukkit.ChatColor.AQUA;
//                        case "Mason" -> org.bukkit.ChatColor.GRAY;
//                        case "Shepherd" -> org.bukkit.ChatColor.WHITE;
//                        case "Blacksmith" -> org.bukkit.ChatColor.DARK_GRAY;
//                        case "Librarian" -> org.bukkit.ChatColor.DARK_BLUE;
//                        case "Butcher" -> org.bukkit.ChatColor.RED;
//                        case "Lumberjack" -> org.bukkit.ChatColor.DARK_GREEN;
//                        case "Leatherworker" -> org.bukkit.ChatColor.GOLD;
//                        default -> org.bukkit.ChatColor.WHITE;
//                    };
//                }
//            }
//        }
        if (!BarterKings.barterGame.inProgress()) {
            // Only process messages from kalyaniplays or CrashCringle12
            if (!event.getPlayer().getName().equals("kalyaniplays") && !event.getPlayer().getName().equals("CrashCringle12")) {
                BarterPlus.inst().getLogger().info("Player " + event.getPlayer().getName() + " tried to chat but the game is not in progress.");
                return;
            }
        }
        // Format the time in a human readable way
        String time = java.time.LocalTime.now().toString();
        String message = "["+time+"] "+event.getPlayer().getName() + ": " + event.getMessage();
        if (!event.getMessage().isEmpty() && !event.getMessage().contains("private_message") && !event.getMessage().contains("[*]") && !event.getMessage().contains("do_nothing")) {
            BarterPlus.inst().getLogger().info("+++++++++++++++++++++++++++++++++++++");
            BarterPlus.inst().getLogger().info(message);
            for (Participant participant : BarterKings.barterGame.getParticipants()) {
                if (participant instanceof NpcParticipant) {
                    // Check if the npc is currently generating a message
                    NpcParticipant npcParticipant = (NpcParticipant) participant;
                    npcParticipant.queueMessage(message);
                }
            }
        } else {
            BarterPlus.inst().getLogger().info("-------------------------------------");
            BarterPlus.inst().getLogger().info(message);
        }
    }

    @EventHandler
    public void onNPCMessage(NPCMessageEvent event) {

    }
    @EventHandler
    public void onEatEvent(PlayerItemConsumeEvent event) {
        event.setCancelled(true);
        event.getPlayer().sendMessage("You can't eat in this game!");
    }


}
