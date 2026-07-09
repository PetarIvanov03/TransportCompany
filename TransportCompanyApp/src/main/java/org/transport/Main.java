package org.transport;

import org.transport.util.HibernateUtil;

// Entry point of the Transport Company application
public class Main {
    public static void main(String[] args) {
        System.out.println("Опит за връзка с базата...");
        HibernateUtil.getSessionFactory();
        System.out.println("Успешно свързан и схемата е синхронизирана!");
        HibernateUtil.shutdown();
    }
}
