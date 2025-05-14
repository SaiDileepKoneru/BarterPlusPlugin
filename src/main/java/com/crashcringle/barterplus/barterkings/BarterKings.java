package com.crashcringle.barterplus.barterkings;

import com.crashcringle.barterplus.BarterPlus;
import com.crashcringle.barterplus.barterkings.ai.GeminiService;
import com.crashcringle.barterplus.barterkings.players.BarterGame;
import com.crashcringle.barterplus.barterkings.villagers.MalmoTrader;
import com.crashcringle.barterplus.barterkings.ai.GPTService;
import com.crashcringle.barterplus.barterkings.trades.TradeController;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.scheduler.BukkitRunnable;

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
        initiateGame();
        gptService = new GPTService(controller);
        geminiService = new GeminiService(controller);
        return barterGame;
    }

    public static BarterGame initiateGame() {
        barterGame = new BarterGame();
        controller = new TradeController();
        return barterGame;
    }

    public static void startGamesService() {
        gptService = new GPTService(controller);
        geminiService = new GeminiService(controller);

            new BukkitRunnable() {
                int[] completedRuns = new int[BarterPlus.inst().maxRuns.length]; // Array to track completed runs for each scenario
                int runIndex = 0; // Index to track which scenario we're on
                @Override
                public void run() {
                    if ((!barterGame.inProgress() && !barterGame.isAskingQuestions()) ) {
                        if (runIndex >= BarterPlus.Scenario.values().length) {
                            BarterPlus.inst().getLogger().info("All scenarios have been completed. Stopping the game service.");
                            BarterPlus.inst().getLogger().info("************************************");
                            // Print completed runs for each scenario
                            for (int i = 0; i < completedRuns.length; i++) {
                                BarterPlus.inst().getLogger().info("Completed runs for scenario " + BarterPlus.Scenario.values()[i] + ": " + completedRuns[i]);
                            }
                            cancel(); // Stop the runnable if all scenarios have been processed
                            return;
                        } else {
                            int maxRuns = BarterPlus.inst().maxRuns[runIndex]; // Get the max runs for the current scenario
                            int runs = completedRuns[runIndex]; // Get the current number of completed runs for this scenario
                            if (runs >= maxRuns) {
                                BarterPlus.inst().getLogger().info("Reached max runs, concluding " + BarterPlus.inst().currentScenario + " games after " + maxRuns + " runs.");
                                runIndex++; // Move to the next scenario
                                if (runIndex >= BarterPlus.Scenario.values().length) {
                                    BarterPlus.inst().getLogger().info("All scenarios have been completed. Stopping the game service.");
                                    BarterPlus.inst().getLogger().info("************************************");
                                    // Print completed runs for each scenario
                                    for (int i = 0; i < completedRuns.length; i++) {
                                        BarterPlus.inst().getLogger().info("Completed runs for scenario " + BarterPlus.Scenario.values()[i] + ": " + completedRuns[i]);
                                    }
                                    cancel(); // Stop the runnable if all scenarios have been processed
                                    return;
                                } else {
                                    BarterPlus.inst().getLogger().info("************************************");
                                    BarterPlus.inst().getLogger().info("Starting new runs with the Scenario: " + BarterPlus.inst().currentScenario);
                                }
                            }
                            BarterPlus.inst().currentScenario = BarterPlus.Scenario.values()[runIndex]; // Set the scenario based on the index
                            BarterPlus.inst().getLogger().info("No game in progress, starting a new " + BarterPlus.inst().currentScenario + " game.");
                            BarterKings.barterGame.clearParticipants(); // Clear any existing participants before starting a new game
                            initiateGame(); // Start a new game
                            barterGame.attemptStart();
                            completedRuns[runIndex]++; // Increment the completed runs for the current scenario
                        }
                    } else {
                        // Game is still in progress or asking questions
                        BarterPlus.inst().getLogger().info("Game is still in progress or asking questions, not starting a new game.");
                    }
                }
            }.runTaskTimer(BarterPlus.inst(), 0, 6000L);
    }

    public static Map<Integer, MalmoTrader> getTraders() {
        return traders;
    }

    public static void setTraders(Map<Integer, MalmoTrader> traders) {
        BarterKings.traders = traders;
    }



}
