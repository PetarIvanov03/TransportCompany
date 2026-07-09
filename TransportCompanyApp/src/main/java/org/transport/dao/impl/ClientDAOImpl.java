package org.transport.dao.impl;

import org.hibernate.Session;
import org.transport.dao.ClientDAO;
import org.transport.entity.Client;

import java.util.List;

// Hibernate implementation of ClientDAO
public class ClientDAOImpl extends AbstractGenericDAO<Client, Long> implements ClientDAO {

    public ClientDAOImpl() {
        super(Client.class);
    }

    @Override
    public Client findByName(Session session, String name) {
        return session.createQuery(
                        "FROM Client c WHERE c.name = :name", Client.class)
                .setParameter("name", name)
                .uniqueResult();
    }
}