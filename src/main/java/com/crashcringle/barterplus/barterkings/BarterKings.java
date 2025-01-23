package com.crashcringle.barterplus.barterkings;

import com.crashcringle.barterplus.BarterPlus;
import com.crashcringle.barterplus.barterkings.ai.GeminiService;
import com.crashcringle.barterplus.barterkings.players.BarterGame;
import com.crashcringle.barterplus.barterkings.villagers.MalmoTrader;
import com.crashcringle.barterplus.barterkings.ai.GPTService;
import com.crashcringle.barterplus.barterkings.trades.TradeController;
import net.citizensnpcs.api.npc.NPC;

import java.util.HashMap;
import java.util.Map;

public class BarterKings {

    public static Map<Integer, MalmoTrader> traders = new HashMap<>();
    public static BarterGame barterGame;
    public static GPTService gptService;
    public static GeminiService geminiService;
    public static TradeController controller;
    public static NPC npc;

    public BarterKings(BarterPlus plugin) {
        controller = new TradeController();
        barterGame = new BarterGame();
    }
    public static BarterGame startNewGame() {
        barterGame = new BarterGame();
        controller = new TradeController();
        gptService = new GPTService(barterGame, controller);
        geminiService = new GeminiService(barterGame, controller);
        return barterGame;
    }

    public static Map<Integer, MalmoTrader> getTraders() {
        return traders;
    }

    public static void setTraders(Map<Integer, MalmoTrader> traders) {
        BarterKings.traders = traders;
    }



}
