package crashcringle.malmoserverplugin.barterkings.players;

import com.cjcrafter.openai.OpenAI;
import com.cjcrafter.openai.chat.ChatMessage;
import com.cjcrafter.openai.chat.ChatRequest;
import com.cjcrafter.openai.chat.tool.Function;
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

    List<ChatMessage> globalMessages = new ArrayList<>();

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
    public  void setProfession(Profession profession) {
        super.setProfession(profession);
        String prompt = GPTService.generateSystemPrompt(this);
        globalMessages.add(ChatMessage.toSystemMessage(prompt));
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
        chunkedMessage += "||\n"+ message;
    }

    public void processMessage(String message) {
        chunkedMessage += message;
        NpcParticipant npcParticipant = this;
        new BukkitRunnable() {
            @Override
            public void run() {
                BarterKings.gptService.processChatGPTMessage(npcParticipant, ChatMessage.toUserMessage(chunkedMessage));
                chunkedMessage = "";
            }
        }.runTaskAsynchronously(MalmoServerPlugin.inst());
    }

    public void sendTradeRequest(TradeRequest tradeRequest) {
        if (isGenerating) {
            chunkMessage(tradeRequest.toPersonalString());
        } else {
            processMessage(tradeRequest.toPersonalString());
        }
    }

    public void denyTradeRequest(TradeRequest tradeRequest) {
        if (isGenerating) {
            chunkMessage("Your request to trade with "+tradeRequest.getRequested()+" has been denied.");
        } else {
            processMessage("Your request to trade with "+tradeRequest.getRequested()+" has been denied.");
        }
    }

    public void acceptTradeRequest(TradeRequest tradeRequest) {
        if (isGenerating) {
            chunkMessage("Your request to trade with "+tradeRequest.getRequested()+" has been accepted.");
        } else {
            processMessage("Your request to trade with "+tradeRequest.getRequested()+" has been accepted.");
        }
    }

    public void cancelTradeRequest(TradeRequest tradeRequest) {
        if (isGenerating) {
            chunkMessage(tradeRequest.getRequested()+" has cancelled the trade request.");
        } else {
            processMessage(tradeRequest.getRequested()+" has cancelled the trade request.");
        }
    }
    public ChatRequest getRequest() {
        String model = "gpt-3.5-turbo";
        ChatRequest request = ChatRequest.builder()
                .user(this.name)
                .model(model)
                .temperature(1f)
                .topP(1f)
                .maxTokens(100)
                .messages(getGlobalMessages())
                .addTool(Function.builder()
                        .name("do_nothing")
                        .description("Do not send a response as you deem appropriate.")
                        .noParameters()
                        .build()
                ).addTool(Function.builder()
                        .name("query_inventory")
                        .description("Returns a list of items in your inventory.")
                        .noParameters()
                        .build()
                ).addTool(Function.builder()
                        .name("trade")
                        .description("Propose a trade to the player.")
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
                        .description("Check your current score.")
                        .noParameters()
                        .build()
                ).addTool(Function.builder()
                        .name("list_trades")
                        .description("List trades you were involved in.")
                        .addBooleanParameter("incoming", "Use to show incoming or outcoming trades, don't specify to show all", false)
                        .addBooleanParameter("completed", "Whether to list completed trades.", true)
                        .build()
                ).addTool(Function.builder()
                        .name("private_message")
                        .description("Send a private message to a player.")
                        .addStringParameter("player", "The player to send the message to.", true)
                        .addStringParameter("message", "The message to send.", true)
                        .build()
                ).addTool(Function.builder()
                        .name("list_players")
                        .description("List all players in the game.")
                        .noParameters()
                        .build()
                ).addTool(Function.builder()
                        .name("check_desires")
                        .description("See what items give you points and how many")
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
