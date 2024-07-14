package crashcringle.malmoserverplugin.barterkings.ai;

import com.cjcrafter.openai.OpenAI;
import com.cjcrafter.openai.chat.ChatMessage;
import com.cjcrafter.openai.chat.ChatRequest;
import com.cjcrafter.openai.chat.ChatResponseChunk;
import com.cjcrafter.openai.chat.ChatUser;
import com.cjcrafter.openai.chat.tool.*;
import com.cjcrafter.openai.exception.HallucinationException;
import com.fasterxml.jackson.databind.JsonNode;
import crashcringle.malmoserverplugin.MalmoServerPlugin;
import crashcringle.malmoserverplugin.barterkings.BarterKings;
import crashcringle.malmoserverplugin.barterkings.players.BarterGame;
import crashcringle.malmoserverplugin.barterkings.players.NpcParticipant;
import crashcringle.malmoserverplugin.barterkings.players.Participant;
import crashcringle.malmoserverplugin.barterkings.players.Profession;
import crashcringle.malmoserverplugin.barterkings.trades.Trade;
import crashcringle.malmoserverplugin.barterkings.trades.TradeController;
import crashcringle.malmoserverplugin.barterkings.trades.TradeRequest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;


public class GPTService {
    String model;
    String key;
    OpenAI openai;
    BarterGame game;
    TradeController controller;


    public GPTService(BarterGame game, TradeController controller) {
        model = "gpt-3.5-turbo";
        key = "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";
        openai = OpenAI.builder()
                .apiKey(key)
                .build();
        this.game = game;
        this.controller = controller;

        // Check config for "openai-model" if not found use "gpt-3.5-turbo"
    }


