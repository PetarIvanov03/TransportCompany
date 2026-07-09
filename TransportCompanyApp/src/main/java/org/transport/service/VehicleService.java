package org.transport.service;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.transport.dao.VehicleDAO;
import org.transport.dao.impl.VehicleDAOImpl;
import org.transport.entity.Vehicle;
import org.transport.util.HibernateUtil;
import org.transport.util.ValidationUtil;

import java.util.List;
import java.util.function.Consumer;

// Business logic for managing the vehicle fleet
public class VehicleService {

    private final VehicleDAO vehicleDAO = new VehicleDAOImpl();

    public void createVehicle(Vehicle vehicle) {
        ValidationUtil.validate(vehicle);
        executeInTransaction(session -> vehicleDAO.save(session, vehicle));
    }

    public void updateVehicle(Vehicle vehicle) {
        ValidationUtil.validate(vehicle);
        executeInTransaction(session -> vehicleDAO.update(session, vehicle));
    }

    public void deleteVehicle(Long id) {
        executeInTransaction(session -> vehicleDAO.deleteById(session, id));
    }

    public Vehicle getById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return vehicleDAO.findById(session, id);
        }
    }

    public List<Vehicle> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return vehicleDAO.findAll(session);
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