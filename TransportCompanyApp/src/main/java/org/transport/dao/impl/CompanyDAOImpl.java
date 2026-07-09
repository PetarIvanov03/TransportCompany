package org.transport.dao.impl;

import org.hibernate.Session;
import org.transport.dao.CompanyDAO;
import org.transport.entity.TransportCompany;

import java.util.List;

// Hibernate implementation of CompanyDAO
public class CompanyDAOImpl extends AbstractGenericDAO<TransportCompany, Long> implements CompanyDAO {

    public CompanyDAOImpl() {
        super(TransportCompany.class);
    }

    @Override
    public List<TransportCompany> findAllSortedByName(Session session) {
        return session.createQuery(
                        "FROM TransportCompany c ORDER BY c.name ASC", TransportCompany.class)
                .list();
    }

    @Override
    public List<TransportCompany> findAllSortedByRevenue(Session session) {
        return session.createQuery(
                        "SELECT c FROM TransportCompany c " +
                                "LEFT JOIN c.vehicles v " +
                                "LEFT JOIN v.transports t " +
                                "GROUP BY c " +
                                "ORDER BY SUM(COALESCE(t.price, 0)) DESC", TransportCompany.class)
                .list();
    }
}