    public static ChatMessage handleToolCall(NpcParticipant npc, ToolCall call, List<Tool> validTools) {
        // The try-catch here is *crucial*. ChatGPT *isn't very good*
        // at tool calls (And you probably aren't very good at prompt
        // engineering yet!). OpenAI will often "Hallucinate" arguments.
        try {
            if (call.getType() != Tool.Type.FUNCTION)
                throw new HallucinationException("Unknown tool call type: " + call.getType());

            FunctionCall function = ((FunctionToolCall) call).getFunction();
            Map<String, JsonNode> arguments = function.tryParseArguments(validTools); // You can pass null here for less strict parsing
            Player npcPlayer = npc.getPlayer();

            switch (function.getName()) {
                case "do_nothing":
                    MalmoServerPlugin.inst().getLogger().info("Doing nothing for " + npcPlayer.getName());
                    return new ChatMessage(ChatUser.TOOL, "{\"do_nothing\": \"Nothing done\"}", null, call.getId());
                case "query_inventory":
                    // Get the agents inventory
                    MalmoServerPlugin.inst().getLogger().info("Querying inventory for " + npcPlayer.getName());
                    MalmoServerPlugin.inst().getLogger().info("Inventory: " + Arrays.toString(npcPlayer.getInventory().getContents()));
                    String json = "{\"inventory\": " + Arrays.toString(npcPlayer.getInventory().getContents()) + "}";
                    return new ChatMessage(ChatUser.TOOL, json, null, call.getId());
                case "check_score":
                    // Get the score of the npc
                    MalmoServerPlugin.inst().getLogger().info("Checking score for " + npcPlayer.getName());
                    // Get the score of the player
                    String scoreBreakdown = npc.getScoreBreakdown();
                    // Return the score breakdown
                    return new ChatMessage(ChatUser.TOOL, "{\"score\": " + scoreBreakdown + "}", null, call.getId());
                case "trade":
                    // Get the trade arguments
                    String player = arguments.get("player").asText();
                    // Check if the player is in the game
                    if (BarterKings.barterGame.getParticipant(player) == null) {
                        throw new HallucinationException("Player not found: " + player);
                    }

                    String offeredItem = arguments.get("offeredItem").asText();
                    int offeredQty = arguments.get("offeredQty").asInt();
                    String requestedItem = arguments.get("requestedItem").asText();
                    int requestedQty = arguments.get("requestedQty").asInt();
                    MalmoServerPlugin.inst().getLogger().info("Proposing trade to " + player + " with " + offeredQty + " " + offeredItem + " for " + requestedQty + " " + requestedItem);
                    // Trade with the player

                    // Verify the items are valid item
                    Material offeredMaterial = Material.matchMaterial(offeredItem);
                    Material requestedMaterial = Material.matchMaterial(requestedItem);
                    if (offeredMaterial == null || requestedMaterial == null) {
                        // Throw hallucination exception and provide the invalid item name(s) if the material is null
                        throw new HallucinationException("Invalid item ID(s): " + (offeredMaterial == null ? offeredItem : "") + (requestedMaterial == null ? requestedItem : ""));
                    }
                    // Create the items stacks
                    ItemStack offeredStack = new ItemStack(offeredMaterial, offeredQty);
                    ItemStack requestedStack = new ItemStack(requestedMaterial, requestedQty);
                    // Create the trade
                    Trade trade = new Trade(offeredStack, requestedStack);
                    // Send the trade request
                    MalmoServerPlugin.inst().getLogger().info("Sending trade request to " + player);
                    TradeController.sendTradeRequest(npcPlayer.getName(), player, trade);
                    return new ChatMessage(ChatUser.TOOL, "{\"trade\": \"Trade request sent\"}", null, call.getId());
                case "accept_trade":
                    // If there are no arguments, accept the most recent trade
                    if (arguments.isEmpty()) {
                        MalmoServerPlugin.inst().getLogger().info("Accepting most recent trade");

                        return new ChatMessage(ChatUser.TOOL, "{\"trade\": \"Trade accepted with " + TradeController.acceptRecentTrade(npcPlayer).getRequester().getName() + "\"}", null, call.getId());
                    }
                    // Get the player argument
                    String acceptPlayer = arguments.get("player").asText();
                    if (BarterKings.barterGame.getParticipant(acceptPlayer) == null) {
                        throw new HallucinationException("Player not found: " + acceptPlayer);
                    }
                    MalmoServerPlugin.inst().getLogger().info("Accepting trade from " + acceptPlayer);
                    // Accept the trade
                    TradeController.acceptTrade(npcPlayer.getName(), acceptPlayer);
                    return new ChatMessage(ChatUser.TOOL, "{\"trade\": \"Trade accepted with " + acceptPlayer + "\"}", null, call.getId());
                case "decline_trade":
                    // If there are no arguments, decline the most recent trade
                    if (arguments.isEmpty()) {
                        MalmoServerPlugin.inst().getLogger().info("Declining most recent trade");

                        return new ChatMessage(ChatUser.TOOL, "{\"trade\": \"Trade declined with " + TradeController.denyRecentTrade(npcPlayer).getRequester().getName() + "\"}", null, call.getId());
                    }
                    // Get the player argument
                    String declinePlayer = arguments.get("player").asText();
                    if (BarterKings.barterGame.getParticipant(declinePlayer) == null) {
                        throw new HallucinationException("Player not found: " + declinePlayer);
                    }
                    MalmoServerPlugin.inst().getLogger().info("Declining trade from " + declinePlayer);
                    // Decline the trade
                    TradeController.denyTrade(npcPlayer.getName(), declinePlayer);
                    return new ChatMessage(ChatUser.TOOL, "{\"trade\": \"Trade declined from " + declinePlayer + "\"}", null, call.getId());
                case "cancel_trade":
                    // If there are no arguments, cancel the most recent trade
                    if (arguments.isEmpty()) {
                        MalmoServerPlugin.inst().getLogger().info("Cancelling most recent trade");
                        TradeController.cancelRecentTrade(npcPlayer);
                        return new ChatMessage(ChatUser.TOOL, "{\"trade\": \"Trade cancelled with " + TradeController.cancelRecentTrade(npcPlayer).getRequested().getName() + "\"}", null, call.getId());
                    }
                    // Get the player argument
                    String cancelPlayer = arguments.get("player").asText();
                    if (BarterKings.barterGame.getParticipant(cancelPlayer) == null) {
                        throw new HallucinationException("Player not found: " + cancelPlayer);
                    }
                    MalmoServerPlugin.inst().getLogger().info("Cancelling trade with " + cancelPlayer);
                    // Cancel the trade
                    TradeController.cancelTrade(npcPlayer.getName(), cancelPlayer);
                    return new ChatMessage(ChatUser.TOOL, "{\"trade\": \"Trade cancelled with " + cancelPlayer + "\"}", null, call.getId());
                case "list_trades":
                    // List all the trades
                    MalmoServerPlugin.inst().getLogger().info("Listing trades");
                    // Get the trades
                    ArrayList<TradeRequest> trades = TradeController.getAllPlayerTradeRequests(npcPlayer);
                    // Create the json string
                    String tradeList = "{\"trades\": [";
                    for (TradeRequest tradeRequest : trades) {
                        tradeList += tradeRequest.toString() + ", ";
                    }
                    tradeList += "]}";
                    return new ChatMessage(ChatUser.TOOL, tradeList, null, call.getId());
                case "check_desires":
                    // Get the desires of the npc
                    MalmoServerPlugin.inst().getLogger().info("Checking desires for " + npcPlayer.getName());
                    // Get the player's profession
                    Profession profession = npc.getProfession();
                    // Tier 3 items are worth 10 points, Tier 2 items are worth 3 points, and Tier 1 items are worth 1 point
                    // Form a simple json string with the items and their point values
                    String desires = "Tier 3 Items: " + profession.getTier3Items().toString() + " (10 points), " +
                            "Tier 2 Items: " + profession.getTier2Items().toString() + " (3 points), " +
                            "Tier 1 Items: " + profession.getTier1Items().toString() + " (1 point)";
                    // Return the desires
                    return new ChatMessage(ChatUser.TOOL, "{\"desires\": " + desires + "}", null, call.getId());
                case "list_players":
                    // Get the players in the game
                    MalmoServerPlugin.inst().getLogger().info("Listing players");
                    // Get the players
                    List<Participant> participants = BarterKings.barterGame.getParticipants();
                    // Create the json string
                    StringBuilder playerList = new StringBuilder("{\"players\": [");
                    for (Participant participant : participants) {
                        playerList.append(participant.getPlayer().getName()).append(", ");
                    }
                    playerList.append("]}");
                    return new ChatMessage(ChatUser.TOOL, playerList.toString(), null, call.getId());
                case "private_message":
                    // Get the player argument
                    String messagePlayer = arguments.get("player").asText();
                    if (BarterKings.barterGame.getParticipant(messagePlayer) == null) {
                        throw new HallucinationException("Player not found: " + messagePlayer);
                    }
                    // Get the message argument
                    String message = arguments.get("message").asText();
                    MalmoServerPlugin.inst().getLogger().info("Sending message to " + messagePlayer + ": " + message);
                    // Send the message
                    // Check if the player is an npc
                    if (BarterKings.barterGame.getParticipant(messagePlayer) instanceof NpcParticipant) {
                        NpcParticipant npcParticipant = (NpcParticipant) BarterKings.barterGame.getParticipant(messagePlayer);
                        if (npcParticipant.isGenerating()) {
                            npcParticipant.chunkMessage("[Private Message]" + npcPlayer.getName() + " -> " + message);
                        } else {
                            npcParticipant.processMessage("[Private Message]" + npcPlayer.getName() + " -> " + message);
                        }
                    } else {
                        Bukkit.getPlayer(messagePlayer).sendMessage(npcPlayer.getName() + " -> " + message);
                    }
                    return new ChatMessage(ChatUser.TOOL, "{\"message\": \"Private Message sent to " + messagePlayer + "\"}", null, call.getId());
            }
            throw new HallucinationException("Unknown tool call function:" + function.getName());

//            String equation = arguments.get("query_inventory").asText();
//            double result = solveEquation(equation);

//            // NaN implies that the equation was invalid
//            if (Double.isNaN(result))
//                throw new HallucinationException("Format was invalid: " + equation);
//
//            // Add the result to the messages list
//            String json = "{\"result\": " + result + "}";
//            return new ChatMessage(ChatUser.TOOL, json, null, call.getId());

        } catch (HallucinationException ex) {

            // Lets let ChatGPT know it made a mistake so it can correct itself
            String json = "{\"error\": \"" + ex.getMessage() + "\"}";
            MalmoServerPlugin.inst().getLogger().info(((FunctionToolCall) call).getFunction().getName() + " Arguments: " + ((FunctionToolCall) call).getFunction().getArguments());
            MalmoServerPlugin.inst().getLogger().warning("Hallucination Error: " + ex.getMessage());
            return new ChatMessage(ChatUser.TOOL, json, null, call.getId());
        }
    }

