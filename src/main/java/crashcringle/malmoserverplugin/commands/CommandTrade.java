package crashcringle.malmoserverplugin.commands;

import crashcringle.malmoserverplugin.MalmoServerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "CommandTrade.onCommand() called");
        if (cmd.getName().equalsIgnoreCase("barter")) {
            if (args.length == 0) {
                if (sender.hasPermission("malmoserverplugin.trade")) {
                    sender.sendMessage("You must specify a player to trade with");
                }
                return true;
            } else {
                if (args[0].equalsIgnoreCase("help")) {
                    if (sender.hasPermission("malmoserverplugin.trade")) {
                        sender.sendMessage("This is the help message");
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("trade")) {
                }
                if (sender.hasPermission("malmoserverplugin.trade")) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        if (args.length == 2) {
                            Player target = Bukkit.getPlayer(args[1]);
                            if (target != null) {
                                if (target.isOnline()) {
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
            }
        }

        return false;
    }



}
