package org.transport.service;

import org.hibernate.Session;
import org.transport.dao.ClientDAO;
import org.transport.dao.impl.ClientDAOImpl;
import org.transport.entity.Client;
import org.transport.util.HibernateUtil;
import org.transport.util.TransactionUtil;
import org.transport.util.ValidationUtil;

import java.util.List;

// Business logic for managing clients
public class ClientService {

    private final ClientDAO clientDAO = new ClientDAOImpl();

    public void createClient(Client client) {
        ValidationUtil.validate(client);
        TransactionUtil.execute(HibernateUtil.getSessionFactory(), session -> clientDAO.save(session, client));
    }

    public void updateClient(Client client) {
        ValidationUtil.validate(client);
        TransactionUtil.execute(HibernateUtil.getSessionFactory(), session -> clientDAO.update(session, client));
    }

    public void deleteClient(Long id) {
        TransactionUtil.execute(HibernateUtil.getSessionFactory(), session -> clientDAO.deleteById(session, id));
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

}