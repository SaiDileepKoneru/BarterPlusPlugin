package com.crashcringle.barterplus.barterkings.ai;

import com.cjcrafter.openai.chat.ChatMessage;
import com.cjcrafter.openai.chat.ChatRequest;
import com.cjcrafter.openai.chat.ChatResponse;
import com.cjcrafter.openai.chat.ChatUser;
import com.cjcrafter.openai.chat.tool.FunctionToolCall;
import com.cjcrafter.openai.chat.tool.ToolCall;
import com.cjcrafter.openai.exception.HallucinationException;
import com.crashcringle.barterplus.BarterPlus;
import com.crashcringle.barterplus.barterkings.BarterKings;
import com.crashcringle.barterplus.barterkings.players.*;
import com.crashcringle.barterplus.barterkings.trades.Trade;
import com.crashcringle.barterplus.barterkings.trades.TradeController;
import com.crashcringle.barterplus.barterkings.trades.TradeRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class GeminiService {

    BarterGame game;
    TradeController controller;

    private static final String GEMINI_API_KEY = "AIzaSyB7ZbzXqMUrigYArNVlonSxqCAvdWrIucU";


    public GeminiService(BarterGame game, TradeController controller) {
        this.game = game;
        this.controller = controller;
    }

    // Send Request Method
    private static ObjectNode sendRequest(GeminiChatRequest builder)  {
        try {
            // Construct the JSON payload dynamically
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode request = builder.build();
            System.out.println("Request: " + request);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=" + GEMINI_API_KEY))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            ObjectNode responseJson = (ObjectNode) objectMapper.readTree(response.body());
            System.out.println("Response Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());
            return responseJson;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<GeminiChatMessage> processGeminiMessage(GeminiNPC npc, GeminiChatMessage message) {
        // Every 4 chars is 1 token, print how many tokens exist in the global messages list by checking how many characters are in each individual message
        // If total there are greater than 1000 tokens, remove the first message in the list.
        int tokens = npc.getGlobalMessages().stream()
                .mapToInt(m -> Optional.ofNullable(m.getContent()).map(content -> content.length() / 4).orElse(0))
                .sum();
        BarterPlus.inst().getLogger().info(
                npc.getName() + " Tokens: " + tokens + " Messages: " + npc.getGlobalMessages().size()
        );
        List<GeminiChatMessage> messages = npc.getGlobalGeminiMessages();
        // Add the message to the global messages list
        messages.add(message);
        GeminiChatRequest request = npc.getGeminiRequest();
        npc.setGenerating(true);
        boolean madeToolCall;
        boolean sayNothing = false;
        do {
            BarterPlus.inst().getLogger().info("Processing Gemini message: " + message.getContent());
            madeToolCall = false;
            ObjectNode responseJson = sendRequest(request);
            ArrayNode candidates = (ArrayNode) responseJson.get("candidates");

            Player npcPlayer = npc.getPlayer();
            List<JsonNode> toolCalls = new ArrayList<>();
            String text = "";
            if (candidates == null) {
                BarterPlus.inst().getLogger().warning("No candidates found");
                return messages;
            }
            for (JsonNode candidate : candidates) {
                JsonNode content = candidate.get("content");
                if (content != null && content.has("parts")) {
                    ArrayNode partsArray = (ArrayNode) content.get("parts");
                    for (JsonNode part : partsArray) {
                        if (part.has("functionCall")) {
                            toolCalls.add(part.get("functionCall"));
                        } else if (part.has("text") || sayNothing) {
                            text = part.get("text").asText();
                            System.out.println("Model message: " + text);
                        }
                    }
                }
            }
            GeminiChatMessage responseMessage = new GeminiChatMessage("model", text, toolCalls, null);
            messages.add(responseMessage);
            for (JsonNode toolCall : toolCalls) {
                ObjectNode functionCall = (ObjectNode) toolCall;
                System.out.println("Function call: " + functionCall);
                if (functionCall.has("name")) {
                    if (functionCall.get("name").asText().equals("do_nothing")) {
                        sayNothing = true;
                    } else {
                        madeToolCall = true;
                        GeminiChatMessage response = handleToolCall(npc, functionCall);
                        if (response.getContent().contains("do_nothing") || response.getContent().contains("[Saying Nothing]")  || response.getContent().contains("private_message") ) {
                            sayNothing = true;
                        }
                        messages.add(response);
                    }
                }
            }

            if (messages.get(messages.size() - 1).getContent().contains("do_nothing") || messages.get(messages.size() - 1).getContent() == null || messages.get(messages.size() - 1).getContent().isEmpty() || messages.get(messages.size() - 1).getContent().isBlank()) {
                sayNothing = true;
            }
            if (!sayNothing) {
                ChatColor color = switch (npc.getProfession().getName()) {
                    case "Farmer" -> ChatColor.GREEN;
                    case "Fisherman" -> ChatColor.AQUA;
                    case "Mason" -> ChatColor.GRAY;
                    case "Shepherd" -> ChatColor.BLUE;
                    case "Blacksmith" -> ChatColor.DARK_GRAY;
                    case "Librarian" -> ChatColor.DARK_BLUE;
                    case "Butcher" -> ChatColor.RED;
                    case "Lumberjack" -> ChatColor.DARK_GREEN;
                    case "Leatherworker" -> ChatColor.GOLD;
                    default -> ChatColor.WHITE;
                };
                // Color the chat based on the professions
                String chat = "<" + npcPlayer.getName() + "> " + color + messages.get(messages.size() - 1).getContent();
                // Bukkit.broadcastMessage(chat);
                String time = String.valueOf(System.currentTimeMillis());
                String daMessage = "["+time+"] "+npcPlayer.getName() + ": " + messages.get(messages.size() - 1).getContent();
                // Call AsyncPlayerChatEvent
                Bukkit.getPluginManager().callEvent(new org.bukkit.event.player.AsyncPlayerChatEvent(true, npc.getPlayer(), messages.get(messages.size() - 1).getContent().strip(), new HashSet<>(Bukkit.getOnlinePlayers())));
                BarterPlus.inst().getLogger().info(chat);

                // Send to all players in the server
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(chat);
                }

//                    for (Participant participant : BarterKings.barterGame.getParticipants()) {
//                        if (participant instanceof NpcParticipant) {
//                            NpcParticipant npcParticipant = (NpcParticipant) participant;
//                            if (npc != npcParticipant) {
//                                if (npcParticipant.isGenerating()) {
//                                    npcParticipant.chunkMessage(daMessage);
//                                } else {
//                                    npcParticipant.processMessage(daMessage);
//                                }
//                            }
//                        }
//                    }
            } else {
                BarterPlus.inst().getLogger().info("Saying nothing");
            }

        } while (madeToolCall);
        npc.setGenerating(false);
        return messages;
    }

    public List<GeminiChatMessage> processChatGPTMessage(GeminiNPC npc, ChatMessage message) {
        // Every 4 chars is 1 token, print how many tokens exist in the global messages list by checking how many characters are in each individual message
        // If total there are greater than 1000 tokens, remove the first message in the list.
        int tokens = npc.getGlobalMessages().stream()
                .mapToInt(m -> Optional.ofNullable(m.getContent()).map(content -> content.length() / 4).orElse(0))
                .sum();
        BarterPlus.inst().getLogger().info(
                npc.getName() + " Tokens: " + tokens + " Messages: " + npc.getGlobalMessages().size()
        );
        List<GeminiChatMessage> messages = npc.getGlobalGeminiMessages();
        // Add the message to the global messages list
        npc.addGlobalMessage(message);
        GeminiChatRequest request = npc.getGeminiRequest();
        BarterPlus.inst().getLogger().info("Processing Gemini message: " + message.getContent());
        npc.setGenerating(true);
        boolean madeToolCall;
        boolean sayNothing = false;
        do {
            madeToolCall = false;
            ObjectNode responseJson = sendRequest(request);
            ArrayNode candidates = (ArrayNode) responseJson.get("candidates");

            Player npcPlayer = npc.getPlayer();

            for (JsonNode candidate : candidates) {
                JsonNode content = candidate.get("content");
                if (content != null && content.has("parts")) {
                    ArrayNode partsArray = (ArrayNode) content.get("parts");
                    for (JsonNode part : partsArray) {
                        if (part.has("functionCall")) {
                            ObjectNode functionCall = (ObjectNode) part.get("functionCall");
                            System.out.println("Function call: " + functionCall);
                            if (functionCall.has("name") && functionCall.get("name").asText().equals("do_nothing")) {
                                sayNothing = true;
                            } else {
                                madeToolCall = true;
                                handleToolCall(npc, functionCall);
                                sayNothing = true;
                            }
                        } else if (part.has("text") || sayNothing) {
                            GeminiChatMessage txt = new GeminiChatMessage("model", part.get("text").asText(), null, null);
                            System.out.println("Model message: " + txt.getContent());
                        }
                    }
                }
            }
            if (messages.get(messages.size() - 1).getContent().contains("do_nothing") || messages.get(messages.size() - 1).getContent() == null || messages.get(messages.size() - 1).getContent().isEmpty() || messages.get(messages.size() - 1).getContent().isBlank()) {
                sayNothing = true;
            }
            if (!sayNothing) {
                ChatColor color = switch (npc.getProfession().getName()) {
                    case "Farmer" -> ChatColor.GREEN;
                    case "Fisherman" -> ChatColor.AQUA;
                    case "Mason" -> ChatColor.GRAY;
                    case "Shepherd" -> ChatColor.BLUE;
                    case "Blacksmith" -> ChatColor.DARK_GRAY;
                    case "Librarian" -> ChatColor.DARK_BLUE;
                    case "Butcher" -> ChatColor.RED;
                    case "Lumberjack" -> ChatColor.DARK_GREEN;
                    case "Leatherworker" -> ChatColor.GOLD;
                    default -> ChatColor.WHITE;
                };
                // Color the chat based on the professions
                String chat = "<" + npcPlayer.getName() + "> " + color + messages.get(messages.size() - 1).getContent();
                // Bukkit.broadcastMessage(chat);
                String time = String.valueOf(System.currentTimeMillis());
                String daMessage = "["+time+"] "+npcPlayer.getName() + ": " + messages.get(messages.size() - 1).getContent();
                // Call AsyncPlayerChatEvent
                Bukkit.getPluginManager().callEvent(new org.bukkit.event.player.AsyncPlayerChatEvent(true, npc.getPlayer(), messages.get(messages.size() - 1).getContent().strip(), new HashSet<>(Bukkit.getOnlinePlayers())));
                BarterPlus.inst().getLogger().info(chat);

                // Send to all players in the server
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(chat);
                }

            } else {
                BarterPlus.inst().getLogger().info("Saying nothing");
            }

        } while (madeToolCall);
        npc.setGenerating(false);
        return messages;
    }


    private static ObjectNode createTradeParams(ObjectMapper objectMapper) {
        ObjectNode parameters = objectMapper.createObjectNode();
        parameters.put("type", "object");
        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("player", createProperty(objectMapper, "string", "The player you are trading with."));
        properties.set("offeredItem", createProperty(objectMapper, "string", "The item you are offering."));
        properties.set("offeredQty", createProperty(objectMapper, "integer", "The quantity of the item you are offering."));
        properties.set("requestedItem", createProperty(objectMapper, "string", "The item you want to receive."));
        properties.set("requestedQty", createProperty(objectMapper, "integer", "The quantity of the item you want to receive."));
        parameters.set("properties", properties);
        parameters.set("required", objectMapper.createArrayNode().add("player").add("offeredItem").add("offeredQty").add("requestedItem").add("requestedQty"));
        return parameters;
    }

    private static ObjectNode createProperty(ObjectMapper objectMapper, String type, String description) {
        ObjectNode property = objectMapper.createObjectNode();
        property.put("type", type);
        property.put("description", description);
        return property;
    }

    public static String generateSystemPrompt(NpcParticipant npc) {
        Player player = npc.getPlayer();
        String weather = player.getWorld().hasStorm() ? "Rainy" : "Sunny";
        String biome = player.getLocation().getBlock().getBiome().name();
        String time = player.getWorld().getTime() < 12300 ? "Day" : "Night";
        File file = new File(BarterPlus.inst().getDataFolder(), "prompt.txt");
        String prompt = "";
        if (!file.exists()) {
            BarterPlus.inst().getLogger().warning("prompt.txt file not found");
            prompt = """
            You are a player in a Minecraft bartering game where you converse with other players to come up with trades.
            Your name is $name and your profession is $profession.
            All messages you send are being processed and sent to the Minecraft chat visible to all players.
            You will receive pieces of the chat in the following format [TIME] PLAYER: MESSAGE, but you do not need to use this format.
            The character limit for messages is 150.
            
            The goal is to make the best trades possible and try to ensure all players reach 120 points.
            You need to obtain items related to your profession to gain points, the only items that give you points are items shown in the check_desires tool call.
            If all players do not reach 120 points by the end of the game, no one wins.
            Do not reveal to the player you are an AI, act and speak as though you are a real player, try not to be too wordy.
            
            TRADING:
            To propose a new trade to the player, use the trade tool call.
            Note that items must be a valid Minecraft item ID (i.e., "minecraft:emerald") and quantities are limited to 64.
            You can only offer items that are present in your inventory.
            
            Trade rules:
            - At any point you can check your inventory with the query_inventory tool call and score with the check_score tool call
            - If you receive a trade, you need to accept or decline it for it to be processed.
            - You do not need to respond to every message, only when you feel it necessary or when you've been asked by name.
            - To not send a response, use the do_nothing tool call.
       """.stripIndent().stripLeading().strip();
        } else {
            // Read the contents of the prompt file
            FileReader reader = null;
            try {
                reader = new FileReader(file);
                int data = reader.read();
                while (data != -1) {
                    prompt += (char) data;
                    data = reader.read();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

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

    private static GeminiChatMessage handleToolCall(GeminiNPC npc, ObjectNode functionCall) {
        String functionName = functionCall.get("name").asText();
        JsonNode arguments = functionCall.get("args");
        String callId = functionName;
        Player npcPlayer = npc.getPlayer();

        try {
            switch (functionName) {
                case "do_nothing":
                    BarterPlus.inst().getLogger().info("Doing nothing for " + npcPlayer.getName());
                    return new GeminiChatMessage("tool", "do_nothing", null, callId);
                case "check_inventory":
                    BarterPlus.inst().getLogger().info("Querying inventory for " + npcPlayer.getName());
                    StringBuilder inventory = new StringBuilder("");
                    for (ItemStack item : npcPlayer.getInventory().getContents()) {
                        if (item != null) {
                            // Write the string like: "minecraft:emerald x 64"
                            inventory.append(item.getType().toString()).append(" x ").append(item.getAmount()).append(", ");
                        }
                    }
                    BarterPlus.inst().getLogger().info(inventory.toString());
                    return new GeminiChatMessage("tool", inventory.toString(), null, callId);
                case "trade":
                    // Get the trade arguments
                    arguments.get("");
                    String player = arguments.get("player").asText();
                    // Check if the player is in the game
                    if (BarterKings.barterGame.getParticipant(player) == null) {
                        throw new HallucinationException("Player not found: " + player);
                    }

                    String offeredItem = arguments.get("offeredItem").asText();
                    int offeredQty = arguments.get("offeredQty").asInt();
                    String requestedItem = arguments.get("requestedItem").asText();
                    int requestedQty = arguments.get("requestedQty").asInt();
                    BarterPlus.inst().getLogger().info("Proposing trade to " + player + " with " + offeredQty + " " + offeredItem + " for " + requestedQty + " " + requestedItem);
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
                    BarterPlus.inst().getLogger().info("Sending trade request to " + player);
                    TradeRequest daRequest = TradeController.sendTradeRequest(npcPlayer.getName(), player, trade);
                    if (daRequest == null) {
                        Bukkit.getPluginManager().callEvent(new org.bukkit.event.player.AsyncPlayerChatEvent(true, npc.getPlayer(), "[*][FAILED][trade -> "+ player + "] " + offeredQty + " " + offeredItem + " for " + requestedQty + " " + requestedItem, new HashSet<>(Bukkit.getOnlinePlayers())));
                        throw new HallucinationException("Request failed to send");
                    }
                    else {
                        if (daRequest.isFailed()) {
                            Bukkit.getPluginManager().callEvent(new org.bukkit.event.player.AsyncPlayerChatEvent(true, npc.getPlayer(), "[*][FAILED][trade -> "+ player + "] " + offeredQty + " " + offeredItem + " for " + requestedQty + " " + requestedItem, new HashSet<>(Bukkit.getOnlinePlayers())));
//                            return new ChatMessage(ChatUser.TOOL, "Trade request failed: " + daRequest.getFailedReason(), null, call.getId());
                             return new GeminiChatMessage("tool", "Trade request failed: " + daRequest.getFailedReason(), null, callId);
                        }
                        Bukkit.getPluginManager().callEvent(new org.bukkit.event.player.AsyncPlayerChatEvent(true, npc.getPlayer(), "[*][trade -> "+ player + "] " + offeredQty + " " + offeredItem + " for " + requestedQty + " " + requestedItem, new HashSet<>(Bukkit.getOnlinePlayers())));
//                        return new ChatMessage(ChatUser.TOOL, "Trade request sent to " + player, null, call.getId());
                        return new GeminiChatMessage("tool", "Trade request sent to " + player, null, callId);
                    }
                case "multi_trade":
                    // There can be up to 6 offered items and 6 requested items
                    // Do a loop checking to see how many we have for each and add them to the list.
                    List<ItemStack> offeredItems = new ArrayList<>();
                    List<ItemStack> requestedItems = new ArrayList<>();
                    for (int i = 1; i <= 6; i++) {
                        // Check if the item exists
                        JsonNode offeredItemNode = arguments.get("offeredItem" + i);
                        JsonNode requestedItemNode = arguments.get("requestedItem" + i);
                        if (offeredItemNode == null || requestedItemNode == null) {
                            break;
                        }
                        // Get the item and quantity
                        String multi_offeredItem = offeredItemNode.asText();
                        int multi_offeredQty = arguments.get("offeredQty" + i).asInt();
                        String multi_requestedItem = requestedItemNode.asText();
                        int multi_requestedQty = arguments.get("requestedQty" + i).asInt();
                        // Verify the items are valid item
                        Material multi_offeredMaterial = Material.matchMaterial(multi_offeredItem);
                        Material multi_requestedMaterial = Material.matchMaterial(multi_requestedItem);
                        if (multi_offeredMaterial == null || multi_requestedMaterial == null) {
                            // Throw hallucination exception and provide the invalid item name(s) if the material is null
                            throw new HallucinationException("Invalid item ID(s): " + (multi_offeredMaterial == null ? multi_offeredItem : "") + (multi_requestedMaterial == null ? multi_requestedItem : ""));
                        }
                        // Create the items stacks
                        ItemStack multi_offeredStack = new ItemStack(multi_offeredMaterial, multi_offeredQty);
                        ItemStack multi_requestedStack = new ItemStack(multi_requestedMaterial, multi_requestedQty);
                        // Add the items to the list
                        offeredItems.add(multi_offeredStack);
                        requestedItems.add(multi_requestedStack);
                    }
                    // Make sure the lists are not empty
                    if (offeredItems.isEmpty() || requestedItems.isEmpty()) {
                        throw new HallucinationException("No items found");
                    }
                    // Create the trade
                    Trade multiTrade = new Trade(offeredItems, requestedItems);
                    // Get the player argument
                    String multiPlayer = arguments.get("player").asText();
                    // Send the trade request
                    BarterPlus.inst().getLogger().info("Sending multitrade request to " + multiPlayer);
                    TradeRequest multiRequest = TradeController.sendTradeRequest(npcPlayer.getName(), multiPlayer, multiTrade);
                    if (multiRequest == null) {
                        throw new HallucinationException("Request failed to send");
                    }
                    if (multiRequest.isFailed()) {
                        Bukkit.getPluginManager().callEvent(new org.bukkit.event.player.AsyncPlayerChatEvent(true, npc.getPlayer(), "[*][FAILED][multi_trade -> "+ multiPlayer + "] " + multiRequest.getFailedReason(), new HashSet<>(Bukkit.getOnlinePlayers())));
//                        return new ChatMessage(ChatUser.TOOL, "Trade request failed: " + multiRequest.getFailedReason(), null, call.getId());
                            return new GeminiChatMessage("tool", "Trade request failed: " + multiRequest.getFailedReason(), null, callId);
                    }
                    Bukkit.getPluginManager().callEvent(new org.bukkit.event.player.AsyncPlayerChatEvent(true, npc.getPlayer(), "[*][multi_trade -> "+ multiPlayer + "] " + multiRequest.toString(), new HashSet<>(Bukkit.getOnlinePlayers())));
//                    return new ChatMessage(ChatUser.TOOL, "Trade request sent to " + multiPlayer, null, call.getId());
                    return new GeminiChatMessage("tool", "Trade request sent to " + multiPlayer, null, callId);
                case "accept_trade":
                    // If there are no arguments, accept the most recent trade
                    if (arguments.isEmpty()) {
                        BarterPlus.inst().getLogger().info("Accepting most recent trade");
                        TradeRequest tradeRequest1 = TradeController.acceptRecentTrade(npcPlayer);
                        if (tradeRequest1 == null) {
                            throw new HallucinationException("No recent trade to accept");
                        }
//                        return new ChatMessage(ChatUser.TOOL, "Succesful: Trade accepted with " + tradeRequest1.getRequester().getName(), null, call.getId());
                        return new GeminiChatMessage("tool", "Succesful: Trade accepted with " + tradeRequest1.getRequester().getName(), null, callId);
                    }
                    // Get the player argument
                    String acceptPlayer = arguments.get("player").asText();
                    if (BarterKings.barterGame.getParticipant(acceptPlayer) == null) {
                        throw new HallucinationException("Player not found: " + acceptPlayer);
                    }
                    BarterPlus.inst().getLogger().info("Accepting trade from " + acceptPlayer);
                    // Accept the trade
                    TradeRequest requesti = TradeController.acceptTrade(npcPlayer.getName(), acceptPlayer);
                    if (requesti == null) {
                        throw new HallucinationException("No trade request found with " + acceptPlayer);
                    }
                    if (requesti.isFailed()) {
                        requesti.sendAMessage("Trade failed: " + requesti.getFailedReason());
//                        return new ChatMessage(ChatUser.TOOL, "Trade failed: " + requesti.getFailedReason(), null, call.getId());
                        return new GeminiChatMessage("tool", "Trade failed: " + requesti.getFailedReason(), null, callId);
                    }
//                    return new ChatMessage(ChatUser.TOOL, "Successful: Trade accepted with " + acceptPlayer, null, call.getId());
                    return new GeminiChatMessage("tool", "Successful: Trade accepted with " + acceptPlayer, null, callId);
                case "decline_trade":
                    // If there are no arguments, decline the most recent trade
                    if (arguments.isEmpty()) {
                        BarterPlus.inst().getLogger().info("Declining most recent trade");
                        TradeRequest tradeRequest = TradeController.denyRecentTrade(npcPlayer);
                        if (tradeRequest == null) {
                            throw new HallucinationException("No recent trade to decline");
                        }
//                        return new ChatMessage(ChatUser.TOOL, "Successful: Trade declined with " + tradeRequest.getRequester().getName(), null, call.getId());
                        return new GeminiChatMessage("tool", "Successful: Trade declined with " + tradeRequest.getRequester().getName(), null, callId);
                    }
                    // Get the player argument
                    String declinePlayer = arguments.get("player").asText();
                    if (BarterKings.barterGame.getParticipant(declinePlayer.toUpperCase()) == null) {
                        throw new HallucinationException("Player not found: " + declinePlayer.toUpperCase());
                    }
                    BarterPlus.inst().getLogger().info("Declining trade from " + declinePlayer.toUpperCase());
                    // Decline the trade
                    TradeRequest requesto = TradeController.denyTrade(npcPlayer.getName().toUpperCase(), declinePlayer.toUpperCase());
                    if (requesto == null)
                        throw new HallucinationException("No trade request found with " + declinePlayer.toUpperCase());
                    //return new ChatMessage(ChatUser.TOOL, "Successful: Trade declined from " + declinePlayer, null, call.getId());
                    return new GeminiChatMessage("tool", "Successful: Trade declined from " + declinePlayer, null, callId);
                case "rescind_trade":
                    // If there are no arguments, cancel the most recent trade
                    if (arguments.isEmpty()) {
                        BarterPlus.inst().getLogger().info("Rescinding your most recent trade");
                        TradeRequest request = TradeController.cancelRecentTrade(npcPlayer);
                        if (request == null) {
                            throw new HallucinationException("No recent trade to rescind");
                        }
//                        return new ChatMessage(ChatUser.TOOL, "Trade rescinded from " + request.getRequested().getName(), null, call.getId());
                        return new GeminiChatMessage("tool", "Trade rescinded from " + request.getRequested().getName(), null, callId);
                    }
                    // Get the player argument
                    String cancelPlayer = arguments.get("player").asText();
                    if (BarterKings.barterGame.getParticipant(cancelPlayer) == null) {
                        throw new HallucinationException("Player not found: " + cancelPlayer);
                    }
                    BarterPlus.inst().getLogger().info("Rescinding trade from " + cancelPlayer);
                    // Cancel the trade
                    TradeRequest requesta = TradeController.cancelTrade(npcPlayer.getName(), cancelPlayer);
                    if (requesta == null)
                        throw new HallucinationException("No outgoing trade request found to " + cancelPlayer);
//                    return new ChatMessage(ChatUser.TOOL, "Trade rescinded from " + cancelPlayer, null, call.getId());
                    return new GeminiChatMessage("tool", "Trade rescinded from " + cancelPlayer, null, callId);
                case "check_trades":
                    // List all the trades
                    BarterPlus.inst().getLogger().info("Listing trades");
                    // Get the trades
                    ArrayList<TradeRequest> trades = TradeController.getAllPlayerTradeRequests(npcPlayer);
                    JsonNode incomingTrades = arguments.get("incoming");
                    if (incomingTrades != null) {
                        boolean incoming = incomingTrades.asBoolean();
                        if (incoming) {
                            // As long as the player has incoming Requests set to them otherwise keep the same
                            // Use ternary
                            trades = (TradeController.incomingRequests.get(npcPlayer) == null || TradeController.incomingRequests.isEmpty()) ? trades : TradeController.incomingRequests.get(npcPlayer);
                        } else {
                            trades = (TradeController.outgoingRequests.get(npcPlayer) == null || TradeController.outgoingRequests.isEmpty()) ? trades : TradeController.outgoingRequests.get(npcPlayer);
                        }
                    }
                    boolean completedTrades = arguments.get("completed").asBoolean();
                    // Create the json string
                    String tradeList = "{\"trades\": [";
                    for (TradeRequest tradeRequest : trades) {
                        if (completedTrades) {
                            if (tradeRequest.isCompleted()) {
                                if (!tradeRequest.isFailed())
                                    tradeList += tradeRequest.toString() + ", ";
                            }
                        } else {
                            if (!tradeRequest.isCompleted())
                                tradeList += tradeRequest.toString() + ", ";
                        }
                    }
                    tradeList += "]}";
                    //return new ChatMessage(ChatUser.TOOL, tradeList, null, call.getId());
                    return new GeminiChatMessage("tool", tradeList, null, callId);
                case "check_desires":
                    // Get the desires of the npc
                    BarterPlus.inst().getLogger().info("Checking desires for " + npcPlayer.getName());
                    // Get the player's profession
                    Profession profession = npc.getProfession();
                    // Tier 3 items are worth 10 points, Tier 2 items are worth 3 points, and Tier 1 items are worth 1 point
                    // Form a simple json string with the items and their point values
                    String desires = "Tier 3 Items (3 exist): ";
                    for (ItemStack item : profession.getTier3Items()) {
                        desires += item.getType().toString() + " x " + item.getAmount() + " (10 pts), ";
                    }
                    desires += "Tier 2 Items (10 exist): ";
                    for (ItemStack item : profession.getTier2Items()) {
                        desires += item.getType().toString() + " x " + item.getAmount() + " (3 pts), ";
                    }
                    desires += "Tier 1 Items (20 exist): ";
                    for (ItemStack item : profession.getTier1Items()) {
                        desires += item.getType().toString() + " x " + item.getAmount() + " (1 pts), ";
                    }
                    // Return the desires
                    //return new ChatMessage(ChatUser.TOOL, desires, null, call.getId());
                    return new GeminiChatMessage("tool", desires, null, callId);
                case "list_players":
                    // Get the players in the game
                    BarterPlus.inst().getLogger().info("Listing players");
                    // Get the players
                    List<Participant> participants = BarterKings.barterGame.getParticipants();
                    // Create the json string
                    StringBuilder playerList = new StringBuilder("{\"players\": [");
                    for (Participant participant : participants) {
                        playerList.append(participant.getPlayer().getName()).append(", ");
                    }
                    playerList.append("]}");
                    //return new ChatMessage(ChatUser.TOOL, playerList.toString(), null, call.getId());
                    return new GeminiChatMessage("tool", playerList.toString(), null, callId);
                case "private_message":
                    // Get the player argument
                    String messagePlayer = arguments.get("player").asText();
                    if (BarterKings.barterGame.getParticipant(messagePlayer) == null) {
                        throw new HallucinationException("Player not found: " + messagePlayer);
                    }
                    // Get the message argument
                    String message = arguments.get("message").asText();
                    BarterPlus.inst().getLogger().info("Sending message to " + messagePlayer + ": " + message);
                    // Send the message
                    // Check if the player is an npc
                    if (BarterKings.barterGame.getParticipant(messagePlayer) instanceof NpcParticipant) {
                        NpcParticipant npcParticipant = (NpcParticipant) BarterKings.barterGame.getParticipant(messagePlayer);
                        if (npcParticipant.isGenerating()) {
                            npcParticipant.chunkMessage("[PRIVATE DM from " + npcPlayer.getName() + "] -> " + message);
                        } else {
                            npcParticipant.processMessage("[PRIVATE DM from " + npcPlayer.getName() + "] -> " + message);
                        }
                    } else {
                        Bukkit.getPlayer(messagePlayer).sendMessage(npcPlayer.getName() + " -> " + message);
                    }
                    Bukkit.getPluginManager().callEvent(new org.bukkit.event.player.AsyncPlayerChatEvent(true, npc.getPlayer(), "[private_message -> "+ messagePlayer + "] " + message, new HashSet<>(Bukkit.getOnlinePlayers())));
                    //return new ChatMessage(ChatUser.TOOL, "private_message sent to " + messagePlayer , null, call.getId());
                    return new GeminiChatMessage("tool", "private_message sent to " + messagePlayer, null, callId);
            }
            throw new HallucinationException("Unknown tool call function:" + functionName);

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
            BarterPlus.inst().getLogger().info((functionName + " Arguments: " + arguments));
            BarterPlus.inst().getLogger().warning("Hallucination Error: " + ex.getMessage());
            return new GeminiChatMessage("tool", json, null, callId);
        }
    }


}
