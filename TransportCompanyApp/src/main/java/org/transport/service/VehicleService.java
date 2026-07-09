package org.transport.service;

import org.hibernate.Session;
import org.transport.dao.VehicleDAO;
import org.transport.dao.impl.VehicleDAOImpl;
import org.transport.entity.Vehicle;
import org.transport.util.HibernateUtil;
import org.transport.util.TransactionUtil;
import org.transport.util.ValidationUtil;

import java.util.List;

// Business logic for managing the vehicle fleet
public class VehicleService {

    private final VehicleDAO vehicleDAO = new VehicleDAOImpl();

    public void createVehicle(Vehicle vehicle) {
        ValidationUtil.validate(vehicle);
        TransactionUtil.execute(HibernateUtil.getSessionFactory(), session -> vehicleDAO.save(session, vehicle));
    }

    public void updateVehicle(Vehicle vehicle) {
        ValidationUtil.validate(vehicle);
        TransactionUtil.execute(HibernateUtil.getSessionFactory(), session -> vehicleDAO.update(session, vehicle));
    }

    public void deleteVehicle(Long id) {
        TransactionUtil.execute(HibernateUtil.getSessionFactory(), session -> vehicleDAO.deleteById(session, id));
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

}