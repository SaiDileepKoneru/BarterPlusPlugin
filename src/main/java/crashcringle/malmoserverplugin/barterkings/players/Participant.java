package crashcringle.malmoserverplugin.barterkings.players;

import crashcringle.malmoserverplugin.MalmoServerPlugin;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class Participant  {
    Profession profession;
    Player player;

    boolean ready;
    public Participant(Player player) {
        this.player = player;
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Participant created for " + player.getName());
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


}
