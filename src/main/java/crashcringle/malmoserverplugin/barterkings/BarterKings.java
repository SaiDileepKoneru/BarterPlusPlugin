package crashcringle.malmoserverplugin.barterkings;

import crashcringle.malmoserverplugin.MalmoServerPlugin;
import crashcringle.malmoserverplugin.barterkings.ai.GPTService;
import crashcringle.malmoserverplugin.barterkings.players.BarterGame;
import crashcringle.malmoserverplugin.barterkings.trades.TradeController;
import crashcringle.malmoserverplugin.barterkings.villagers.MalmoTrader;
import net.citizensnpcs.api.npc.NPC;

import java.util.HashMap;
import java.util.Map;

public class BarterKings {

    public static Map<Integer, MalmoTrader> traders = new HashMap<>();
    public static BarterGame barterGame;
    public static GPTService gptService;
    public static TradeController controller;
    public static NPC npc;

    public BarterKings(MalmoServerPlugin plugin) {
        controller = new TradeController();
        barterGame = new BarterGame();
    }
    public static BarterGame startNewGame() {
        barterGame = new BarterGame();
        controller = new TradeController();
        gptService = new GPTService(barterGame, controller);
        return barterGame;
    }

    public static Map<Integer, MalmoTrader> getTraders() {
        return traders;
    }

    public static void setTraders(Map<Integer, MalmoTrader> traders) {
        BarterKings.traders = traders;
    }



}
