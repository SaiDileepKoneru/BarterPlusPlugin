package crashcringle.malmoserverplugin.barterkings;

import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.UUID;


public class PersistentTagger {
    @Getter
    private static final String MALMO_ENTITY = "MalmoEntity";
    @Getter
    private static final String TRADER_VILLAGER_ENTITY = "MalmoTrader";
    @Getter
    private static final String TRADER_NPC_ENTITY = "MalmoTraderNPC";
    private PersistentTagger() {
    }

    public static void tag(@NotNull Entity entity, String key, String value) {
        entity.getPersistentDataContainer().set(new NamespacedKey(MetadataHandler.PLUGIN, key), PersistentDataType.STRING, value);
    }

    public static MalmoTrader getTrader(Entity entity) {
        UUID uuid = getUUID(entity, TRADER_VILLAGER_ENTITY);
        if (uuid == null) return null;
        return BarterKings.getTraders().get(uuid);
    }

    @Nullable
    public static UUID getUUID(Entity entity, String key) {
        if (entity == null) return null;
        String uuidString = entity.getPersistentDataContainer().get(new NamespacedKey(MetadataHandler.PLUGIN, key), PersistentDataType.STRING);
        if (uuidString == null) return null;
        return UUID.fromString(uuidString);
    }

    public static void tagMalmoTrader(Entity entity) {
        tag(entity, TRADER_VILLAGER_ENTITY, entity.getType().toString());
    }
    public static void tagMalmoTraderNPC(Entity entity) {
        tag(entity, TRADER_NPC_ENTITY, entity.getType().toString());
    }

}
