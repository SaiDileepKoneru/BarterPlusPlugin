package crashcringle.malmoserverplugin;

import crashcringle.malmoserverplugin.api.MalmoTraderInteractEvent;
import crashcringle.malmoserverplugin.barterkings.BarterKings;
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
        if (!(event.getRightClicked() instanceof Villager)) return;
        if (!BarterKings.getTraders().containsKey(event.getRightClicked().getEntityId()))
            new MalmoTrader((Villager) event.getRightClicked());
        else
            BarterKings.traders.get(event.getRightClicked().getEntityId()).printRecipes();
    }

    @EventHandler
    
    public void broadcastTradeEvent (AsyncPlayerChatEvent event) {
        if (event.getMessage().contains("want to trade") || event.getMessage().contains("trade with me?") || event.getMessage().contains("trade?")) {
            event.setCancelled(true);
            TextComponent message = new TextComponent(event.getPlayer().getDisplayName() + " wants to trade!");
            message.setColor(ChatColor.AQUA);
            message.setItalic(true);
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.AQUA + event.getPlayer().getName() + ": " + ChatColor.DARK_RED + event.getMessage()).create()));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org"));
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.spigot().sendMessage(message);
            }


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
                    target.sendMessage(ChatColor.AQUA + player.getDisplayName() + " wants to trade with you!");
                }
            } else {
                player.sendMessage("You're not in a game right now!");
            }
        }
    }


}
