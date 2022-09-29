package crashcringle.malmoserverplugin;

import crashcringle.malmoserverplugin.api.MalmoTraderInteractEvent;
import crashcringle.malmoserverplugin.barterkings.BarterKings;
import crashcringle.malmoserverplugin.barterkings.villagers.MalmoTrader;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
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
    public void broadcastTrade (AsyncPlayerChatEvent event) {
        if (event.getMessage().contains("trade") || event.getMessage().contains("Trade")) {
            event.setCancelled(true);
            MalmoServerPlugin.inst().getServer().broadcastMessage(event.getPlayer().getDisplayName() + " wants to trade!");

        }
    }


}
