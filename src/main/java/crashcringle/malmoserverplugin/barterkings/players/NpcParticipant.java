package crashcringle.malmoserverplugin.barterkings.players;

import com.cjcrafter.openai.OpenAI;
import com.cjcrafter.openai.chat.ChatMessage;
import com.cjcrafter.openai.chat.ChatRequest;
import com.cjcrafter.openai.chat.ChatUser;
import com.cjcrafter.openai.chat.tool.Function;
import com.cjcrafter.openai.threads.Thread;
import com.cjcrafter.openai.threads.message.CreateThreadMessageRequest;
import com.cjcrafter.openai.threads.message.ThreadUser;
import com.cjcrafter.openai.threads.runs.CreateRunRequest;
import com.cjcrafter.openai.threads.runs.Run;
import crashcringle.malmoserverplugin.MalmoServerPlugin;
import crashcringle.malmoserverplugin.barterkings.BarterKings;
import crashcringle.malmoserverplugin.barterkings.ai.GPTService;
import crashcringle.malmoserverplugin.barterkings.trades.TradeRequest;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.event.player.AsyncPlayerChatEvent;
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
        chunkedMessage += " || "+ message;
    }

    public void processMessage(String message) {
        chunkedMessage += message;
        NpcParticipant npcParticipant = this;
        try {
            new BukkitRunnable() {
                @Override
                public void run() {
                        BarterKings.gptService.processChatGPTMessage(npcParticipant, ChatMessage.toUserMessage(chunkedMessage));
                        chunkedMessage = "";
                }
            // Random between 1 and 6 seconds
            }.runTaskLaterAsynchronously(MalmoServerPlugin.inst(), 20L * (8 + (long) (Math.random() * 10)));
        } catch (Exception e) {
            // Lets let ChatGPT know it made a mistake so it can correct itself
            MalmoServerPlugin.inst().getLogger().warning("ChatMessage: "+chunkedMessage);
            String json = "{\"error\": \"" + e.getMessage() + "\"}";
            MalmoServerPlugin.inst().getLogger().warning("Hallucination Error: " + e.getMessage());

            MalmoServerPlugin.inst().getLogger().warning("\n\n**Last Request: " + this.getRequest().getMessages().get(this.getRequest().getMessages().size()-1).getContent());
            BarterKings.gptService.processChatGPTMessage(npcParticipant, ChatMessage.toSystemMessage(json));

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
                MalmoServerPlugin.inst().getLogger().info("Still generating");
                run =  BarterKings.gptService.getOpenai().threads().runs(thread).retrieve(run);
                isGenerating = true;
            } else {
                MalmoServerPlugin.inst().getLogger().info("Generating complete");
                processMessage2(message);
            }
        } else {
            MalmoServerPlugin.inst().getLogger().info("Generating");
            processMessage2(message);
        }
    }

    public void processMessage2(String message) {
        BarterKings.gptService.getOpenai().threads().messages(thread).create(CreateThreadMessageRequest.builder()
                .role(ThreadUser.USER)
                .content(message)
                .build());
        MalmoServerPlugin.inst().getLogger().info("Processing message1: "+message);
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
        }.runTaskLaterAsynchronously(MalmoServerPlugin.inst(), 20L * (5 + (long) (Math.random() * 30)));
    }

    public void sendTradeRequest(TradeRequest tradeRequest) {
        queueMessage(tradeRequest.toPersonalString());
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
        thread = BarterKings.gptService.getOpenai().threads().create();
        String prompt = GPTService.generateSystemPrompt(this);
        globalMessages.add(ChatMessage.toSystemMessage(prompt));
    }
    public ChatRequest getRequest() {
    String model = "gpt-4o";
    ChatRequest request = ChatRequest.builder()
            .user(this.name + " (NPC)")
            .model(model)
            .temperature(0.7f)
            .topP(0.8f)
            .messages(getGlobalMessages())
            .addTool(Function.builder()
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
                    .description("Propose a trade to the player (You can only have a single open trade with a given player).")
                    .addStringParameter("player", "The player you are trading with.", true)
                    .addStringParameter("offeredItem", "The item you are offering.", true)
                    .addIntegerParameter("offeredQty", "The quantity of the item you are offering.", true)
                    .addStringParameter("requestedItem", "The item you want to receive.", true)
                    .addIntegerParameter("requestedQty", "The quantity of the item you want to receive.", true)
                    .build()
            ).addTool(Function.builder()
                    .name("accept_trade")
                    .description("Accept a trade proposal.")
                    .addStringParameter("player", "The player whose request you wish to accept. Omit parameter to accept the most recent request.", true)
                    .build()
            ).addTool(Function.builder()
                    .name("decline_trade")
                    .description("Decline a trade proposal.")
                    .addStringParameter("player", "The player whose request you wish to decline. Omit parameter to decline the most recent request.", false)
                    .build()
            ).addTool(Function.builder()
                    .name("cancel_trade")
                    .description("Rescind a trade proposal.")
                    .addStringParameter("player", "The player to rescind your request from. Omit to cancel your most recent request", false)
                    .build()
            ).addTool(Function.builder()
                    .name("check_score")
                    .description("Check your current score based on items in your inventory.")
                    .noParameters()
                    .build()
            ).addTool(Function.builder()
                    .name("check_trades")
                    .description("List trades you were involved in.")
                    .addBooleanParameter("incoming", "Use to show incoming or outcoming trades, don't specify to show all", false)
                    .addBooleanParameter("completed", "Whether to list completed trades.", true)
                    .build()
            ).addTool(Function.builder()
                    .name("check_desires")
                    .description("See the only items that you want in your inventory to gain points")
                    .noParameters()
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
