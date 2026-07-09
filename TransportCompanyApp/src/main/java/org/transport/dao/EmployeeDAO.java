package org.transport.dao;

import org.hibernate.Session;
import org.transport.entity.Employee;
import org.transport.entity.enums.DriverQualification;

import java.util.List;

// Data-access contract for Employee entities
public interface EmployeeDAO extends GenericDAO<org.transport.entity.Employee, Long> {
    List<Employee> findAllSortedBySalary(Session session);
    List<Employee> findDriversByQualification(Session session, DriverQualification qualification);
    Employee findByIdWithCompany(Session session, Long id);
    List<DriverQualification> findQualifications(Session session, Long driverId);
}