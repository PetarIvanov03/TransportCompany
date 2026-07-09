package org.transport.dao;

import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.transport.dao.impl.TransportDAOImpl;
import org.transport.entity.*;
import org.transport.entity.enums.CargoType;
import org.transport.entity.enums.PaymentStatus;
import org.transport.util.TestHibernateUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransportDAOIntegrationTest {

    private Session session;
    private TransportDAOImpl transportDAO;

    @BeforeEach
    void setUp() {
        session = TestHibernateUtil.getSessionFactory().openSession();
        transportDAO = new TransportDAOImpl();
    }

    @AfterEach
    void tearDown() {
        session.close();
    }

    @Test
    void saveAndFindByDestination_returnsCorrectTransport() {
        session.beginTransaction();

        TransportCompany company = new TransportCompany();
        company.setName("Тест Транспорт ЕООД");
        session.persist(company);

        Bus bus = new Bus();
        bus.setRegistrationNumber("PB1234AB");
        bus.setSeatCapacity(50);
        bus.setCompany(company);
        session.persist(bus);

        Client client = new Client();
        client.setName("Иван Иванов");
        session.persist(client);

        Driver driver = new Driver();
        driver.setName("Петър Петров");
        driver.setSalary(BigDecimal.valueOf(2000));
        driver.setCompany(company);
        session.persist(driver);

        Transport transport = new Transport();
        transport.setOriginPoint("Пловдив");
        transport.setDestinationPoint("Варна");
        transport.setDepartureDate(LocalDateTime.now());
        transport.setArrivalDate(LocalDateTime.now().plusHours(5));
        transport.setPrice(BigDecimal.valueOf(80));
        transport.setCargoType(CargoType.PEOPLE);
        transport.setPaymentStatus(PaymentStatus.UNPAID);
        transport.setClient(client);
        transport.setVehicle(bus);
        transport.setDriver(driver);

        transportDAO.save(session, transport);
        session.getTransaction().commit();

        session.beginTransaction();
        List<Transport> results = transportDAO.findByDestination(session, "Варна");
        session.getTransaction().commit();

        assertEquals(1, results.size());
        assertEquals("Варна", results.get(0).getDestinationPoint());
        assertEquals("Пловдив", results.get(0).getOriginPoint());
    }
}