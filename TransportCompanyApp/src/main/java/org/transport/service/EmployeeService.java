package org.transport.service;

import org.hibernate.Session;
import org.transport.dao.EmployeeDAO;
import org.transport.dao.impl.EmployeeDAOImpl;
import org.transport.entity.Employee;
import org.transport.entity.enums.DriverQualification;
import org.transport.util.HibernateUtil;
import org.transport.util.TransactionUtil;
import org.transport.util.ValidationUtil;

import java.util.List;

// Business logic for managing employees and drivers
public class EmployeeService {

    private final EmployeeDAO employeeDAO = new EmployeeDAOImpl();

    public void createEmployee(Employee employee) {
        ValidationUtil.validate(employee);
        TransactionUtil.execute(HibernateUtil.getSessionFactory(), session -> employeeDAO.save(session, employee));
    }

    public void updateEmployee(Employee employee) {
        ValidationUtil.validate(employee);
        TransactionUtil.execute(HibernateUtil.getSessionFactory(), session -> employeeDAO.update(session, employee));
    }

    public void deleteEmployee(Long id) {
        TransactionUtil.execute(HibernateUtil.getSessionFactory(), session -> employeeDAO.deleteById(session, id));
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

    public Employee getByIdWithCompany(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return employeeDAO.findByIdWithCompany(session, id);
        }
    }

    public List<DriverQualification> getQualifications(Long driverId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return employeeDAO.findQualifications(session, driverId);
        }
    }
}