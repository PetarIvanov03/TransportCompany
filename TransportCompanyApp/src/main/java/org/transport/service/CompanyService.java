package org.transport.service;

import org.hibernate.Session;
import org.transport.dao.CompanyDAO;
import org.transport.dao.impl.CompanyDAOImpl;
import org.transport.entity.TransportCompany;
import org.transport.util.HibernateUtil;
import org.transport.util.TransactionUtil;
import org.transport.util.ValidationUtil;

import java.util.List;

// Business logic for managing transport companies
public class CompanyService {

    private final CompanyDAO companyDAO = new CompanyDAOImpl();

    public void createCompany(TransportCompany company) {
        ValidationUtil.validate(company);
        TransactionUtil.execute(HibernateUtil.getSessionFactory(), session -> companyDAO.save(session, company));
    }

    public void updateCompany(TransportCompany company) {
        ValidationUtil.validate(company);
        TransactionUtil.execute(HibernateUtil.getSessionFactory(), session -> companyDAO.update(session, company));
    }

    public void deleteCompany(Long id) {
        TransactionUtil.execute(HibernateUtil.getSessionFactory(), session -> companyDAO.deleteById(session, id));
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

    public TransportCompany getByIdWithVehicles(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return companyDAO.findByIdWithVehicles(session, id);
        }
    }

    public TransportCompany getByIdWithEmployees(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return companyDAO.findByIdWithEmployees(session, id);
        }
    }
}