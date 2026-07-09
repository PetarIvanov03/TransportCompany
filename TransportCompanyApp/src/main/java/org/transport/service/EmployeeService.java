package org.transport.service;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.transport.dao.EmployeeDAO;
import org.transport.dao.impl.EmployeeDAOImpl;
import org.transport.entity.Employee;
import org.transport.entity.enums.DriverQualification;
import org.transport.util.HibernateUtil;
import org.transport.util.ValidationUtil;

import java.util.List;
import java.util.function.Consumer;

// Business logic for managing employees and drivers
public class EmployeeService {

    private final EmployeeDAO employeeDAO = new EmployeeDAOImpl();

    public void createEmployee(Employee employee) {
        ValidationUtil.validate(employee);
        executeInTransaction(session -> employeeDAO.save(session, employee));
    }

    public void updateEmployee(Employee employee) {
        ValidationUtil.validate(employee);
        executeInTransaction(session -> employeeDAO.update(session, employee));
    }

    public void deleteEmployee(Long id) {
        executeInTransaction(session -> employeeDAO.deleteById(session, id));
    }

    public Employee getById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return employeeDAO.findById(session, id);
        }
    }

    public List<Employee> getAllSortedBySalary() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return employeeDAO.findAllSortedBySalary(session);
        }
    }

    public List<Employee> getDriversByQualification(DriverQualification qualification) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return employeeDAO.findDriversByQualification(session, qualification);
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