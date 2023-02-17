package crashcringle.malmoserverplugin.data;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import crashcringle.malmoserverplugin.barterkings.players.Participant;
import crashcringle.malmoserverplugin.barterkings.players.Profession;

public class SessionFactoryMaker {
    private static SessionFactory factory;

    private static void configureFactory()
    {
        try {
            factory = new Configuration()
                    .addAnnotatedClass(Participant.class)
                    .addAnnotatedClass(Profession.class)
                    .configure().buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Failed to create sessionFactory object." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static org.hibernate.SessionFactory getFactory() {
        if (factory == null) {
            configureFactory();
        }

        return factory;
    }

}