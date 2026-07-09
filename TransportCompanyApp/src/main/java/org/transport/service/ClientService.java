package org.transport.service;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.transport.dao.ClientDAO;
import org.transport.dao.impl.ClientDAOImpl;
import org.transport.entity.Client;
import org.transport.util.HibernateUtil;
import org.transport.util.ValidationUtil;

import java.util.List;
import java.util.function.Consumer;

// Business logic for managing clients
public class ClientService {

    private final ClientDAO clientDAO = new ClientDAOImpl();

    public void createClient(Client client) {
        ValidationUtil.validate(client);
        executeInTransaction(session -> clientDAO.save(session, client));
    }

    public void updateClient(Client client) {
        ValidationUtil.validate(client);
        executeInTransaction(session -> clientDAO.update(session, client));
    }

    public void deleteClient(Long id) {
        executeInTransaction(session -> clientDAO.deleteById(session, id));
    }

    public Client getById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return clientDAO.findById(session, id);
        }
    }

    public List<Client> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return clientDAO.findAll(session);
        }
    }

    private void executeInTransaction(Consumer<Session> action) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
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