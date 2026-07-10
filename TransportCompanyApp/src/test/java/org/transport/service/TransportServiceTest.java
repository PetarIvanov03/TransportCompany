package org.transport.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.transport.dao.TransportDAO;
import org.transport.entity.Transport;
import org.transport.entity.enums.CargoType;
import org.transport.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class TransportServiceTest {

    @Mock
    private TransportDAO transportDAO;

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Transaction transaction;

    private TransportService transportService;
    private Transport validTransport;

    @BeforeEach
    void setUp() {
        transportService = new TransportService(transportDAO, sessionFactory);

        lenient().when(sessionFactory.openSession()).thenReturn(session);
        lenient().when(session.beginTransaction()).thenReturn(transaction);

        validTransport = new Transport();
        validTransport.setOriginPoint("Пловдив");
        validTransport.setDestinationPoint("София");
        validTransport.setDepartureDate(LocalDateTime.now());
        validTransport.setArrivalDate(LocalDateTime.now().plusHours(3));
        validTransport.setPrice(BigDecimal.valueOf(150));
        validTransport.setPaymentStatus(PaymentStatus.UNPAID);
        validTransport.setCargoType(CargoType.PEOPLE);
        validTransport.setCargoWeight(null);
    }

    @Test
    void createTransport_goodsWithoutWeight_throwsException() {
        validTransport.setCargoType(CargoType.GOODS);
        validTransport.setCargoWeight(null);

        assertThrows(IllegalArgumentException.class,
                () -> transportService.createTransport(validTransport));
    }

    @Test
    void createTransport_peopleWithWeight_throwsException() {
        validTransport.setCargoType(CargoType.PEOPLE);
        validTransport.setCargoWeight(BigDecimal.valueOf(500));

        assertThrows(IllegalArgumentException.class,
                () -> transportService.createTransport(validTransport));
    }

    @Test
    void createTransport_arrivalBeforeDeparture_throwsException() {
        validTransport.setDepartureDate(LocalDateTime.now());
        validTransport.setArrivalDate(LocalDateTime.now().minusHours(1));

        assertThrows(IllegalArgumentException.class,
                () -> transportService.createTransport(validTransport));
    }

    @Test
    void createTransport_validData_callsDaoSave() {
        transportService.createTransport(validTransport);

        verify(transportDAO).save(any(), eq(validTransport));
    }
}