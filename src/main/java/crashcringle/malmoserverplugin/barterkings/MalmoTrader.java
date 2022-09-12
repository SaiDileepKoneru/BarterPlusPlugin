package crashcringle.malmoserverplugin.barterkings;

import crashcringle.malmoserverplugin.MalmoServerPlugin;
import org.bukkit.entity.*;
import org.bukkit.inventory.MerchantRecipe;

import java.util.logging.Level;

public class MalmoTrader {
    Villager trader;

    public MalmoTrader(Villager villager) {
        trader = villager;
        trader.setCustomName("Malmo Trader");
        trader.setProfession(Villager.Profession.HUSK);
        trader.setGlowing(true);
    }

    public LivingEntity getLivingEntity() {
        return trader;
    }
    public MalmoTrader getMalmoTrader(Villager villager) {
        return this;
    }

    public void setName(String name) {
        trader.setCustomName(name);
    }

    public void printRecipes() {
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "**********************************");
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Malmo Trader Recipes");
        trader.getRecipes().forEach(recipe -> {
            MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Recipe: " + recipe.getResult().getType());
            recipe.getIngredients().forEach(itemStack -> {
                MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Ingredient: " + itemStack.getType());
            });
            MalmoServerPlugin.inst().getLogger().log(Level.INFO, "**********************************");
        });
    }

    public void addRecipe(MerchantRecipe recipe) {
        trader.getRecipes().add(recipe);
    }
    public void removeRecipe(MerchantRecipe recipe) {
        trader.getRecipes().remove(recipe);
    }
    public void removeAllRecipes() {
        trader.getRecipes().clear();
    }

    public int getRiches() {
        return trader.getRiches();
    }

    public void setRiches(int riches) {
        trader.setRiches(riches);
    }
}