    public static double solveEquation(String equation) {
        //Expression expression = new Expression(equation);
        return 2.4;
    }


    public List<ChatMessage> processChatGPTMessage(NpcParticipant npc, ChatMessage message) {

        // Every 4 chars is 1 token, print how many tokens exist in the global messages list by checking how many characters are in each individual message
        // If total there are greater than 1000 tokens, remove the first message in the list.
        int tokens = npc.getGlobalMessages().stream()
                .mapToInt(m -> Optional.ofNullable(m.getContent()).map(content -> content.length() / 4).orElse(0))
                .sum();
        MalmoServerPlugin.inst().getLogger().info(
                npc.getName() + " Tokens: " + tokens + " Messages: " + npc.getGlobalMessages().size()
        );

        if (tokens > 1900) {
            // Keep removing the second message until the tokens are less than 1800
            while (tokens > 1800) {
                MalmoServerPlugin.inst().getLogger().info("Removing message: " + npc.getGlobalMessages().get(1).getContent());
                List<ChatMessage> newGlobalMessages = new ArrayList<>(npc.getGlobalMessages());
                newGlobalMessages.remove(2);
                npc.setGlobalMessages(newGlobalMessages);
                tokens = npc.getGlobalMessages().stream()
                        .mapToInt(m -> Optional.ofNullable(m.getContent()).map(content -> content.length() / 4).orElse(0))
                        .sum();
            }
        }
        List<ChatMessage> messages = npc.getGlobalMessages();
        ChatRequest request = npc.getRequest();
        // Add the user's message to the list
        messages.add(message);
        MalmoServerPlugin.inst().getLogger().info("Generating Response for " + npc.getPlayer().getName() + "...");
        npc.setGenerating(true);

        // Generate a response
        boolean madeToolCall;
        boolean sayNothing = false;
        do {
            madeToolCall = false;
            for (ChatResponseChunk chunk : openai.streamChatCompletion(request)) {
                String delta = chunk.get(0).getDeltaContent();
                if (delta != null) {

                }


                // When the response is finished, we can add it to the messages list.
                if (chunk.get(0).isFinished())
                    messages.add(chunk.get(0).getMessage());
            }

            // Get a random item out the npcs inventory to trade
            Player npcPlayer = npc.getPlayer();

            // If the API returned a tool call to us, we need to handle it.
            List<ToolCall> toolCalls = messages.get(messages.size() - 1).getToolCalls();
            if (toolCalls != null) {
                madeToolCall = true;
                for (ToolCall call : toolCalls) {
                    ChatMessage response = handleToolCall(npc, call, request.getTools());
                    if (response.getContent().contains("do_nothing")) {
                        sayNothing = true;
                    }
                    messages.add(response);
                }
            } else {
                if (!sayNothing) {
                    ChatColor color = switch (npc.getProfession().getName()) {
                        case "Farmer" -> ChatColor.GREEN;
                        case "Fisherman" -> ChatColor.AQUA;
                        case "Mason" -> ChatColor.GRAY;
                        case "Shepherd" -> ChatColor.WHITE;
                        case "Blacksmith" -> ChatColor.DARK_GRAY;
                        case "Librarian" -> ChatColor.DARK_BLUE;
                        case "Butcher" -> ChatColor.RED;
                        case "Lumberjack" -> ChatColor.DARK_GREEN;
                        case "Leatherworker" -> ChatColor.GOLD;
                        default -> ChatColor.WHITE;
                    };
                    // Color the chat based on the professions
                    String chat = "<" + npcPlayer.getName() + "> " + color + messages.get(messages.size() - 1).getContent();
                    Bukkit.broadcastMessage(chat);
                    String time = String.valueOf(System.currentTimeMillis());
                    String daMessage = "["+time+"] "+npcPlayer.getName() + ": " + messages.get(messages.size() - 1).getContent();

                    for (Participant participant : BarterKings.barterGame.getParticipants()) {
                        if (participant instanceof NpcParticipant) {
                            NpcParticipant npcParticipant = (NpcParticipant) participant;
                            if (npc != npcParticipant) {
                                if (npcParticipant.isGenerating()) {
                                    npcParticipant.chunkMessage(daMessage);
                                } else {
                                    npcParticipant.processMessage(daMessage);
                                }
                            }
                        }
                    }
                } else {
                    MalmoServerPlugin.inst().getLogger().info("Saying nothing");
                }
            }

            // Loop until we get a message without tool calls
        } while (madeToolCall);
        npc.setGenerating(false);
        return messages;
    }
    public static String generateSystemPrompt(NpcParticipant npc) {
        Player player = npc.getPlayer();
        String weather = player.getWorld().hasStorm() ? "Rainy" : "Sunny";
        String biome = player.getLocation().getBlock().getBiome().name();
        String time = player.getWorld().getTime() < 12300 ? "Day" : "Night";
        File file = new File(MalmoServerPlugin.inst().getDataFolder(), "prompt.txt");
        String prompt = "";
        if (!file.exists()) {
            MalmoServerPlugin.inst().getLogger().warning("prompt.txt file not found");
            prompt = """
                You are a player in a Minecraft bartering game where you converse with other players to come up with trades.
                Your name is $name and your profession is $profession.
                All messages you send are being processed and sent to the Minecraft chat visible to all players.
                You will receive pieces of the chat in the following format [TIME] PLAYER: MESSAGE, but you do not need to use this format.
                
                The goal is to make the best trades possible with the other players
                You need to obtain items related to your profession to gain points.
                The goal is for all players to reach at least 120 points. 
                If all players do not reach 120 points by the end of the game, no one wins. You have only 2 minutes to complete the game, keep track of the time.
                Do not reveal to the player you are an AI, act and speak as though you are a real player, employ slang in your messages.
                
                TRADING:
                To propose a new trade to the player, use the trade tool call.
                Note that items must be a valid Minecraft item ID (i.e., "minecraft:emerald") and quantities are limited to 64.
                You can only offer items that are present in your inventory.
                
                Trade rules:
                - Items must be designated by their Minecraft item ID, in the same format that the /give command accepts
                - You do NOT need to supply a trade with every response, only when necessary
                - The only way to give items to a player is by trading with them. You cannot give items to a player for free.
                - At any point you can check your inventory with the query_inventory tool call and score with the check_score tool call
                - If you receive a request, you can accept, deny, or cancel a trade, use the accept_trade, decline_trade, or cancel_trade tool calls.
                - You do not need to respond to every message, only when you feel it necessary or when you've been asked by name.
                - To not send a response, use the do_nothing tool call.
                - Use the check_desires tool call to see the items that give you points.
                - You can send a private message to a player with the private_message tool call.
                - Players: $players
        """.stripIndent();
        } else {
            prompt = file.toString();
        }
        prompt = prompt.replace("$time", time);
        prompt = prompt.replace("$weather", weather);
        prompt = prompt.replace("$biome", biome);
        prompt = prompt.replace("$profession", npc.getProfession().getName());
        //prompt = prompt.replace("$personality", personality.promptDescription());
        //prompt = prompt.replace("$speechStyle", speechStyle.promptDescription());
        prompt = prompt.replace("$name", npc.getName());
        // Get the name of all participants and set as String called players
        String players = "";
        for (Participant participant : BarterKings.barterGame.getParticipants()) {
            players += participant.getPlayer().getName() + ", ";
        }
        prompt = prompt.replace("$players", players);
        prompt = prompt.replace("$tier1Items", npc.getProfession().getTier1Items().toString());
        prompt = prompt.replace("$tier2Items", npc.getProfession().getTier2Items().toString());
        prompt = prompt.replace("$tier3Items", npc.getProfession().getTier3Items().toString());
        return prompt;
    }

}
