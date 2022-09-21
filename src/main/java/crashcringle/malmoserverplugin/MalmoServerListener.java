package crashcringle.malmoserverplugin;

import crashcringle.malmoserverplugin.api.MalmoTraderInteractEvent;
import crashcringle.malmoserverplugin.barterkings.BarterKings;
import crashcringle.malmoserverplugin.barterkings.MalmoTrader;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;

import java.util.logging.Level;
public class MalmoServerListener implements Listener {
//
//     @EventHandler
//    public void inventoryUsed(InventoryOpenEvent event) {
//        int counter = 0;
//        Inventory inventory = event.getInventory();
//        for (ItemStack item : inventory.getContents()) {
//            if (item != null) {
//                MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Item: " + item.getType());
//                event.getPlayer().sendMessage("Item: " +  item.getType());
//                counter++;
//            }
//        }
//        if (counter < 2) {
//            MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Empty Inventory");
//            event.getPlayer().sendMessage("Empty Inventory");
//        }
//     }
//
//    @EventHandler
//    public void inventoryCreativeUsed(InventoryCreativeEvent event) {
//        int counter = 0;
//        Inventory inventory = event.getInventory();
//        for (ItemStack item : inventory.getContents()) {
//            if (item != null) {
//                MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Creative Item: " + item.getType());
//                event.getWhoClicked().sendMessage("Creative Item: " + item.getType());
//                counter++;
//            }
//        }
//        if (counter < 2) {
//            MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Empty Inventory");
//            event.getWhoClicked().sendMessage("Empty Inventory");
//        }
//    }
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
//        Villager villager = (Villager) event.getRightClicked();
//        villager.getRecipes().forEach(recipe -> {
//            MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Recipe: " + recipe.getResult().getType());
//            event.getPlayer().sendMessage("Recipe: " + recipe.getResult().getType());
//            recipe.getIngredients().forEach(itemStack -> {
//                MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Ingredient: " + itemStack.getType());
//                event.getPlayer().sendMessage("Ingredient: " + itemStack.getType());
//            });
//        });


//    @EventHandler
//    public void InventoryClicked(InventoryClickEvent event) {
//        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "********************************************************");
//        if (event.getInventory() instanceof MerchantInventory) {
//            MerchantInventory merchantInventory = (MerchantInventory) event.getInventory();
//
//            MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Merchant Inventory Contents");
//            for (ItemStack item : merchantInventory.getContents()) {
//                if (item != null) {
//                    MalmoServerPlugin.inst().getLogger().log(Level.INFO,  "Item " + item.getType());
//                }
//            }
////            if (merchantInventory.getSelectedRecipe() != null) {
////                if (merchantInventory.getSelectedRecipe().getIngredients() != null)
////                    if (merchantInventory.getSelectedRecipe().getResult() != null)
////                        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Merchant Recipe Ingredients: " + merchantInventory.getSelectedRecipe().getIngredients());
////                MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Merchant Recipe Results " + merchantInventory.getSelectedRecipe().getResult());
////
////            }
//        }
//        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Clicked Inventory Name: " + event.getClickedInventory().getName());
//        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Clicked Inventory Type: " + event.getClickedInventory().getType());
//        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Clicked Inventory Title: " + event.getClickedInventory().getTitle());
//        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Clicked Inventory Clicker " + event.getWhoClicked().getName());
//        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Clicked Inventory Slot: " + event.getSlot());
//        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Clicked Inventory Slot Type: " + event.getSlotType().toString());
//        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Clicked Inventory Current Item: " + event.getCurrentItem());
//        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Clicked Inventory Contents");
//
//        for (ItemStack item : event.getClickedInventory().getContents()) {
//
//            if (item != null) {
//                MalmoServerPlugin.inst().getLogger().log(Level.INFO,  "Item: " + item.getType());
//            }
//        }
//        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "********************************************************");
//        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Inventory Contents");
//
//        for (ItemStack item : event.getInventory().getContents()) {
//            if (item != null) {
//                MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Inventory: " + item.getType());
//            }
//        }
//        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "+---------------------------------------------------------------------------------------------------------------------+");
//
//    }


}
