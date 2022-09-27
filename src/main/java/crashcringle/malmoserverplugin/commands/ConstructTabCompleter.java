package crashcringle.malmoserverplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ConstructTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("barter")) {
            if (args.length == 1) {
                List<String> list = new ArrayList<>();
                list.add("help");
                list.add("trade");
                return list;
            }
            else if (args.length >= 2) {
                if (args[0].equalsIgnoreCase("trade")) {
                    List<String> players = new ArrayList<>();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        players.add(player.getName());
                    }
                    return players;

                }
            }
        }
        return null;
    }
}

