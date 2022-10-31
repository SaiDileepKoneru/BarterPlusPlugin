package crashcringle.malmoserverplugin.barterkings.players;

import org.bukkit.entity.Player;

public class Participant  {
    Profession profession;
    Player player;

    boolean ready;
    public Participant(Player player) {
        this.player = player;
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
        ready = true;
    }

    public void unready() {
        ready = false;
    }


}
