package org.transport.dao.impl;

import org.hibernate.Session;
import org.transport.dao.EmployeeDAO;
import org.transport.entity.Employee;
import org.transport.entity.enums.DriverQualification;

import java.util.List;

// Hibernate implementation of EmployeeDAO
public class EmployeeDAOImpl extends AbstractGenericDAO<Employee, Long> implements EmployeeDAO {

    public EmployeeDAOImpl() {
        super(Employee.class);
    }

    @Override
    public List<Employee> findAllSortedBySalary(Session session) {
        return session.createQuery(
                        "FROM Employee e ORDER BY e.salary DESC", Employee.class)
                .list();
    }

    @Override
    public List<Employee> findDriversByQualification(Session session, DriverQualification qualification) {
        return session.createQuery(
                        "SELECT DISTINCT d FROM Driver d " +
                                "JOIN d.qualifications q " +
                                "WHERE q = :qualification", Employee.class)
                .setParameter("qualification", qualification)
                .list();
    }

    @Override
    public Employee findByIdWithCompany(Session session, Long id) {
        return session.createQuery(
                        "SELECT e FROM Employee e " +
                                "LEFT JOIN FETCH e.company " +
                                "WHERE e.id = :id", Employee.class)
                .setParameter("id", id)
                .uniqueResult();
    }

    @Override
    public List<DriverQualification> findQualifications(Session session, Long driverId) {
        return session.createQuery(
                        "SELECT q FROM Driver d JOIN d.qualifications q WHERE d.id = :id",
                        DriverQualification.class)
                .setParameter("id", driverId)
                .list();
    }
}