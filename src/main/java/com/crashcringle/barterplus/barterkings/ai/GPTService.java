package com.crashcringle.barterplus.barterkings.ai;

import com.cjcrafter.openai.OpenAI;
import com.cjcrafter.openai.assistants.Assistant;
import com.cjcrafter.openai.chat.*;
import com.cjcrafter.openai.chat.tool.*;
import com.cjcrafter.openai.exception.HallucinationException;
import com.cjcrafter.openai.threads.Thread;
import com.cjcrafter.openai.threads.message.TextContent;
import com.cjcrafter.openai.threads.message.ThreadMessage;
import com.cjcrafter.openai.threads.message.ThreadMessageContent;
import com.cjcrafter.openai.threads.runs.MessageCreationDetails;
import com.cjcrafter.openai.threads.runs.Run;
import com.cjcrafter.openai.threads.runs.RunStep;
import com.crashcringle.barterplus.BarterPlus;
import com.crashcringle.barterplus.barterkings.BarterKings;
import com.crashcringle.barterplus.barterkings.players.BarterGame;
import com.crashcringle.barterplus.barterkings.players.Participant;
import com.crashcringle.barterplus.barterkings.players.Profession;
import com.crashcringle.barterplus.barterkings.trades.Trade;
import com.crashcringle.barterplus.barterkings.trades.TradeController;
import com.crashcringle.barterplus.barterkings.trades.TradeRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.crashcringle.barterplus.barterkings.players.NpcParticipant;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class GPTService {
    String key;
    @Getter
    OpenAI openai;
    @Getter
    Assistant assistant;
    BarterGame game;
    TradeController controller;


    public GPTService(BarterGame game, TradeController controller) {
        key = "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";
        openai = OpenAI.builder()
                .apiKey(key)
                .build();
        assistant = openai.assistants().retrieve("asst_SdbFT7Znc78YldySYVWweJYL");
        this.game = game;
        this.controller = controller;

        // Check config for "openai-model" if not found use "gpt-3.5-turbo"
    }


    public static ChatMessage handleToolCall(NpcParticipant npc, ToolCall call, List<Tool> validTools) {

        if (!BarterKings.barterGame.inProgress()) {
            return new ChatMessage(ChatUser.TOOL, "The Game is Over, no more tool calls.", null, call.getId());
        }
        try {
            if (call.getType() != Tool.Type.FUNCTION)
                throw new HallucinationException("Unknown tool call type: " + call.getType());
            FunctionCall function = ((FunctionToolCall) call).getFunction();
            if (function.getName().contains("multi_tool_use.parallel")) {
                BarterPlus.inst().getLogger().info("Multi-tool use is not supported, Please call functions individually");
                return new ChatMessage(ChatUser.TOOL, "Multi-tool use is not supported, Please call functions individually", null, call.getId());
            }
            Map<String, JsonNode> arguments = function.tryParseArguments(validTools); // You can pass null here for less strict parsing
            Player npcPlayer = npc.getPlayer();
            BarterPlus.inst().getLogger().info("Tool Call: " + function.getName());
            BarterPlus.inst().getLogger().info("-Arguments: " + arguments);
            switch (function.getName()) {
                case "do_nothing":
                    BarterPlus.inst().getLogger().info("Doing nothing for " + npcPlayer.getName());
                    return new ChatMessage(ChatUser.TOOL, "do_nothing", null, call.getId());
                case "check_inventory":
                    // Get the agents inventory
                    BarterPlus.inst().getLogger().info("Querying inventory for " + npcPlayer.getName());
                    // Append each item in the inventory to a string that isn't null
                    StringBuilder inventory = new StringBuilder("");
                    for (ItemStack item : npcPlayer.getInventory().getContents()) {
                        if (item != null) {
                            // Write the string like: "minecraft:emerald x 64"
                            inventory.append(item.getType().toString()).append(" x ").append(item.getAmount()).append(", ");
                        }
                    }
                    BarterPlus.inst().getLogger().info(inventory.toString());
                    return new ChatMessage(ChatUser.TOOL, inventory.toString(), null, call.getId());
                case "check_score":
                    // Get the score of the npc
                    BarterPlus.inst().getLogger().info("Checking score for " + npcPlayer.getName());
                    // Get the score of the player
                    String scoreBreakdown = npc.getScoreBreakdown();
                    BarterPlus.inst().getLogger().info("Score Breakdown: " + scoreBreakdown);
                    // Return the score breakdown
                    return new ChatMessage(ChatUser.TOOL, scoreBreakdown, null, call.getId());
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
                            return new ChatMessage(ChatUser.TOOL, "Trade request failed: " + daRequest.getFailedReason(), null, call.getId());
                        }
                        Bukkit.getPluginManager().callEvent(new org.bukkit.event.player.AsyncPlayerChatEvent(true, npc.getPlayer(), "[*][trade -> "+ player + "] " + offeredQty + " " + offeredItem + " for " + requestedQty + " " + requestedItem, new HashSet<>(Bukkit.getOnlinePlayers())));
                        return new ChatMessage(ChatUser.TOOL, "Trade request sent to " + player, null, call.getId());
                    }
                case "accept_trade":
                    // If there are no arguments, accept the most recent trade
                    if (arguments.isEmpty()) {
                        BarterPlus.inst().getLogger().info("Accepting most recent trade");
                        TradeRequest tradeRequest1 = TradeController.acceptRecentTrade(npcPlayer);
                        if (tradeRequest1 == null) {
                            throw new HallucinationException("No recent trade to accept");
                        }
                        return new ChatMessage(ChatUser.TOOL, "Succesful: Trade accepted with " + tradeRequest1.getRequester().getName(), null, call.getId());
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
                        return new ChatMessage(ChatUser.TOOL, "Trade failed: " + requesti.getFailedReason(), null, call.getId());
                    }
                    return new ChatMessage(ChatUser.TOOL, "Successful: Trade accepted with " + acceptPlayer, null, call.getId());
                case "decline_trade":
                    // If there are no arguments, decline the most recent trade
                    if (arguments.isEmpty()) {
                        BarterPlus.inst().getLogger().info("Declining most recent trade");
                        TradeRequest tradeRequest = TradeController.denyRecentTrade(npcPlayer);
                        if (tradeRequest == null) {
                            throw new HallucinationException("No recent trade to decline");
                        }
                        return new ChatMessage(ChatUser.TOOL, "Successful: Trade declined with " + tradeRequest.getRequester().getName(), null, call.getId());
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
                    return new ChatMessage(ChatUser.TOOL, "Successful: Trade declined from " + declinePlayer, null, call.getId());
                case "rescind_trade":
                    // If there are no arguments, cancel the most recent trade
                    if (arguments.isEmpty()) {
                        BarterPlus.inst().getLogger().info("Rescinding your most recent trade");
                        TradeRequest request = TradeController.cancelRecentTrade(npcPlayer);
                        if (request == null) {
                            throw new HallucinationException("No recent trade to rescind");
                        }
                        return new ChatMessage(ChatUser.TOOL, "Trade rescinded from " + request.getRequested().getName(), null, call.getId());
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
                    return new ChatMessage(ChatUser.TOOL, "Trade rescinded from " + cancelPlayer, null, call.getId());
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
                    return new ChatMessage(ChatUser.TOOL, tradeList, null, call.getId());
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
                    return new ChatMessage(ChatUser.TOOL, desires, null, call.getId());
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
                    return new ChatMessage(ChatUser.TOOL, playerList.toString(), null, call.getId());
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
                    return new ChatMessage(ChatUser.TOOL, "private_message sent to " + messagePlayer , null, call.getId());
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
            BarterPlus.inst().getLogger().info(((FunctionToolCall) call).getFunction().getName() + " Arguments: " + ((FunctionToolCall) call).getFunction().getArguments());
            BarterPlus.inst().getLogger().warning("Hallucination Error: " + ex.getMessage());
            return new ChatMessage(ChatUser.TOOL, json, null, call.getId());
        }
    }

    public static double solveEquation(String equation) {
        //Expression expression = new Expression(equation);
        return 2.4;
    }


    public List<ChatMessage> processChatGPTMessage(NpcParticipant npc, ChatMessage message)  {

        // Every 4 chars is 1 token, print how many tokens exist in the global messages list by checking how many characters are in each individual message
        // If total there are greater than 1000 tokens, remove the first message in the list.
        int tokens = npc.getGlobalMessages().stream()
                .mapToInt(m -> Optional.ofNullable(m.getContent()).map(content -> content.length() / 4).orElse(0))
                .sum();
        BarterPlus.inst().getLogger().info(
                npc.getName() + " Tokens: " + tokens + " Messages: " + npc.getGlobalMessages().size()
        );

//        if (tokens > 1900) {
//            // Keep removing the second message until the tokens are less than 1800
//            while (tokens > 1800) {
//                BarterPlus.inst().getLogger().info("Removing message: " + npc.getGlobalMessages().get(1).getContent());
//                List<ChatMessage> newGlobalMessages = new ArrayList<>(npc.getGlobalMessages());
//                newGlobalMessages.remove(2);
//                npc.setGlobalMessages(newGlobalMessages);
//                tokens = npc.getGlobalMessages().stream()
//                        .mapToInt(m -> Optional.ofNullable(m.getContent()).map(content -> content.length() / 4).orElse(0))
//                        .sum();
//            }
//        }

        List<ChatMessage> messages = npc.getGlobalMessages();
        ChatRequest request = npc.getRequest();
        // Add the user's message to the list
        messages.add(message);
        BarterPlus.inst().getLogger().info("Generating Response for " + npc.getPlayer().getName() + "...");
        npc.setGenerating(true);
        //BarterPlus.inst().getLogger().info("\n\nRequest: " + request.toString() +"\n\n");
        // Generate a response
        boolean madeToolCall;
        boolean sayNothing = false;
        do {
            madeToolCall = false;
//            for (ChatResponseChunk chunk : openai.streamChatCompletion(request)) {
//                String delta = chunk.get(0).getDeltaContent();
//                if (delta != null) {
//                }
//
//                // When the response is finished, we can add it to the messages list.
//                if (chunk.get(0).isFinished())
//                    messages.add(chunk.get(0).getMessage());
//            }
            //ChatResponse response2 = openai.createChatCompletion(request);
            // If we get an exception try again 4 times every 10 seconds
            // Use Bukkit scheduler to run after a 10 second delay
            ChatResponse response2 = openai.createChatCompletion(request);
            //BarterPlus.inst().getLogger().info("Response: " + response2.toString());
            messages.add(response2.get(0).getMessage());
            try {
                File file = new File(BarterPlus.inst().getDataFolder().getPath() +"/requests/request-" + request.getUser() + ".json");
                // Write the contents of the request to the file
                FileWriter writer = new FileWriter(file);
                writer.write("********************\n"+request.toString() + "\n\n");
                for (ChatMessage message2 : messages) {
                    writer.write(message2.getContent() + "\n");
                }
                writer.write(response2.get(0).getMessage().toString());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Get a random item out the npcs inventory to trade
            Player npcPlayer = npc.getPlayer();

            // If the API returned a tool call to us, we need to handle it.
            List<ToolCall> toolCalls = messages.get(messages.size() - 1).getToolCalls();
           // BarterPlus.inst().getLogger().info("Response: " + messages.get(messages.size() - 1) + "\n\n");

            if (toolCalls != null) {
                madeToolCall = true;
                for (ToolCall call : toolCalls) {
                    ChatMessage response = handleToolCall(npc, call, request.getTools());

                    if (response.getContent().contains("do_nothing") || response.getContent().contains("[Saying Nothing]")  || response.getContent().contains("private_message") ) {
                        sayNothing = true;
                    }
                    messages.add(response);
                }
            } else {
                if (messages.get(messages.size() - 1).getContent().contains("do_nothing") || messages.get(messages.size() - 1).getContent() == null || messages.get(messages.size() - 1).getContent().isEmpty() || messages.get(messages.size() - 1).getContent().isBlank()) {
                    sayNothing = true;
                }
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
            }

            // Loop until we get a message without tool calls
        } while (madeToolCall);
        npc.setGenerating(false);
        return messages;
    }

    public List<String> processChatGPTMessage2(NpcParticipant npc, Thread thread) {
        Run run = npc.getRun();
        List<String> messages = new ArrayList<>();
        // Once the run stops, we want to retrieve the steps of the run.
        // this includes message outputs, function calls, code
        // interpreters, etc.
        for (RunStep step : openai.threads().runs(thread).steps(run).list().getData()) {
            if (step.getType() != RunStep.Type.MESSAGE_CREATION) {
                BarterPlus.inst().getLogger().info ("Assistant made step: " + step.getType());
                continue;
            }

            // This cast is safe since we checked the type above
            MessageCreationDetails details = (MessageCreationDetails) step.getStepDetails();
            ThreadMessage message = openai.threads().messages(thread).retrieve(details.getMessageCreation().getMessageId());
            for (ThreadMessageContent content : message.getContent()) {
                if (content.getType() != ThreadMessageContent.Type.TEXT) {
                    BarterPlus.inst().getLogger().info ("Unhandled message content type: " + content.getType());
                    BarterPlus.inst().getLogger().info ("This will never occur since this Assistant doesn't use images.");
                }

                BarterPlus.inst().getLogger().info (((TextContent) content).getText().getValue());
                messages.add(((TextContent) content).getText().getValue());
            }
        }
        return messages;
    }

//    public List<ChatMessage> processChatGPTMessage(NpcParticipant npc, ChatMessage message) {
//
//        // Every 4 chars is 1 token, print how many tokens exist in the global messages list by checking how many characters are in each individual message
//        // If total there are greater than 1000 tokens, remove the first message in the list.
//        int tokens = npc.getGlobalMessages().stream()
//                .mapToInt(m -> Optional.ofNullable(m.getContent()).map(content -> content.length() / 4).orElse(0))
//                .sum();
//        BarterPlus.inst().getLogger().info(
//                npc.getName() + " Tokens: " + tokens + " Messages: " + npc.getGlobalMessages().size()
//        );
//
//        if (tokens > 1900) {
//            // Keep removing the second message until the tokens are less than 1800
//            while (tokens > 1800) {
//                BarterPlus.inst().getLogger().info("Removing message: " + npc.getGlobalMessages().get(1).getContent());
//                List<ChatMessage> newGlobalMessages = new ArrayList<>(npc.getGlobalMessages());
//                newGlobalMessages.remove(2);
//                npc.setGlobalMessages(newGlobalMessages);
//                tokens = npc.getGlobalMessages().stream()
//                        .mapToInt(m -> Optional.ofNullable(m.getContent()).map(content -> content.length() / 4).orElse(0))
//                        .sum();
//            }
//        }
//        List<ChatMessage> messages = npc.getGlobalMessages();
//        ChatRequest request = npc.getRequest();
//        BarterPlus.inst().getLogger().info("Sending Request for " + npc.getPlayer().getName() + "...\n\n");
//        //BarterPlus.inst().getLogger().info("Request: " + request.toString());
//        // Add the user's message to the list
//        messages.add(message);
//        BarterPlus.inst().getLogger().info("Generating Response for " + npc.getPlayer().getName() + "...");
//        npc.setGenerating(true);
//
//        // Generate a response
//        boolean madeToolCall;
//        boolean sayNothing = false;
//        do {
//            madeToolCall = false;
//            for (ChatResponseChunk chunk : openai.streamChatCompletion(request)) {
//                String delta = chunk.get(0).getDeltaContent();
//                if (delta != null) {
//
//                }
//
//
//                // When the response is finished, we can add it to the messages list.
//                if (chunk.get(0).isFinished())
//                    messages.add(chunk.get(0).getMessage());
//            }
//
//            // Get a random item out the npcs inventory to trade
//            Player npcPlayer = npc.getPlayer();
//
//            // If the API returned a tool call to us, we need to handle it.
//            List<ToolCall> toolCalls = messages.get(messages.size() - 1).getToolCalls();
//            if (toolCalls != null) {
//                madeToolCall = true;
//                for (ToolCall call : toolCalls) {
//                    ChatMessage response = handleToolCall(npc, call, request.getTools());
//                    if (response.getContent().contains("do_nothing")) {
//                        sayNothing = true;
//                    }
//                    messages.add(response);
//                }
//            } else {
//                if (!sayNothing) {
//                    ChatColor color = switch (npc.getProfession().getName()) {
//                        case "Farmer" -> ChatColor.GREEN;
//                        case "Fisherman" -> ChatColor.AQUA;
//                        case "Mason" -> ChatColor.GRAY;
//                        case "Shepherd" -> ChatColor.WHITE;
//                        case "Blacksmith" -> ChatColor.DARK_GRAY;
//                        case "Librarian" -> ChatColor.DARK_BLUE;
//                        case "Butcher" -> ChatColor.RED;
//                        case "Lumberjack" -> ChatColor.DARK_GREEN;
//                        case "Leatherworker" -> ChatColor.GOLD;
//                        default -> ChatColor.WHITE;
//                    };
//                    // Color the chat based on the professions
//                    String chat = "<" + npcPlayer.getName() + "> " + color + messages.get(messages.size() - 1).getContent();
//                    Bukkit.broadcastMessage(chat);
//                    String time = String.valueOf(System.currentTimeMillis());
//                    String daMessage = "["+time+"] "+npcPlayer.getName() + ": " + messages.get(messages.size() - 1).getContent();
//
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
//                } else {
//                    BarterPlus.inst().getLogger().info("Saying nothing");
//                }
//            }
//
//            // Loop until we get a message without tool calls
//        } while (madeToolCall);
//        npc.setGenerating(false);
//        return messages;
//    }

//    public List<ChatMessage> processChatGPTMessage(NpcParticipant npc, ChatMessage message) {
//
//        // Every 4 chars is 1 token, print how many tokens exist in the global messages list by checking how many characters are in each individual message
//        // If total there are greater than 1000 tokens, remove the first message in the list.
//        int tokens = npc.getGlobalMessages().stream()
//                .mapToInt(m -> Optional.ofNullable(m.getContent()).map(content -> content.length() / 4).orElse(0))
//                .sum();
//        BarterPlus.inst().getLogger().info(
//                npc.getName() + " Tokens: " + tokens + " Messages: " + npc.getGlobalMessages().size()
//        );
//
//        if (tokens > 1900) {
//            // Keep removing the second message until the tokens are less than 1800
//            while (tokens > 1800) {
//                BarterPlus.inst().getLogger().info("Removing message: " + npc.getGlobalMessages().get(1).getContent());
//                List<ChatMessage> newGlobalMessages = new ArrayList<>(npc.getGlobalMessages());
//                newGlobalMessages.remove(2);
//                npc.setGlobalMessages(newGlobalMessages);
//                tokens = npc.getGlobalMessages().stream()
//                        .mapToInt(m -> Optional.ofNullable(m.getContent()).map(content -> content.length() / 4).orElse(0))
//                        .sum();
//            }
//        }
//        List<ChatMessage> messages = npc.getGlobalMessages();
//        ChatRequest request = npc.getRequest();
//        BarterPlus.inst().getLogger().info("Sending Request for " + npc.getPlayer().getName() + "...\n\n");
//        //BarterPlus.inst().getLogger().info("Request: " + request.toString());
//        // Add the user's message to the list
//        messages.add(message);
//        BarterPlus.inst().getLogger().info("Generating Response for " + npc.getPlayer().getName() + "...");
//        npc.setGenerating(true);
//
//        // Generate a response
//        boolean madeToolCall;
//        boolean sayNothing = false;
//        do {
//            madeToolCall = false;
//            for (ChatResponseChunk chunk : openai.streamChatCompletion(request)) {
//                String delta = chunk.get(0).getDeltaContent();
//                if (delta != null) {
//
//                }
//
//
//                // When the response is finished, we can add it to the messages list.
//                if (chunk.get(0).isFinished())
//                    messages.add(chunk.get(0).getMessage());
//            }
//
//            // Get a random item out the npcs inventory to trade
//            Player npcPlayer = npc.getPlayer();
//
//            // If the API returned a tool call to us, we need to handle it.
//            List<ToolCall> toolCalls = messages.get(messages.size() - 1).getToolCalls();
//            if (toolCalls != null) {
//                madeToolCall = true;
//                for (ToolCall call : toolCalls) {
//                    ChatMessage response = handleToolCall(npc, call, request.getTools());
//                    if (response.getContent().contains("do_nothing")) {
//                        sayNothing = true;
//                    }
//                    messages.add(response);
//                }
//            } else {
//                if (!sayNothing) {
//                    ChatColor color = switch (npc.getProfession().getName()) {
//                        case "Farmer" -> ChatColor.GREEN;
//                        case "Fisherman" -> ChatColor.AQUA;
//                        case "Mason" -> ChatColor.GRAY;
//                        case "Shepherd" -> ChatColor.WHITE;
//                        case "Blacksmith" -> ChatColor.DARK_GRAY;
//                        case "Librarian" -> ChatColor.DARK_BLUE;
//                        case "Butcher" -> ChatColor.RED;
//                        case "Lumberjack" -> ChatColor.DARK_GREEN;
//                        case "Leatherworker" -> ChatColor.GOLD;
//                        default -> ChatColor.WHITE;
//                    };
//                    // Color the chat based on the professions
//                    String chat = "<" + npcPlayer.getName() + "> " + color + messages.get(messages.size() - 1).getContent();
//                    Bukkit.broadcastMessage(chat);
//                    String time = String.valueOf(System.currentTimeMillis());
//                    String daMessage = "["+time+"] "+npcPlayer.getName() + ": " + messages.get(messages.size() - 1).getContent();
//
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
//                } else {
//                    BarterPlus.inst().getLogger().info("Saying nothing");
//                }
//            }
//
//            // Loop until we get a message without tool calls
//        } while (madeToolCall);
//        npc.setGenerating(false);
//        return messages;
//    }
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

}
