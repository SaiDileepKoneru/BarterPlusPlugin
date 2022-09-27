package crashcringle.malmoserverplugin.barterkings.villagers;

import crashcringle.malmoserverplugin.MalmoServerPlugin;
import crashcringle.malmoserverplugin.barterkings.BarterKings;
import org.bukkit.entity.*;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.logging.Level;

public class MalmoTrader implements Merchant {
    Villager mTrader;

    public MalmoTrader(Villager villager) {
        mTrader = villager;
        mTrader.setCustomName("Malmo Trader");
        mTrader.setProfession(Villager.Profession.LIBRARIAN);
        mTrader.setGlowing(true);
        PersistentTagger.tagMalmoTrader(mTrader);
        BarterKings.traders.put(villager.getEntityId(), this);
    }

    public LivingEntity getLivingEntity() {
        return mTrader;
    }
    public MalmoTrader getMalmoTrader(Villager villager) {
        return this;
    }

    public void setName(String name) {
        mTrader.setCustomName(name);
    }

    public void printRecipes() {
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "**********************************");
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Malmo Trader Recipes");
        mTrader.getRecipes().forEach(recipe -> {
            MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Recipe: " + recipe.getResult().getType());
            recipe.getIngredients().forEach(itemStack -> {
                MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Ingredient: " + itemStack.getType());
            });
            MalmoServerPlugin.inst().getLogger().log(Level.INFO, "**********************************");
        });
    }

    public void addRecipe(MerchantRecipe recipe) {
        mTrader.getRecipes().add(recipe);
    }
    public void removeRecipe(MerchantRecipe recipe) {
        mTrader.getRecipes().remove(recipe);
    }
    public void removeAllRecipes() {
        mTrader.getRecipes().clear();
    }



    /**
     * Get a list of trades currently available from this merchant.
     *
     * @return an immutable list of trades
     */
    @NotNull
    @Override
    public List<MerchantRecipe> getRecipes() {
        return mTrader.getRecipes();
    }

    /**
     * Set the list of trades currently available from this merchant.
     * <br>
     * This will not change the selected trades of players currently trading
     * with this merchant.
     *
     * @param recipes a list of recipes
     */
    @Override
    public void setRecipes(@NotNull List<MerchantRecipe> recipes) {
        mTrader.setRecipes(recipes);
    }

    /**
     * Get the recipe at a certain index of this merchant's trade list.
     *
     * @param i the index
     * @return the recipe
     * @throws IndexOutOfBoundsException if recipe index out of bounds
     */
    @NotNull
    @Override
    public MerchantRecipe getRecipe(int i) throws IndexOutOfBoundsException {
        return mTrader.getRecipe(i);
    }

    /**
     * Set the recipe at a certain index of this merchant's trade list.
     *
     * @param i      the index
     * @param recipe the recipe
     * @throws IndexOutOfBoundsException if recipe index out of bounds
     */
    @Override
    public void setRecipe(int i, @NotNull MerchantRecipe recipe) throws IndexOutOfBoundsException {
        mTrader.setRecipe(i, recipe);
    }

    /**
     * Get the number of trades this merchant currently has available.
     *
     * @return the recipe count
     */
    @Override
    public int getRecipeCount() {
        return mTrader.getRecipeCount();
    }

    /**
     * Gets whether this merchant is currently trading.
     *
     * @return whether the merchant is trading
     */
    @Override
    public boolean isTrading() {
        return mTrader.isTrading();
    }

    /**
     * Gets the player this merchant is trading with, or null if it is not
     * currently trading.
     *
     * @return the trader, or null
     */
    @Nullable
    @Override
    public HumanEntity getTrader() {
        return mTrader.getTrader();
    }


}
