package org.transport.service;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.transport.dao.CompanyDAO;
import org.transport.dao.impl.CompanyDAOImpl;
import org.transport.entity.TransportCompany;
import org.transport.util.HibernateUtil;
import org.transport.util.ValidationUtil;

import java.util.List;
import java.util.function.Consumer;

// Business logic for managing transport companies
public class CompanyService {

    private final CompanyDAO companyDAO = new CompanyDAOImpl();

    public void createCompany(TransportCompany company) {
        ValidationUtil.validate(company);
        executeInTransaction(session -> companyDAO.save(session, company));
    }

    public void updateCompany(TransportCompany company) {
        ValidationUtil.validate(company);
        executeInTransaction(session -> companyDAO.update(session, company));
    }

    public void deleteCompany(Long id) {
        executeInTransaction(session -> companyDAO.deleteById(session, id));
    }

    public TransportCompany getById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return companyDAO.findById(session, id);
        }
    }

    public List<TransportCompany> getAllSortedByName() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return companyDAO.findAllSortedByName(session);
        }
    }

    public List<TransportCompany> getAllSortedByRevenue() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return companyDAO.findAllSortedByRevenue(session);
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