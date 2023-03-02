package crashcringle.malmoserverplugin.barterkings.players;

import crashcringle.malmoserverplugin.MalmoServerPlugin;
//import crashcringle.malmoserverplugin.data.PlayerConverter;
//import crashcringle.malmoserverplugin.data.ProfessionConverter;
//import crashcringle.malmoserverplugin.data.SessionFactoryMaker;

import jakarta.persistence.*;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import lombok.Data;


import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.logging.Level;

import static crashcringle.malmoserverplugin.barterkings.players.BarterGame.fm;

//@Entity
//@NamedQueries(
//    @NamedQuery(name = "Participant.findByUUID", query = "select pd from PlayerData pd where pd.uuid=?1")
//)
@Data
public class Participant  {

   // @Id
    private String uuid;
   // @Column
    String name;

//    @Convert(converter = ProfessionConverter.class)
//    @Column
    Profession profession;

   // @Convert(converter = PlayerConverter.class)
   // @Column
    Player player;
    int score = 0;
    Player clickedPlayer;
    boolean ready;

    ChatColor color = ChatColor.WHITE;

    public Participant(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId().toString();
        this.name = player.getName();
        this.clickedPlayer = player;
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Participant created for " + player.getName());
    }

    public Participant(String uuid) {
        this.player = Bukkit.getPlayer(uuid);
        this.uuid = uuid;
        this.name = player.getName();
        this.clickedPlayer = player;
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Participant created for " + player.getName());
    }

//    public static Participant getParticipantData(String uuid) {
//        Participant pd;
//        SessionFactory sessionFactory = SessionFactoryMaker.getFactory();
//
//        try (Session session = sessionFactory.openSession()) {
//            pd = session.createNamedQuery("Participant.findByUUID", Participant.class)
//                    .setParameter(1, uuid).getSingleResultOrNull();
//
//            if (pd == null) {
//                Transaction tx = session.beginTransaction();
//                pd = new Participant(uuid);
//                session.merge(pd);
//                tx.commit();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            pd = null;
//        }
//
//        return pd;
//    }

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
     * @param
     * @return
     */
    public void calculateScore() {
        int score = 0;
        if (this.getProfession() == null) {
            MalmoServerPlugin.inst().getLogger().log(Level.WARNING, "Profession is null for " + getPlayer().getName());
            return;
        }
        getPlayer().sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Your Score Breakdown:");
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Score Breakdown " + getPlayer().getName());
        for (ItemStack item : getPlayer().getInventory().getContents()) {
            if (item != null) {
                int addedScore = 0;
                ItemStack item2 = new ItemStack(item.getType());
                item2.setAmount(1);
                MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Item: " + item.getType() + " x" + item.getAmount());
                if (this.getProfession().getTier1Items().contains(item2)) {
                    addedScore += item.getAmount();
                } else if (this.getProfession().getTier2Items().contains(item2)) {
                    addedScore += 3 * item.getAmount();
                } else if (this.getProfession().getTier3Items().contains(item2)) {
                    addedScore += 10 * item.getAmount();
                }
                 if (addedScore > 0) {

                     String message = String.format("%s%-25s x%-4d = %4d", ChatColor.GOLD, fm(item.getType()), item.getAmount(), addedScore);
                     getPlayer().sendMessage(message);
                     MalmoServerPlugin.inst().getLogger().log(Level.INFO, fm(item.getType()) + " x" + item.getAmount() + " = " + addedScore);
                     score += addedScore;
                 }
            }
        }
        setScore(score);
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "Score for " + getPlayer().getName() + " is " + score);
        MalmoServerPlugin.inst().getLogger().log(Level.INFO, "*********************************************");

    }

    public int getCalculatedScore() {
        calculateScore();
        return score;
    }


}
