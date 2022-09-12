package crashcringle.malmoserverplugin.barterkings;

import crashcringle.malmoserverplugin.MalmoServerPlugin;

import java.util.HashMap;
import java.util.Map;

public class BarterKings {

    public static Map<Integer, MalmoTrader> traders = new HashMap<>();
    public BarterKings(MalmoServerPlugin plugin) {

    }

    public static Map<Integer, MalmoTrader> getTraders() {
        return traders;
    }

    public static void setTraders(Map<Integer, MalmoTrader> traders) {
        BarterKings.traders = traders;
    }

}
