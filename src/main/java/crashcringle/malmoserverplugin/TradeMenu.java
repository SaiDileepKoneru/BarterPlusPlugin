package crashcringle.malmoserverplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.mask.RecipeMask;
import org.ipvp.canvas.slot.ClickOptions;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.type.ChestMenu;

import crashcringle.malmoserverplugin.barterkings.trades.TradeRequest;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TradeMenu {
    Menu menu;
    List<Slot> player1Slots = new ArrayList<>();
    List<Slot> player2Slots = new ArrayList<>();
    List<ItemStack> player1Items = new ArrayList<>();
    List<ItemStack> player2Items = new ArrayList<>();
    Player player1;
    Player player2;
    boolean player1Ready = false;
    boolean player2Ready = false;

    public TradeMenu(Player player1, Player player2, TradeRequest tradeRequest) {
        this.player1 = player1;
        this.player2 = player2;
        menu = createMenu();
        addCheckerBorder(menu);
        makeSlotsAccessible(menu);
        addCloseHandler(menu);
        Slot slot = menu.getSlot(2);
        slot.setItemTemplate(p -> {
            int level = p.getLevel();
            ItemStack item = new ItemStack(Material.KNOWLEDGE_BOOK);
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(player1.getDisplayName() + ChatColor.GOLD + "'s offered items!'");
            item.setItemMeta(itemMeta);
            return item;
        });
        slot = menu.getSlot(6);
        slot.setItemTemplate(p -> {
            int level = p.getLevel();
            ItemStack item = new ItemStack(Material.KNOWLEDGE_BOOK);
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(player2.getDisplayName() + ChatColor.GOLD + "'s offered items!'");
            item.setItemMeta(itemMeta);
            return item;
        });
        slot = menu.getSlot(27);
        slot.setItemTemplate(p -> {
            int level = p.getLevel();
            ItemStack item = new ItemStack(Material.REDSTONE);
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(ChatColor.RED + "Deny trade?");
            item.setItemMeta(itemMeta);
            return item;
        });
        
        slot.setClickHandler((player, info) -> {
            tradeRequest.setCancelled(true);
            this.menu.close();
        });

        slot = menu.getSlot(31);
        slot.setItemTemplate(p -> {
            int level = p.getLevel();
            ItemStack item = new ItemStack(Material.EMERALD);
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(ChatColor.GREEN + "Accept trade?");
            item.setItemMeta(itemMeta);
            return item;
        });

        slot.setClickHandler((player, info) -> {
            if (player == player1 && !player1Ready) {
                player1Ready = true;
                return;
            } else if (player == player2 && !player2Ready) {
                player2Ready = true;
                return;
            }
            tradeRequest.completeTradeMenu();
        });

        slot = menu.getSlot(31);
        slot.setItemTemplate(p -> {
            int level = p.getLevel();
            ItemStack item = new ItemStack(Material.REDSTONE);
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(ChatColor.RED + "Deny trade?");
            item.setItemMeta(itemMeta);
            return item;
        });
        
        slot.setClickHandler((player, info) -> {
            tradeRequest.setCancelled(true);
            this.menu.close();
        });

        slot = menu.getSlot(35);
        slot.setItemTemplate(p -> {
            int level = p.getLevel();
            ItemStack item = new ItemStack(Material.EMERALD);
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(ChatColor.GREEN + "Accept trade?");
            item.setItemMeta(itemMeta);
            return item;
        });

        slot.setClickHandler((player, info) -> {
            if (player == player1 && !player1Ready) {
                player1Ready = true;
                return;
            } else if (player == player2 && !player2Ready) {
                player2Ready = true;
                return;
            }
            tradeRequest.completeTradeMenu();
        });
//        Menu.Builder pageTemplate = ChestMenu.builder(3).title("Items").redraw(true);
//        Mask itemSlots = BinaryMask.builder(pageTemplate.getDimensions())
//                .pattern("011111110").build();
//        List<Menu> pages = PaginatedMenuBuilder.builder(pageTemplate)
//                .slots(itemSlots)
//                .nextButton(new ItemStack(Material.GREEN_STAINED_GLASS_PANE))
//                .nextButtonEmpty(new ItemStack(Material.GRAY_STAINED_GLASS_PANE)) // Icon when no next page available
//                .nextButtonSlot(23)
//                .previousButton(new ItemStack(Material.GREEN_STAINED_GLASS_PANE))
//                .previousButtonEmpty(new ItemStack(Material.GRAY_STAINED_GLASS_PANE)) // Icon when no previous page available
//                .previousButtonSlot(21)
//                .addItem(new ItemStack(Material.DIRT))
//                .addItem(new ItemStack(Material.GRASS))
//                .addItem(new ItemStack(Material.COBBLESTONE))
//                .addItem(new ItemStack(Material.STONE))
//                // ...
//                .build();
//        pages.set(0, menu);

    }
    public TradeMenu(Player player1) {
        this.player1 = player1;
    }

    public Menu createMenu() {
    return ChestMenu.builder(4)
            .title("Trade Menu")
            .build();
    }
    public void display2Menu(Player player) {
//        menu = createMenu();
//        menu.open(player);
//        addWhiteBorder(menu);
    }

    public void displayMenu(Player p1, Player p2) {
        menu.open(p1);
        menu.open(p2);
    }
    public void displayMenu() {
        menu.open(player1);
        menu.open(player2);
    }
    
    public void addClickOptions(Slot slot) {
        ClickOptions options = ClickOptions.builder()
                .allow(ClickType.LEFT, ClickType.RIGHT, ClickType.DOUBLE_CLICK, ClickType.CREATIVE, ClickType.UNKNOWN)
                .allow(InventoryAction.PLACE_ALL, InventoryAction.DROP_ONE_SLOT, InventoryAction.DROP_ALL_CURSOR, InventoryAction.SWAP_WITH_CURSOR, InventoryAction.PLACE_SOME, InventoryAction.PICKUP_SOME, InventoryAction.PICKUP_ONE, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF, InventoryAction.UNKNOWN, InventoryAction.HOTBAR_SWAP, InventoryAction.PLACE_ONE, InventoryAction.PLACE_SOME)
                .build();
        slot.setClickOptions(options);
    }
    public void makeSlotsAccessible(Menu menu) {
        int rows = 2;
        int start = 10;
        // Sets Player 1's slots
        for (int i = 0; i < rows; i++) {
            int x = 9 * i;
            for (int j = start+x; j < start+x+3; j++) {
                Slot slot = menu.getSlot(j);
                addClickOptions(slot);
                addClickHandler(slot);
                player1Slots.add(slot);
            }
        }
        start = 14;
        // Sets Player 2's slots
        for (int i = 0; i < rows; i++) {
            int x = 9 * i;
            for (int j = start+x; j < start+3+x; j++) {
                Slot slot = menu.getSlot(j);
                addClickOptions(slot);
                addClickHandler(slot);
                player2Slots.add(slot);
            }
        }
    }
    public void addCloseHandler(Menu menu) {
        menu.setCloseHandler((player, menu1) -> {
            player.sendMessage("You just closed the menu...");
        });
    }

    // getItemAmount shows the amount of item added or removed
    // getRawItem shows the item in the slot prior to this specific click
    public void addClickHandler(Slot slot) {
        slot.setClickHandler((player, info) -> {
            // If the slot is not the player's slot, return
            if ((player == player1 && !player1Slots.contains(slot)) || (player == player2 && !player2Slots.contains(slot))) {
                MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Player " + player.getName() + " tried to access a slot that is not theirs!");
                info.setResult(Event.Result.DENY);
                return;
            }
            // If the slot is the player's slot, allow the click

            MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Player " + player.getName() + " clicked slot " + slot.getIndex() + " at " + System.currentTimeMillis());
            MalmoServerPlugin.inst().getLogger().log(Level.INFO, info.getClickType().toString() + " " + info.getAction().toString() + " " + info.getResult().toString());
            MalmoServerPlugin.inst().getLogger().log(Level.INFO, info.getClickedSlot().getItem(player1) + " vs" +info.getClickedSlot().getItem(player2));
            MalmoServerPlugin.inst().getLogger().log(Level.INFO, info.getClickedSlot().getRawItem(player1) + " vs" + info.getClickedSlot().getRawItem(player2));
           // MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Amount : " + info.getItemAmount());
            MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Cursor Item : " + player.getItemOnCursor());
            //MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Adding Item : " + info.getAddingItem());

            player.sendMessage("You clicked the slot at index " + info.getClickedSlot().getIndex());
            Player oppPlayer = player == player1 ? player2 : player1;
            // If the player is adding item to the slot
            if (info.isAddingItem()) {
                // Get the item that the player is adding
                ItemStack addingItem = new ItemStack(info.getAddingItem());
                // Get the amount of the item that the player is adding
                int addingAmount = info.getItemAmount();
                addingItem.setAmount(addingAmount);
                // If the slot is empty, set the item in the slot to the item that the player is adding
                if (info.getClickedSlot().getRawItem(player) == null) {
                    info.getClickedSlot().setRawItem(oppPlayer, addingItem);

                } else {
                    // Get the item that is currently in the slot
                    ItemStack slotItem = new ItemStack(info.getClickedSlot().getRawItem(player));
                    // Get the amount of the item that is currently in the slot
                    int slotAmount = slotItem.getAmount();
                    // If the slot is not empty, add the item that the player is adding to the item in the slot
                    slotItem.setAmount(slotAmount + addingAmount);
                    info.getClickedSlot().setRawItem(oppPlayer, slotItem);
                    info.getClickedSlot().setItem(slotItem);
                }
            } else if (info.isTakingItem()) {
                // Get the item that the player is taking

            }
            // Additional functionality goes here
        });
    }
    public void addWhiteBorder(Menu menu) {
        Mask mask = BinaryMask.builder(menu)
                .item(new ItemStack(Material.WHITE_STAINED_GLASS_PANE))
                .pattern("111111111") // First row
                .pattern("100010001") // Second row
                .pattern("100010001") // Third row
                .pattern("111111111").build(); // Fourth row
        mask.apply(menu);
    }
    public void addCheckerBorder(Menu menu) {
        Mask mask = RecipeMask.builder(menu)
                .item('w', new ItemStack(Material.WHITE_STAINED_GLASS_PANE))
                .item('r', new ItemStack(Material.RED_STAINED_GLASS_PANE))
                .item('c', new ItemStack(Material.CYAN_STAINED_GLASS_PANE))
                .pattern("rrrrwcccc") // First row
                .pattern("r000w000c") // Second row
                .pattern("r000w000c") // Third row
                .pattern("rrrrwcccc").build(); // Fourth row
        mask.apply(menu);
    }
}
