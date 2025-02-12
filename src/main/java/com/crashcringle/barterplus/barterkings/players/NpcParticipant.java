package com.crashcringle.barterplus.barterkings.players;

import com.cjcrafter.openai.chat.ChatMessage;
import com.cjcrafter.openai.chat.ChatRequest;
import com.cjcrafter.openai.chat.tool.Function;
import com.cjcrafter.openai.threads.Thread;
import com.cjcrafter.openai.threads.message.CreateThreadMessageRequest;
import com.cjcrafter.openai.threads.message.ThreadUser;
import com.cjcrafter.openai.threads.runs.CreateRunRequest;
import com.cjcrafter.openai.threads.runs.Run;
import com.crashcringle.barterplus.BarterPlus;
import com.crashcringle.barterplus.barterkings.BarterKings;
import com.crashcringle.barterplus.barterkings.ai.GPTService;
import com.crashcringle.barterplus.barterkings.ai.GeminiChatMessage;
import com.crashcringle.barterplus.barterkings.trades.TradeRequest;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NpcParticipant extends Participant {
    NPC npc;
    @Getter
    @Setter
    String type;
    @Getter
    @Setter
    boolean isGenerating = false;

    @Getter
    @Setter
    boolean isInProgress = false;

    boolean initialized = false;

    List<ChatMessage> globalMessages = new ArrayList<>();

    Thread thread;
    @Getter
    @Setter
    Run run;

    String chunkedMessage = "";
    List<String> chatEvents = new ArrayList<>();

    // map of chat messages per player
    Map<Player, List<ChatMessage>> chatMessages = new HashMap<>();
    public NpcParticipant(Player player) {
        super(player);
    }

    public NpcParticipant(NPC npc) {
        super((Player) npc.getEntity());
        this.npc = npc;
    }

    @Override
    public void setProfession(Profession profession) {
        super.setProfession(profession);
        if (!initialized)
            initializeAgent();
    }
    public void setNPC(NPC npc) {
        this.npc = npc;
    }
    public NPC getNPC() {
        return npc;
    }


    public void addMessage(Player player, ChatMessage message) {
        if (!chatMessages.containsKey(player)) {
            chatMessages.put(player, new ArrayList<>());
        }
        chatMessages.get(player).add(message);
    }
    public void addGlobalMessage(GeminiChatMessage message) {
        ChatMessage chatMessage = ChatMessage.toUserMessage(message.getContent());
        globalMessages.add(chatMessage);
    }

    public List<ChatMessage> getMessages(Player player) {
        return chatMessages.get(player);
    }

    public void addGlobalMessage(ChatMessage message) {
        globalMessages.add(message);
    }

    public void resetMessages() {
        chatMessages.clear();
    }

    public void chunkMessage(String message) {
        chunkedMessage = chunkedMessage + "\n" + message;
    }

    public void processMessage(String message) {
        chunkedMessage += message;
        NpcParticipant npcParticipant = this;
        try {
            isGenerating = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    isGenerating = true;
                    isInProgress = false;
                    try {
                        BarterKings.gptService.processChatGPTMessage(npcParticipant, ChatMessage.toUserMessage(chunkedMessage));
                    } catch (Exception e) {
                        // Lets let ChatGPT know it made a mistake so it can correct itself
                        BarterPlus.inst().getLogger().warning("ChatMessage: "+chunkedMessage);
                        String json = "{\"error\": \"" + e.getMessage() + "\"}";
                        BarterPlus.inst().getLogger().warning("Hallucination Error: " + e.getMessage());
                        BarterPlus.inst().getLogger().warning("\n\n**Last Request: " + npcParticipant.getRequest().getMessages().get(npcParticipant.getRequest().getMessages().size()-1).getContent());
                        BarterKings.gptService.processChatGPTMessage(npcParticipant, ChatMessage.toSystemMessage(json));

                    }
                    chunkedMessage = "";
                }
            // Random between 1 and 6 seconds
            }.runTaskLaterAsynchronously(BarterPlus.inst(), 20L * (BarterPlus.inst().globalBufferTime + (long) (Math.random() * 49)));
        } catch (Exception e) {
            // Lets let ChatGPT know it made a mistake so it can correct itself
            BarterPlus.inst().getLogger().warning("ChatMessage: "+chunkedMessage);
            String json = "{\"error\": \"" + e.getMessage() + "\"}";
            BarterPlus.inst().getLogger().warning("Hallucination Error: " + e.getMessage());

            BarterPlus.inst().getLogger().warning("\n\n**Last Request: " + this.getRequest().getMessages().get(this.getRequest().getMessages().size()-1).getContent());
            BarterKings.gptService.processChatGPTMessage(npcParticipant, ChatMessage.toSystemMessage(json));
            isGenerating = false;
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
    public void queueMessage(String message) {
        if (isGenerating) {
            chunkMessage(message);
        } else {
            processMessage(message);
        }
    }

    public void queueMessage2(String message) {
        BarterKings.gptService.getOpenai().threads().messages(thread).create(CreateThreadMessageRequest.builder()
                .role(ThreadUser.USER)
                .content(message)
                .build());
        if (run != null) {
            if (!run.getStatus().isTerminal()) {
                BarterPlus.inst().getLogger().info("Still generating");
                run =  BarterKings.gptService.getOpenai().threads().runs(thread).retrieve(run);
                isGenerating = true;
            } else {
                BarterPlus.inst().getLogger().info("Generating complete");
                processMessage2(message);
            }
        } else {
            BarterPlus.inst().getLogger().info("Generating");
            processMessage2(message);
        }
    }

    public void processMessage2(String message) {
        BarterKings.gptService.getOpenai().threads().messages(thread).create(CreateThreadMessageRequest.builder()
                .role(ThreadUser.USER)
                .content(message)
                .build());
        BarterPlus.inst().getLogger().info("Processing message1: "+message);

        NpcParticipant npcParticipant = this;
        new BukkitRunnable() {
            @Override
            public void run() {
                run = BarterKings.gptService.getOpenai().threads().runs(thread).create(CreateRunRequest.builder()
                        .assistant(BarterKings.gptService.getAssistant())
                        .build());
                BarterKings.gptService.processChatGPTMessage2(npcParticipant, thread);
            }
        // Random between 1 and 6 seconds
        }.runTaskLaterAsynchronously(BarterPlus.inst(), 20L * (5 + (long) (Math.random() * 30)));
    }

    public void sendTradeRequest(TradeRequest tradeRequest) {
        BarterPlus.inst().getLogger().info("Queueing message for " + this.getName() + ": " + tradeRequest.toNPCString());
        queueMessage(tradeRequest.toNPCString());
    }

    public void denyTradeRequest(TradeRequest tradeRequest, boolean initiator) {
        if (initiator) {
            queueMessage("You have declined the trade request from "+tradeRequest.getRequester().getName());
        } else {
            queueMessage("Your trade request with "+tradeRequest.getRequested().getName()+" has been declined.");
        }
    }

    public void acceptTradeRequest(TradeRequest tradeRequest, boolean initiator) {
        if (initiator) {
            queueMessage("You have accepted the trade request from "+tradeRequest.getRequester().getName());
        } else {
            queueMessage("Your trade request with "+tradeRequest.getRequested().getName()+" has been accepted.");
        }

    }

    public void cancelTradeRequest(TradeRequest tradeRequest, boolean initiator) {
        if (initiator) {
            queueMessage("You have cancelled the trade request with "+tradeRequest.getRequested().getName());
        } else {
            queueMessage("Your trade request with "+tradeRequest.getRequester().getName()+" has been cancelled.");
        }
    }

    public void initializeAgent() {
       // thread = BarterKings.gptService.getOpenai().threads().create();
        String prompt = GPTService.generateSystemPrompt(this);
        globalMessages.add(ChatMessage.toSystemMessage(prompt));
    }

    public ChatRequest getRequest() {
    String model = BarterPlus.inst().model;
    ChatRequest request = ChatRequest.builder()
            .user(this.name + "-" + BarterKings.barterGame.getId())
            .model(model)
            .temperature(BarterPlus.inst().temperature)
            .topP(BarterPlus.inst().topP)
            .seed(BarterPlus.inst().seed)
            .messages(getGlobalMessages())
            .addTool(Function.builder()
                .name("check_desires")
                .description("Check the items that you need in your inventory to obtain to gain points. There are a finite amount of items, you need to make trades with players to collect them. ")
                .noParameters()
                .build()
            ).addTool(Function.builder()
                    .name("do_nothing")
                    .description("Do not send a response as you deem appropriate.")
                    .noParameters()
                    .build()
            ).addTool(Function.builder()
                    .name("check_inventory")
                    .description("Returns a list of items in your inventory.")
                    .noParameters()
                    .build()
            ).addTool(Function.builder()
                    .name("trade")
                    .description("Propose a trade to the player. You can only have a maximum of one active trade with each person at a time.")
                    .addStringParameter("player", "The player you are trading with.", true)
                    .addStringParameter("offeredItem", "The item you are offering.", true)
                    .addIntegerParameter("offeredQty", "The quantity of the item you are offering.", true)
                    .addStringParameter("requestedItem", "The item you want to receive.", true)
                    .addIntegerParameter("requestedQty", "The quantity of the item you want to receive.", true)
                    .build()
            ).addTool(Function.builder()
                    .name("multi_trade")
                    .description("Propose a trade to the player involving multiple items. You can exchange up to 6 items for up to 6 items. You can only have a maximum of one active trade with each person at a time.")
                    .addStringParameter("player", "The player you are trading with.", true)
                    .addStringParameter("offeredItem1", "The first item you are offering", true)
                    .addIntegerParameter("offeredQty1", "The quantity of the first item you are offering", true)
                    .addStringParameter("offeredItem2", "The second item you are offering", false)
                    .addIntegerParameter("offeredQty2", "The quantity of the second item you are offering", false)
                    .addStringParameter("offeredItem3", "The third item you are offering", false)
                    .addIntegerParameter("offeredQty3", "The quantity of the third item you are offering", false)
                    .addStringParameter("offeredItem4", "The fourth item you are offering", false)
                    .addIntegerParameter("offeredQty4", "The quantity of the fourth item you are offering", false)
                    .addStringParameter("offeredItem5", "The fifth item you are offering", false)
                    .addIntegerParameter("offeredQty5", "The quantity of the fifth item you are offering", false)
                    .addStringParameter("offeredItem6", "The sixth item you are offering", false)
                    .addIntegerParameter("offeredQty6", "The quantity of the sixth item you are offering", false)
                    .addStringParameter("requestedItem1", "The first item you want to receive", true)
                    .addIntegerParameter("requestedQty1", "The quantity of the first item you want to receive", true)
                    .addStringParameter("requestedItem2", "The second item you want to receive", false)
                    .addIntegerParameter("requestedQty2", "The quantity of the second item you want to receive", false)
                    .addStringParameter("requestedItem3", "The third item you want to receive", false)
                    .addIntegerParameter("requestedQty3", "The quantity of the third item you want to receive", false)
                    .addStringParameter("requestedItem4", "The fourth item you want to receive", false)
                    .addIntegerParameter("requestedQty4", "The quantity of the fourth item you want to receive", false)
                    .addStringParameter("requestedItem5", "The fifth item you want to receive", false)
                    .addIntegerParameter("requestedQty5", "The quantity of the fifth item you want to receive", false)
                    .addStringParameter("requestedItem6", "The sixth item you want to receive", false)
                    .addIntegerParameter("requestedQty6", "The quantity of the sixth item you want to receive", false)
                    .build()
            ).addTool(Function.builder()
                    .name("accept_trade")
                    .description("Accept a trade request. This completes the trade and exchanges the items if successful.")
                    .addStringParameter("player", "The player whose request you wish to accept. Omit parameter to accept the most recent request.", true)
                    .build()
            ).addTool(Function.builder()
                    .name("private_message")
                    .description("Send a private message to a player. You can also reply to private messages with this")
                    .addStringParameter("player", "The player to send the message to.", true)
                    .addStringParameter("message", "The message to send.", true)
                    .build()
            ).addTool(Function.builder()
                    .name("decline_trade")
                    .description("Decline a trade request. This completes the trade request by rejecting it. Use this to reject an offer.")
                    .addStringParameter("player", "The player whose request you wish to decline. Omit parameter to decline the most recent request.", false)
                    .build()
            ).addTool(Function.builder()
                    .name("rescind_trade")
                    .description("Rescind a trade request. This completes the trade request by rescinding it. You can only rescind trades that you have sent/initiated. Use this to cancel an offer you have made.")
                    .addStringParameter("player", "The player to rescind your request from. Omit to rescind your most recent request", false)
                    .build()
            ).addTool(Function.builder()
                    .name("check_score")
                    .description("Checks the value of all items in your inventory and returns your current score. Items worth 0 pts are worth nothing to you. Check your desires to see what items are worth points.")
                    .noParameters()
                    .build()
            ).addTool(Function.builder()
                    .name("check_trades")
                    .description("List trades you were involved in.")
                    .addBooleanParameter("incoming", "True to show incoming trades, else shows outgoing trades, don't specify to show all", false)
                    .addBooleanParameter("completed", "Whether to list completed trades.", true)
                    .build()
            ).build();

        return request;
    }


    public List<ChatMessage> getGlobalMessages() {
        return globalMessages;
    }

    public void setGlobalMessages(List<ChatMessage> globalMessages) {
        this.globalMessages = globalMessages;
    }





}
