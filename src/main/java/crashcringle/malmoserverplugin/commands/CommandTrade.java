package crashcringle.malmoserverplugin.commands;

import crashcringle.malmoserverplugin.MalmoServerPlugin;
import crashcringle.malmoserverplugin.barterkings.trades.Trade;
import crashcringle.malmoserverplugin.barterkings.trades.TradeController;
import crashcringle.malmoserverplugin.barterkings.trades.TradeRequest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.logging.Level;

public class CommandTrade implements CommandExecutor {


    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender  Source of the command
     * @param cmd Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand( CommandSender sender,  Command cmd,  String label,  String[] args) {
        if (cmd.getName().equalsIgnoreCase("barter")) {
            if (args.length == 0) {
                if (sender.hasPermission("malmoserverplugin.help")) {
                    sender.sendMessage(ChatColor.GRAY + "Commands:");
                    sender.sendMessage(ChatColor.GREEN + "/barter trade <player> <offeredItem> <amount> <requestedItem> <amount> - Request a trade with a player");
                    sender.sendMessage(ChatColor.GREEN + "/barter accept [player] - Accept a trade request from a player");
                    sender.sendMessage(ChatColor.GREEN + "/barter deny [player] - Deny a trade request from a player");
                    sender.sendMessage(ChatColor.GREEN + "/barter cancel [player] - Cancel a trade request to a player");
                    sender.sendMessage(ChatColor.GREEN + "/barter openTrade <player> - Opens an insecure trade with a player");
                    sender.sendMessage(ChatColor.GREEN + "/help - Display this help message");                }
                return true;    // Return true because the command was executed successfully
            } else {
                if (args[0].equalsIgnoreCase("help")) {
                    if (sender.hasPermission("malmoserverplugin.help")) {
                        sender.sendMessage(ChatColor.GRAY + "Commands:");
                        sender.sendMessage(ChatColor.GREEN + "/barter trade <player> <offeredItem> <amount> <requestedItem> <amount> - Request a trade with a player");
                        sender.sendMessage(ChatColor.GREEN + "/barter accept [player] - Accept a trade request from a player");
                        sender.sendMessage(ChatColor.GREEN + "/barter deny [player] - Deny a trade request from a player");
                        sender.sendMessage(ChatColor.GREEN + "/barter cancel [player] - Cancel a trade request to a player");
                        sender.sendMessage(ChatColor.GREEN + "/barter openTrade <player> - Opens an insecure trade with a player");
                        sender.sendMessage(ChatColor.GREEN + "/help - Display this help message");
                    }
                    return true;    // Return true because the command was executed successfully
                } else if (args[0].equalsIgnoreCase("openTrade")) {
                    if (sender.hasPermission("malmoserverplugin.openTrade")) {
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            if (args.length == 2) {
                                Player target = Bukkit.getPlayer(args[1]);
                                if (target != null) {
                                    if (target.isOnline()) {
                                        if (player.getNearbyEntities(5,5,5).contains(target)) {
                                            if (target != player) {
                                                MalmoServerPlugin.inst().getLogger().log(Level.INFO, player.getName() + " is trading with " + target.getName());
                                                player.openInventory(target.getInventory());
                                                target.openInventory(player.getInventory());
                                                player.sendMessage(ChatColor.GREEN + "You are now trading with " + target.getName());
                                                target.sendMessage(ChatColor.GREEN + player.getName() + " is now trading with you");
                                            } else {
                                                player.sendMessage(ChatColor.RED + "You cannot trade with yourself");
                                            }
                                        } else {
                                            player.sendMessage(ChatColor.RED + "The player you are trying to trade with is not nearby");
                                            return true;
                                        }
                                    } else {
                                        player.sendMessage(ChatColor.RED + "That player is not online");
                                    }
                                } else {
                                    player.sendMessage(ChatColor.RED + "That player is not online");
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "You must specify a player to trade with");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "You must be a player to use this command");
                        }
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("trade")) {
                    if (sender.hasPermission("malmoserverplugin.trade.request")) {
                        if (args.length == 4) {
                            Player requested = Bukkit.getPlayer(args[1]);
                            if (requested != null) {
                                if (requested.isOnline()) {
                                    if (requested != sender) {
                                        if (sender instanceof Player) {
                                            Player requester = (Player) sender;
                                            try {
                                                TradeController.sendTradeRequest(requester, requested, new Trade(requester.getInventory().getItemInMainHand(),  Integer.parseInt(args[3]), new ItemStack(Material.getMaterial(args[2].toUpperCase())))) ;
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                sender.sendMessage(ChatColor.RED + "Error");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else if (args.length == 6) {
                            Player requested = Bukkit.getPlayer(args[1]);
                            if (requested != null) {
                                if (requested.isOnline()) {
                                    if (requested != sender) {
                                        if (sender instanceof Player) {
                                            Player requester = (Player) sender;
                                            try {
                                                if (requester.getInventory().containsAtLeast(new ItemStack(Material.getMaterial(args[2].toUpperCase())), Integer.parseInt(args[3]))) {
                                                    TradeController.sendTradeRequest(requester, requested, new Trade(new ItemStack(Material.getMaterial(args[2].toUpperCase())), Integer.parseInt(args[3]), new ItemStack(Material.getMaterial(args[4].toUpperCase())), Integer.parseInt(args[5])));
                                                } else {
                                                    requester.sendMessage(ChatColor.RED + "You do not have enough of that item to trade");
                                                }
                                            } catch (IllegalArgumentException e) {
                                                requester.sendMessage(ChatColor.DARK_RED + " Invalid Item");
                                                return false;
                                            }

                                        }
                                    }
                                }
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "You must specify a player to trade with, the item you want to trade, the amount of that item you want to trade, the item you want to receive, and the amount of that item you want to receive");
                        }

                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("deny")) {
                    if (sender.hasPermission("malmoserverplugin.trade.deny")) {
                        if (args.length == 2) {
                            Player requester = Bukkit.getPlayer(args[1]);
                            if (requester != null) {
                                if (requester.isOnline()) {
                                    if (requester != sender) {
                                        if (sender instanceof Player) {
                                            Player requested = (Player) sender;
                                            TradeController.denyTrade(requested, requester);
                                        }
                                    }
                                }
                            }
                        } else {
                            if (sender instanceof Player) {
                                TradeController.denyRecentTrade((Player) sender);
                            }

                        }

                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("accept")) {

                    if (sender.hasPermission("malmoserverplugin.trade.accept")) {
                        if (args.length == 2) {
                            Player requester = Bukkit.getPlayer(args[1]);
                            if (requester != null) {
                                if (requester.isOnline()) {
                                    if (requester != sender) {
                                        if (sender instanceof Player) {
                                            Player requested = (Player) sender;
                                            TradeController.acceptTrade(requested, requester);
                                        }
                                    }
                                }
                            }
                        } else {
                            if (sender instanceof Player) {
                                TradeController.acceptRecentTrade((Player) sender);
                            }
                        }
                    }
                    return true;
                 } else if (args[0].equalsIgnoreCase("cancel")) {
                    if (sender.hasPermission("malmoserverplugin.trade.cancel")) {
                        if (args.length == 2) {
                            Player requested = Bukkit.getPlayer(args[1]);
                            if (requested != null) {
                                if (requested.isOnline()) {
                                    if (requested != sender) {
                                        if (sender instanceof Player) {
                                            Player requester = (Player) sender;
                                            TradeController.cancelTrade(requester, requested);
                                        }
                                    }
                                }
                            }
                        } else {
                            if (sender instanceof Player) {
                                TradeController.cancelRecentTrade((Player) sender);
                            }
                        }
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("list")) {
                    if (sender.hasPermission("malmoserverplugin.trade.list")) {
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            List<TradeRequest> trades = TradeController.getAllPlayerTradeRequests(player);
                            if (trades.size() > 0) {
                                player.sendMessage(ChatColor.BLUE + "Trades:");
                                for (TradeRequest traderequest : trades) {
                                    player.sendMessage(ChatColor.BLUE+ traderequest.toString());
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "You have no trades");
                            }
                        }
                    }
                    return true;
                }
            else {
                    sender.sendMessage(ChatColor.RED + "Invalid command");
                    return false;
                }
            }
        }

        return false;
    }

}
