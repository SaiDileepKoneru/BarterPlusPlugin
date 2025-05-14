package com.crashcringle.barterplus.barterkings.players;

import com.cjcrafter.openai.chat.ChatMessage;
import com.cjcrafter.openai.chat.ChatRequest;
import com.cjcrafter.openai.chat.tool.Function;
import com.cjcrafter.openai.threads.message.CreateThreadMessageRequest;
import com.cjcrafter.openai.threads.message.ThreadUser;
import com.cjcrafter.openai.threads.runs.CreateRunRequest;
import com.crashcringle.barterplus.BarterPlus;
import com.crashcringle.barterplus.barterkings.BarterKings;
import com.crashcringle.barterplus.barterkings.ai.GPTService;
import com.crashcringle.barterplus.barterkings.ai.GeminiChatMessage;
import com.crashcringle.barterplus.barterkings.ai.GeminiChatRequest;
import com.crashcringle.barterplus.barterkings.ai.GeminiService;
import com.crashcringle.barterplus.barterkings.trades.TradeRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeminiNPC extends NpcParticipant {

    public GeminiNPC(Player player) {
        super(player);
    }

    public GeminiNPC(NPC npc) {
        super(npc);
    }

    List<GeminiChatMessage> globalGeminiMessages = new ArrayList<>();
    Map<Player, List<GeminiChatMessage>> geminiChatMessages = new HashMap<>();

    @Override
    public void addMessage(Player player, ChatMessage message) {
        if (!chatMessages.containsKey(player)) {
            chatMessages.put(player, new ArrayList<>());
            geminiChatMessages.put(player, new ArrayList<>());
        }
        chatMessages.get(player).add(message);
        geminiChatMessages.get(player).add(new GeminiChatMessage("user", message.getContent()));
    }

    public void addMessage(Player player, GeminiChatMessage message) {
        if (!geminiChatMessages.containsKey(player)) {
            geminiChatMessages.put(player, new ArrayList<>());
            chatMessages.put(player, new ArrayList<>());
        }
        geminiChatMessages.get(player).add(message);
        chatMessages.get(player).add(ChatMessage.toUserMessage(message.getContent()));
    }

    @Override
    public List<ChatMessage> getMessages(Player player) {
        return chatMessages.get(player);
    }

    public List<GeminiChatMessage> getGeminiMessages(Player player) {
        return geminiChatMessages.get(player);
    }

    @Override
    public void addGlobalMessage(ChatMessage message) {
        globalMessages.add(message);
        globalGeminiMessages.add(new GeminiChatMessage("user", message.getContent()));
    }

    public void addGlobalMessage(GeminiChatMessage message) {
        globalGeminiMessages.add(message);
        globalMessages.add(ChatMessage.toUserMessage(message.getContent()));
    }

    @Override
    public void resetMessages() {
        chatMessages.clear();
        geminiChatMessages.clear();
    }

    @Override
    public void chunkMessage(String message) {chunkedMessage = chunkedMessage + "\n" + message;
    }

    @Override
    public void processMessage(String message) {
        chunkedMessage += message;
        GeminiNPC npcParticipant = this;
        try {
            isGenerating = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    isGenerating = true;
                    //                 try {
                    BarterKings.geminiService.processGeminiMessage(npcParticipant, new GeminiChatMessage("user", chunkedMessage));
//                    } catch (Exception e) {
//                        // Lets let ChatGPT know it made a mistake so it can correct itself
//                        BarterPlus.inst().getLogger().warning("ChatMessage: "+chunkedMessage);
//                        String json = "{\"error\": \"" + e.getMessage() + "\"}";
//                        BarterPlus.inst().getLogger().warning("Hallucination Error: " + e.getMessage());
//
//                        BarterPlus.inst().getLogger().warning("\n\n**Last Request: " + npcParticipant.getRequest().getMessages().get(npcParticipant.getRequest().getMessages().size()-1).getContent());
//                        BarterKings.gptService.processChatGPTMessage(npcParticipant, ChatMessage.toSystemMessage(json));
//
//                    }

                    chunkedMessage = "";
                }
                // Random between 1 and 6 seconds
            }.runTaskLaterAsynchronously(BarterPlus.inst(), 20L * Math.max(0, (BarterPlus.inst().globalBufferTime + (long) (Math.random() * 49))));
        } catch (Exception e) {
            // Lets let ChatGPT know it made a mistake so it can correct itself
            BarterPlus.inst().getLogger().warning("ChatMessage: "+chunkedMessage);
            String json = "{\"error\": \"" + e.getMessage() + "\"}";
            BarterPlus.inst().getLogger().warning("Hallucination Error: " + e.getMessage());

            BarterPlus.inst().getLogger().warning("\n\n**Last Request: " + this.getRequest().getMessages().get(this.getRequest().getMessages().size()-1).getContent());
            BarterKings.geminiService.processGeminiMessage(npcParticipant, new GeminiChatMessage("system", json));
        }
        BarterPlus.inst().globalBufferTime += 1;
        BarterPlus.inst().getLogger().info("Global buffer time: "+BarterPlus.inst().globalBufferTime);
        // Check if a minute has passed since the current time
        if (System.currentTimeMillis() - BarterPlus.inst().lastTime > 60000) {
            BarterPlus.inst().globalBufferTime = 0;
            BarterPlus.inst().getLogger().info("Resetting global buffer time");
            BarterPlus.inst().lastTime = System.currentTimeMillis();
        }
    }
    @Override
    public void queueMessage(String message) {
        if (isGenerating) {
            chunkMessage(message);
        } else {
            processMessage(message);
        }
    }

    @Override
    public void queueMessage2(String message) {
    }

    @Override
    public void processMessage2(String message) {

    }

    public void sendTradeRequest(TradeRequest tradeRequest) {
        BarterPlus.inst().getLogger().info("Queueing message for " + this.getName() + ": " + tradeRequest.toNPCString());
        queueMessage(tradeRequest.toNPCString());
    }

    @Override
    public void denyTradeRequest(TradeRequest tradeRequest, boolean initiator) {
        if (initiator) {
            queueMessage("You have declined the trade request from "+tradeRequest.getRequester().getName());
        } else {
            queueMessage("Your trade request with "+tradeRequest.getRequested().getName()+" has been declined.");
        }
    }
    @Override

    public void acceptTradeRequest(TradeRequest tradeRequest, boolean initiator) {
        if (initiator) {
            queueMessage("You have accepted the trade request from "+tradeRequest.getRequester().getName());
        } else {
            queueMessage("Your trade request with "+tradeRequest.getRequested().getName()+" has been accepted.");
        }

    }
    @Override

    public void cancelTradeRequest(TradeRequest tradeRequest, boolean initiator) {
        if (initiator) {
            queueMessage("You have cancelled the trade request with "+tradeRequest.getRequested().getName());
        } else {
            queueMessage("Your trade request with "+tradeRequest.getRequester().getName()+" has been cancelled.");
        }
    }
    @Override
    public void initializeAgent() {
        // thread = BarterKings.gptService.getOpenai().threads().create();
        String prompt = GeminiService.generateSystemPrompt(this);
        globalMessages.add(ChatMessage.toSystemMessage(prompt));
        globalGeminiMessages.add(new GeminiChatMessage("user", prompt));
    }



    public GeminiChatRequest getGeminiRequest() {
        // Construct the JSON payload dynamically
        ObjectMapper objectMapper = new ObjectMapper();
        GeminiChatRequest builder = new GeminiChatRequest()
                .setModel(BarterPlus.inst().geminiModel)
                .setMessages(globalGeminiMessages)
                .addTool("check_desires", "Check the items that you need in your inventory to obtain to gain points.", null)
                .addTool("do_nothing", "Do not send any chat, message, or response as you deem appropriate.", null)
                .addTool("check_inventory", "Returns a list of items in your inventory.", null)
                .addTool("trade", "Propose a 1:1 trade to the player. You can only have a maximum of one active trade with each person at a time.", createTradeParams(objectMapper))
                .addTool("multi_trade", "Propose a trade to the player involving multiple items. You can exchange up to 6 items for up to 6 items. You can only have a maximum of one active trade with each person at a time.", createMultiTradeParams(objectMapper))
                .addTool("accept_trade", "Accept a trade request. This completes the trade and exchanges the items if successful.", createAcceptTradeParams(objectMapper))
                .addTool("private_message", "Send a private message to a player, Only the receiving player can see this message (Use a non-function call text for global messages). You can also reply to private messages with this", createPrivateMessageParams(objectMapper))
                .addTool("decline_trade", "Decline a trade request. This completes the trade request by rejecting it.", createDeclineTradeParams(objectMapper))
                .addTool("rescind_trade", "Rescind a trade request. This completes the trade request by rescinding it.", createRescindTradeParams(objectMapper))
                .addTool("check_score", "Checks the value of all items in your inventory and returns your current score. Items worth 0 pts are worth nothing to you. Check your desires to see what items are worth points.", null)
                .addTool("check_trades", "List trades you were involved in.", createCheckTradesParams(objectMapper))
                .setTemperature(BarterPlus.inst().temperature)
                .setTopP(BarterPlus.inst().topP)
//                .setTopK(40)
                .setMaxOutputTokens(3192);

        return builder;
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

    private static ObjectNode createMultiTradeParams(ObjectMapper objectMapper) {
        ObjectNode parameters = objectMapper.createObjectNode();
        parameters.put("type", "object");
        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("player", createProperty(objectMapper, "string", "The player you are trading with."));
        for (int i = 1; i <= 6; i++) {
            properties.set("offeredItem" + i, createProperty(objectMapper, "string", "The item you are offering (item " + i + ")"));
            properties.set("offeredQty" + i, createProperty(objectMapper, "integer", "The quantity of the item you are offering (item " + i + ")"));
            properties.set("requestedItem" + i, createProperty(objectMapper, "string", "The item you want to receive (item " + i + ")"));
            properties.set("requestedQty" + i, createProperty(objectMapper, "integer", "The quantity of the item you want to receive (item " + i + ")"));
        }
        parameters.set("properties", properties);
        parameters.set("required", objectMapper.createArrayNode().add("player").add("offeredItem1").add("offeredQty1").add("requestedItem1").add("requestedQty1"));
        return parameters;
    }

    private static ObjectNode createAcceptTradeParams(ObjectMapper objectMapper) {
        ObjectNode parameters = objectMapper.createObjectNode();
        parameters.put("type", "object");
        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("player", createProperty(objectMapper, "string", "The player whose request you wish to accept. Omit parameter to accept the most recent request."));
        parameters.set("properties", properties);
        parameters.set("required", objectMapper.createArrayNode().add("player"));
        return parameters;
    }

    private static ObjectNode createPrivateMessageParams(ObjectMapper objectMapper) {
        ObjectNode parameters = objectMapper.createObjectNode();
        parameters.put("type", "object");
        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("player", createProperty(objectMapper, "string", "The player to send the message to."));
        properties.set("message", createProperty(objectMapper, "string", "The message to send."));
        parameters.set("properties", properties);
        parameters.set("required", objectMapper.createArrayNode().add("player").add("message"));
        return parameters;
    }

    private static ObjectNode createDeclineTradeParams(ObjectMapper objectMapper) {
        ObjectNode parameters = objectMapper.createObjectNode();
        parameters.put("type", "object");
        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("player", createProperty(objectMapper, "string", "The player whose request you wish to decline. Omit parameter to decline the most recent request."));
        parameters.set("properties", properties);
        return parameters;
    }

    private static ObjectNode createRescindTradeParams(ObjectMapper objectMapper) {
        ObjectNode parameters = objectMapper.createObjectNode();
        parameters.put("type", "object");
        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("player", createProperty(objectMapper, "string", "The player to rescind your request from. Omit to rescind your most recent request."));
        parameters.set("properties", properties);
        return parameters;
    }

    private static ObjectNode createCheckTradesParams(ObjectMapper objectMapper) {
        ObjectNode parameters = objectMapper.createObjectNode();
        parameters.put("type", "object");
        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("incoming", createProperty(objectMapper, "boolean", "True to show incoming trades, else shows outgoing trades, don't specify to show all"));
        properties.set("completed", createProperty(objectMapper, "boolean", "Whether to list completed trades."));
        parameters.set("properties", properties);
        parameters.set("required", objectMapper.createArrayNode().add("completed"));
        return parameters;
    }

    private static ObjectNode createProperty(ObjectMapper objectMapper, String type, String description) {
        ObjectNode property = objectMapper.createObjectNode();
        property.put("type", type);
        property.put("description", description);
        return property;
    }

    @Override
    public ChatRequest getRequest() {
        return null;
    }

    @Override
    public List<ChatMessage> getGlobalMessages() {
        return globalMessages;
    }

    public List<GeminiChatMessage> getGlobalGeminiMessages() {
        return globalGeminiMessages;
    }

    public void setGlobalMessages(List<ChatMessage> globalMessages) {

        this.globalMessages = globalMessages;
        for (ChatMessage message : globalMessages) {
            globalGeminiMessages.add(new GeminiChatMessage("user", message.getContent()));
        }
    }

}
