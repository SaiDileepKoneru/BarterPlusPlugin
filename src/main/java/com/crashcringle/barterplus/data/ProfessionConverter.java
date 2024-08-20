//package crashcringle.barterplus.data;
//
//import barterkings.com.crashcringle.barterplus.BarterKings;
//import players.barterkings.com.crashcringle.barterplus.Profession;
//import jakarta.persistence.AttributeConverter;
//import org.bukkit.Bukkit;
//import org.bukkit.entity.Player;
//
//public class ProfessionConverter implements AttributeConverter<Profession, String> {
//
//    @Override
//    public String convertToDatabaseColumn(final Profession profession) {
//        return profession.getName();
//    }
//
//    @Override
//    public Profession convertToEntityAttribute(final String name) {
//        return BarterKings.getProfession(name);
//    }
//}