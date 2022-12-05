package crashcringle.malmoserverplugin;

import crashcringle.malmoserverplugin.api.MalmoTraderInteractEvent;
import crashcringle.malmoserverplugin.barterkings.BarterKings;
import crashcringle.malmoserverplugin.barterkings.villagers.MalmoTrader;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;


import java.util.logging.Level;
public class MalmoServerListener implements Listener {


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
            player.sendMessage(ChatColor.AQUA + "You right clicked " + target.getDisplayName());
            target.sendMessage(ChatColor.AQUA + player.getDisplayName() + " wants to trade with you!");
        }
    }

}
