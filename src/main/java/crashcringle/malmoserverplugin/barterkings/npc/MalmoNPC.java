package crashcringle.malmoserverplugin.barterkings.npc;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.EntityType;

public class MalmoNPC {

    public MalmoNPC() {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "TraderPrototype1");
        //npc.spawn(location);
    }
}
