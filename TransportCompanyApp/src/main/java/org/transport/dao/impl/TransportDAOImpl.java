package org.transport.dao.impl;

import org.hibernate.Session;
import org.transport.dao.TransportDAO;
import org.transport.entity.Transport;

import java.time.LocalDateTime;
import java.util.List;

// Hibernate implementation of TransportDAO
public class TransportDAOImpl extends AbstractGenericDAO<Transport, Long> implements TransportDAO {

    public TransportDAOImpl() {
        super(Transport.class);
    }

    @Override
    public List<Transport> findByDestination(Session session, String destination) {
        return session.createQuery(
                        "FROM Transport t WHERE t.destinationPoint = :destination", Transport.class)
                .setParameter("destination", destination)
                .list();
    }

    @Override
    public List<Transport> findByDateRange(Session session, LocalDateTime from, LocalDateTime to) {
        return session.createQuery(
                        "FROM Transport t WHERE t.departureDate BETWEEN :from AND :to", Transport.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .list();
    }

    @Override
    public Transport findByIdWithDetails(Session session, Long id) {
        return session.createQuery(
                        "SELECT t FROM Transport t " +
                                "LEFT JOIN FETCH t.client " +
                                "LEFT JOIN FETCH t.vehicle " +
                                "LEFT JOIN FETCH t.driver " +
                                "WHERE t.id = :id", Transport.class)
                .setParameter("id", id)
                .uniqueResult();
    }
}