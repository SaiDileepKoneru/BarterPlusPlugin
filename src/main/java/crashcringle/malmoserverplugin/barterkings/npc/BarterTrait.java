package crashcringle.malmoserverplugin.barterkings.npc;

import crashcringle.malmoserverplugin.MalmoServerPlugin;
import crashcringle.malmoserverplugin.barterkings.BarterKings;
import crashcringle.malmoserverplugin.barterkings.trades.Trade;
import crashcringle.malmoserverplugin.barterkings.trades.TradeController;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
//This is your trait that will be applied to a npc using the /trait mytraitname command. Each NPC gets its own instance of this class.
//the Trait class has a reference to the attached NPC class through the protected field 'npc' or getNPC().
//The Trait class also implements Listener so you can add EventHandlers directly to your trait.
@TraitName("barterplus")
public class BarterTrait extends Trait {
    public BarterTrait() {
        super("barterplus");
        plugin = JavaPlugin.getPlugin(MalmoServerPlugin.class);
    }

    MalmoServerPlugin plugin = null;

    boolean SomeSetting = false;

    // see the 'Persistence API' section
    @Persist("mysettingname") boolean automaticallyPersistedSetting = false;

    // Here you should load up any values you have previously saved (optional).
    // This does NOT get called when applying the trait for the first time, only loading onto an existing npc at server start.
    // This is called AFTER onAttach so you can load defaults in onAttach and they will be overridden here.
    // This is called BEFORE onSpawn, npc.getEntity() will return null.
    public void load(DataKey key) {
        SomeSetting = key.getBoolean("SomeSetting", false);
    }

    // Save settings for this NPC (optional). These values will be persisted to the Citizens saves file
    public void save(DataKey key) {
        key.setBoolean("SomeSetting",SomeSetting);
    }

    // An example event handler. All traits will be registered automatically as Spigot event Listeners
    @EventHandler
    public void click(net.citizensnpcs.api.event.NPCRightClickEvent event){
        //Handle a click on a NPC. The event has a getNPC() method.
        //Be sure to check event.getNPC() == this.getNPC() so you only handle clicks on this NPC!
        if (event.getNPC() == this.getNPC()) {
            event.setCancelled(true);
            //The NPC was right clicked!
            event.getClicker().sendMessage("You requested to trade with " + event.getNPC().getName() + "!");
            if (BarterKings.barterGame.inProgress()) {
                // Get a random item out the npcs inventory to trade
                Player npcPlayer = (Player) event.getNPC().getEntity();
                ItemStack[] npcInventory = npcPlayer.getInventory().getContents();
                npcPlayer.chat("Hey!!");

                int counter = 10;
                ItemStack npcItem;
                do {
                    npcItem = npcInventory[(int) (Math.random() * npcInventory.length)];
                    if (npcItem == null) {
                        counter--;
                    }
                }
                while (npcItem == null && counter > 0);
                if (counter <= 0) {
                    npcItem = new ItemStack(Material.DIRT);
                }
                try {
                    BarterKings.controller.sendTradeRequest(npcPlayer, event.getClicker(), new Trade(npcItem, event.getClicker().getInventory().getItemInMainHand()));
                } catch (Exception e) {
                    e.printStackTrace();
                    event.getClicker().sendMessage(ChatColor.RED + "Error, Try again!");
                }
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
//        if (event.getMessage().toUpperCase().contains(npc.getName().toUpperCase())) {
//            npc.getNavigator().setTarget(event.getPlayer().getLocation());
//            MalmoServerPlugin.inst().getLogger().info("NPC is now targeting " + event.getPlayer().getName());
//        } else {
//            MalmoServerPlugin.inst().getLogger().info("NPC is not targeting " + event.getPlayer().getName());
//        }
    }



    // Called every tick
    @Override
    public void run() {

    }

    //Run code when your trait is attached to a NPC.
    //This is called BEFORE onSpawn, so npc.getEntity() will return null
    //This would be a good place to load configurable defaults for new NPCs.
    @Override
    public void onAttach() {

        plugin.getServer().getLogger().info(npc.getName() + "has been assigned BarterTrait!");

    }

    // Run code when the NPC is despawned. This is called before the entity actually despawns so npc.getEntity() is still valid.
    @Override
    public void onDespawn() {
    }

    //Run code when the NPC is spawned. Note that npc.getEntity() will be null until this method is called.
    //This is called AFTER onAttach and AFTER Load when the server is started.
    @Override
    public void onSpawn() {
        if (npc.getEntity() instanceof Player) {
            Player npcPlayer = (Player) npc.getEntity();
            npcPlayer.getInventory().addItem(new ItemStack(Material.DIAMOND, 1));
        }
    }

    //run code when the NPC is removed. Use this to tear down any repeating tasks.
    @Override
    public void onRemove() {
    }

}
