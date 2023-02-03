package crashcringle.malmoserverplugin.barterkings.players;

import crashcringle.malmoserverplugin.MalmoServerPlugin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

public class Participant  {
    Profession profession;
    Player player;
    int score = 0;
    Player clickedPlayer;
    boolean ready;
    public Participant(Player player) {
        this.player = player;
        this.clickedPlayer = player;
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Participant created for " + player.getName());
    }

    public int getScore() {
        return this.score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setProfession(Profession profession) {
        this.profession = profession;
    }

    public Profession getProfession() {
        return profession;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getClickedPlayer() {
        return clickedPlayer;
    }

    public void setClickedPlayer(Player player) {
        this.clickedPlayer = player;
    }

    public boolean isReady() {
        return ready;
    }

    public void readyUp() {
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Player " + player.getName() + " is ready!");
        ready = true;
    }

    public void unready() {
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Player " + player.getName() + " is not ready!");
        ready = false;
    }

    /**
     * This method takes a parameter of participant and returns the score 
     * of the participant based on the amount of tier 1, 2, and 3 items they have in their inventory.
     * @param participant
     * @return
     */
    public void calculateScore() {
        int score = 0;
        if (this.getProfession() == null) {
            MalmoServerPlugin.inst().getLogger().log(Level.WARNING, "Profession is null for " + getPlayer().getName());
            return;
        }
        for (ItemStack item : getPlayer().getInventory().getContents()) {
            if (item != null) {
                if (this.getProfession().getTier1Items().contains(item)) {
                    score += 1;
                } else if (this.getProfession().getTier2Items().contains(item)) {
                    score += 2;
                } else if (this.getProfession().getTier3Items().contains(item)) {
                    score += 3;
                }
            }
        }
        setScore(score);
    }


}
