package org.transport.dao;

import org.hibernate.Session;
import org.transport.entity.TransportCompany;

import java.util.List;

public interface CompanyDAO extends GenericDAO<TransportCompany, Long> {
    List<TransportCompany> findAllSortedByName(Session session);
    List<TransportCompany> findAllSortedByRevenue(Session session);
    TransportCompany findByIdWithVehicles(Session session, Long id);
    TransportCompany findByIdWithEmployees(Session session, Long id);
}