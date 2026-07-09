package org.transport.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class HibernateUtil {

    private static SessionFactory sessionFactory;

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");

            Properties dbProps = loadDbProperties();
            configuration.setProperty("hibernate.connection.url", dbProps.getProperty("db.url"));
            configuration.setProperty("hibernate.connection.username", dbProps.getProperty("db.username"));
            configuration.setProperty("hibernate.connection.password", dbProps.getProperty("db.password"));

            return configuration.buildSessionFactory();
        } catch (Exception e) {
            System.err.println("SessionFactory creation failed: " + e);
            throw new ExceptionInInitializerError(e);
        }
    }

    private static Properties loadDbProperties() {
        Properties props = new Properties();
        try (InputStream input = HibernateUtil.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new RuntimeException("db.properties not found in classpath. " +
                        "Copy db.properties.example to db.properties and fill in your credentials.");
            }
            props.load(input);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load db.properties", e);
        }
        return props;
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = buildSessionFactory();
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}