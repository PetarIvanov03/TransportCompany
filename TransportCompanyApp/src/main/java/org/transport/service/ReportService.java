package org.transport.service;

import org.hibernate.Session;
import org.transport.entity.Driver;
import org.transport.util.HibernateUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Generates reports and справки (e.g. revenue by company, trips by driver)
public class ReportService {

    public long getTotalTransportCount() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT COUNT(t) FROM Transport t", Long.class)
                    .uniqueResult();
        }
    }

    public BigDecimal getTotalRevenue() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            BigDecimal result = session.createQuery(
                            "SELECT SUM(t.price) FROM Transport t", BigDecimal.class)
                    .uniqueResult();
            return result != null ? result : BigDecimal.ZERO;
        }
    }

    public Map<Driver, Long> getTransportCountPerDriver() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createQuery(
                            "SELECT t.driver, COUNT(t) FROM Transport t GROUP BY t.driver", Object[].class)
                    .list();

            Map<Driver, Long> result = new LinkedHashMap<>();
            for (Object[] row : rows) {
                result.put((Driver) row[0], (Long) row[1]);
            }
            return result;
        }
    }

    public Map<Driver, BigDecimal> getRevenuePerDriver() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createQuery(
                            "SELECT t.driver, SUM(t.price) FROM Transport t GROUP BY t.driver", Object[].class)
                    .list();

            Map<Driver, BigDecimal> result = new LinkedHashMap<>();
            for (Object[] row : rows) {
                result.put((Driver) row[0], (BigDecimal) row[1]);
            }
            return result;
        }
    }

    public BigDecimal getCompanyRevenueForPeriod(Long companyId, LocalDateTime from, LocalDateTime to) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            BigDecimal result = session.createQuery(
                            "SELECT SUM(t.price) FROM Transport t " +
                                    "WHERE t.vehicle.company.id = :companyId " +
                                    "AND t.departureDate BETWEEN :from AND :to", BigDecimal.class)
                    .setParameter("companyId", companyId)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .uniqueResult();
            return result != null ? result : BigDecimal.ZERO;
        }
    }
}