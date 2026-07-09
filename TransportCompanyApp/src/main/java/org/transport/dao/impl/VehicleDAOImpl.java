package org.transport.dao.impl;

import org.transport.dao.VehicleDAO;
import org.transport.entity.Vehicle;

// Hibernate implementation of VehicleDAO
public class VehicleDAOImpl extends AbstractGenericDAO<Vehicle, Long> implements VehicleDAO {

    public VehicleDAOImpl() {
        super(Vehicle.class);
    }
}