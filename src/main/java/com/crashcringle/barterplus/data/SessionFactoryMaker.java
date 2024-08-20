package com.crashcringle.barterplus.data;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.crashcringle.barterplus.barterkings.players.Participant;

public class SessionFactoryMaker {
    private static SessionFactory factory;

    private static void configureFactory()
    {
        try {
            factory = new Configuration()
                    .addAnnotatedClass(Participant.class)
//                    .addAnnotatedClass(Profession.class)
//                    .addAnnotatedClass(TradeRequest.class)
//                    .addAnnotatedClass(Trade.class)
//                    .addAnnotatedClass()
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
    public Session getSession() {
        return factory.openSession();
    }

}