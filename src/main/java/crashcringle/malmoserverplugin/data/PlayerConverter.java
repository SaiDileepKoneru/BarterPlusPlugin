package crashcringle.malmoserverplugin.data;

import crashcringle.malmoserverplugin.barterkings.BarterKings;
import crashcringle.malmoserverplugin.barterkings.players.Profession;
import jakarta.persistence.AttributeConverter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerConverter implements AttributeConverter<Player, String> {

    @Override
    public String convertToDatabaseColumn(final Player player) {
        return player.getName();
    }

    @Override
    public Player convertToEntityAttribute(final String name) {
        return Bukkit.getPlayer(name);
    }
}