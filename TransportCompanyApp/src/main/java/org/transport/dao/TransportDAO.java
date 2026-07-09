package org.transport.dao;

import org.hibernate.Session;
import org.transport.entity.Transport;

import java.time.LocalDateTime;
import java.util.List;

// Data-access contract for Transport (order) entities
public interface TransportDAO extends GenericDAO<org.transport.entity.Transport, Long> {
    List<Transport> findByDestination(Session session, String destination);
    List<Transport> findByDateRange(Session session, LocalDateTime from, LocalDateTime to);
    Transport findByIdWithDetails(Session session, Long id);
}