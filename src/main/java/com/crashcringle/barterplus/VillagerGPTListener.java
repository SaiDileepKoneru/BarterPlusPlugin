package com.crashcringle.barterplus;

import com.github.javafaker.Faker;
import org.bukkit.Bukkit;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.ParametersAreNonnullByDefault;

public class VillagerGPTListener implements Listener {
    @ParametersAreNonnullByDefault
    public Listeners(VillagerGPTAssign plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    @EventHandler
    public void onVillagerSpawn(EntitySpawnEvent event) {
        if (event.getEntity().getType() != EntityType.VILLAGER) {
            return;
        }
        try {
            if (event.getLocation().getWorld().getName().contains("em_")) {
                return;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        // There's a 5% chance of a villager spawning as a GPT Villager
        if (Math.random() > 0.03)
            return;
        Faker faker = new Faker();
        // Set the villager's name
        Villager villager = (Villager) event.getEntity();
        villager.setCustomName(faker.name().firstName() + " " + faker.name().lastName());
        villager.setCustomNameVisible(true);
        villager.setVillagerType(Villager.Type.SWAMP);
        villager.setProfession(Villager.Profession.CARTOGRAPHER);
        // TAG
        CustomPersistentTagger.tagGPTVillager(villager);
        // Chance to convert nearby villagers
        for (int i = 0; i < 5; i++) {
            Villager nearbyVillager = (Villager) villager.getWorld().getNearbyEntities(villager.getLocation(), 25, 25, 25).stream().filter(entity -> entity instanceof Villager).findFirst().orElse(null);
            if (nearbyVillager != null) {
                if (Math.random() > 0.02) {
                    faker = new Faker();
                    nearbyVillager.setCustomName(faker.name().firstName() + " " + faker.name().lastName());
                    nearbyVillager.setCustomNameVisible(true);
                    nearbyVillager.setVillagerType(Villager.Type.SWAMP);
                    CustomPersistentTagger.tagGPTVillager(nearbyVillager);
                }
            }
        }


    }
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() == EntityType.WARDEN) {
            if (event.getCause() == EntityDamageEvent.DamageCause.THORNS) {
                if (event.getEntity().getCustomName().contains("Shrek"))
                    event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onVillagerProfessionChange(VillagerCareerChangeEvent event) {
        // There's a 2% chance of a villager spawning as a GPT Villager
        if (Math.random() > 0.01)
            return;
        Faker faker = new Faker();
        // Set the villager's name
        Villager villager = event.getEntity();
        villager.setCustomName(faker.name().firstName() + " " + faker.name().lastName());
        villager.setCustomNameVisible(true);
        villager.setVillagerType(Villager.Type.SWAMP);
        villager.setProfession(Villager.Profession.CARTOGRAPHER);
        // TAG
        CustomPersistentTagger.tagGPTVillager(villager);
    }

    @EventHandler
    public void onPlayerHitVillager(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player player = (Player) event.getDamager();
            Player target = (Player) event.getEntity();
            // Check if the player has the target's soul

        }
    }


}