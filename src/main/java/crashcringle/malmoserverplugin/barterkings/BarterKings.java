package crashcringle.malmoserverplugin.barterkings;

import crashcringle.malmoserverplugin.MalmoServerPlugin;
import crashcringle.malmoserverplugin.barterkings.players.PlayerHandler;
import crashcringle.malmoserverplugin.barterkings.trades.TradeController;
import crashcringle.malmoserverplugin.barterkings.villagers.MalmoTrader;

import java.util.HashMap;
import java.util.Map;

public class BarterKings {

    public static Map<Integer, MalmoTrader> traders = new HashMap<>();
    public static PlayerHandler barterGame;
    public static TradeController controller;

    public BarterKings(MalmoServerPlugin plugin) {
        controller = new TradeController();
        barterGame = new PlayerHandler();
    }
    public static void startNewGame() {
        barterGame = new PlayerHandler();
        controller = new TradeController();
    }

    public static Map<Integer, MalmoTrader> getTraders() {
        return traders;
    }

    public static void setTraders(Map<Integer, MalmoTrader> traders) {
        BarterKings.traders = traders;
    }

}
