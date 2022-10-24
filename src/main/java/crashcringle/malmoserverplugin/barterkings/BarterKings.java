package crashcringle.malmoserverplugin.barterkings;

import crashcringle.malmoserverplugin.MalmoServerPlugin;
import crashcringle.malmoserverplugin.barterkings.trades.TradeController;
import crashcringle.malmoserverplugin.barterkings.villagers.MalmoTrader;

import java.util.HashMap;
import java.util.Map;

public class BarterKings {

    public static Map<Integer, MalmoTrader> traders = new HashMap<>();

    public BarterKings(MalmoServerPlugin plugin) {
        new TradeController();
    }

    public static Map<Integer, MalmoTrader> getTraders() {
        return traders;
    }

    public static void setTraders(Map<Integer, MalmoTrader> traders) {
        BarterKings.traders = traders;
    }

}
