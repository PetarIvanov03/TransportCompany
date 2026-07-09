package org.transport.dao;

import org.hibernate.Session;
import org.transport.entity.Client;

public interface ClientDAO extends GenericDAO<Client, Long> {
    Client findByName(Session session, String name);
}