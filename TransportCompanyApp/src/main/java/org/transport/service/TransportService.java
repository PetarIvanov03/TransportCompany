package org.transport.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.transport.dao.TransportDAO;
import org.transport.dao.impl.TransportDAOImpl;
import org.transport.entity.Transport;
import org.transport.entity.enums.CargoType;
import org.transport.util.HibernateUtil;
import org.transport.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

// Business logic for creating and managing transport orders
public class TransportService {

    private final TransportDAO transportDAO;
    private final SessionFactory sessionFactory;

    public TransportService() {
        this(new TransportDAOImpl(), HibernateUtil.getSessionFactory());
    }

    public TransportService(TransportDAO transportDAO, SessionFactory sessionFactory) {
        this.transportDAO = transportDAO;
        this.sessionFactory = sessionFactory;
    }

    public void createTransport(Transport transport) {
        validateTransport(transport);
        executeInTransaction(session -> transportDAO.save(session, transport));
    }

    public void updateTransport(Transport transport) {
        validateTransport(transport);
        executeInTransaction(session -> transportDAO.update(session, transport));
    }

    public void deleteTransport(Long id) {
        executeInTransaction(session -> transportDAO.deleteById(session, id));
    }

    public Transport getById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return transportDAO.findById(session, id);
        }
    }

    public List<Transport> getByDestination(String destination) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return transportDAO.findByDestination(session, destination);
        }
    }

    public List<Transport> getByDateRange(LocalDateTime from, LocalDateTime to) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return transportDAO.findByDateRange(session, from, to);
        }
    }

    private void validateTransport(Transport transport) {
        ValidationUtil.validate(transport);

        if (transport.getCargoType() == CargoType.GOODS && transport.getCargoWeight() == null) {
            throw new IllegalArgumentException(
                    "Теглото на товара е задължително при превоз на стока (cargoType = GOODS).");
        }
        if (transport.getCargoType() == CargoType.PEOPLE && transport.getCargoWeight() != null) {
            throw new IllegalArgumentException(
                    "Теглото на товара трябва да е празно при превоз на хора (cargoType = PEOPLE).");
        }
        if (!transport.getArrivalDate().isAfter(transport.getDepartureDate())) {
            throw new IllegalArgumentException(
                    "Датата на пристигане трябва да е след датата на тръгване.");
        }
    }

    private void executeInTransaction(Consumer<Session> action) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                action.accept(session);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
        }
    }
}