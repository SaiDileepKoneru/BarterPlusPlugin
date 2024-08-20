package com.crashcringle.barterplus.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConstructTabCompleter implements TabCompleter {
    private static final String[] numero = { "1","2","3","4","5","6","7","8","9","0"};

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("barter")) {
            if (args.length == 1) {
                List<String> list = new ArrayList<>();
                list.add("help");
                list.add("list");
                list.add("cancel");
                list.add("trade");
                list.add("openTrade");
                list.add("deny");
                list.add("accept");
                list.add("readyUp");
                list.add("unready");
                list.add("join");
                list.add("leave");
                list.add("start");
                final List<String> completions = new ArrayList<>();
                StringUtil.copyPartialMatches(args[0], list, completions);
                //copy matches of first argument from list (if first arg is 'm' will return just 'minecraft')
                Collections.sort(completions);
                return completions;
            }
            else if (args.length >= 2) {
                if (args[0].equalsIgnoreCase("openTrade")) {
                    List<String> players = new ArrayList<>();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!player.getName().equalsIgnoreCase(sender.getName())) {
                            players.add(player.getName());
                        }                    
                    }
                    return players;

                } else if (args[0].equalsIgnoreCase("trade")) {
                    if (args.length == 2) {
                        List<String> players = new ArrayList<>();
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (!player.getName().equalsIgnoreCase(sender.getName())) {
                                players.add(player.getName());
                            }
                        }
                        return players;
                    } else if (args.length == 3) {
                        final List<String> items = new ArrayList<>();
                        for (Material material : Material.values()) {
                            items.add(material.name());
                        }
                        final List<String> completions = new ArrayList<>();
                        //convert to list
                        StringUtil.copyPartialMatches(args[2], items, completions);
                        //copy matches of first argument from list (if first arg is 'm' will return just 'minecraft')
                        Collections.sort(completions);
                        //you want to sort no?
                        return completions;
                    } else if (args.length == 5) {
                        final List<String> items = new ArrayList<>();
                        for (Material material : Material.values()) {
                            items.add(material.name());
                        }
                        final List<String> completions = new ArrayList<>();
                        //convert to list
                        StringUtil.copyPartialMatches(args[4], items, completions);
                        //copy matches of first argument from list (if first arg is 'm' will return just 'minecraft')
                        Collections.sort(completions);
                        //you want to sort no?
                        return completions;
                    } else {
                        final List<String> completions = new ArrayList<>();
                        //convert to list
                        StringUtil.copyPartialMatches(args[args.length == 6 ? 5 : 3 ], new ArrayList<>(Arrays.asList(numero)), completions);
                        //copy matches of first argument from list (if first arg is 'm' will return just 'minecraft')
                        Collections.sort(completions);
                        //you want to sort no?
                        return completions;
                    }
                } else if (args[0].equalsIgnoreCase("deny") || args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("cancel")) {
                    List<String> players = new ArrayList<>();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!player.getName().equalsIgnoreCase(sender.getName())) {
                            players.add(player.getName());
                        }                    
                    }
                    return players;
                } 
            }
        }
        return null;
    }
}

