package org.transport.ui;

import org.transport.entity.Client;
import org.transport.entity.Driver;
import org.transport.entity.Transport;
import org.transport.entity.Truck;
import org.transport.entity.Vehicle;
import org.transport.entity.enums.CargoType;
import org.transport.entity.enums.PaymentStatus;
import org.transport.service.TransportService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

// Console menu for CRUD operations over transport orders (trips)
public class TransportMenu implements Menu {

    private final TransportService transportService;
    private final Scanner scanner;

    public TransportMenu(TransportService transportService, Scanner scanner) {
        this.transportService = transportService;
        this.scanner = scanner;
    }

    @Override
    public void show() {
        boolean back = false;
        while (!back) {
            printMenu();
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> createTransport();
                    case "2" -> updateTransport();
                    case "3" -> deleteTransport();
                    case "4" -> viewTransport();
                    case "5" -> searchByDestination();
                    case "6" -> searchByPeriod();
                    case "0" -> back = true;
                    default -> ConsolePrinter.printError("Невалиден избор.");
                }
            } catch (IllegalArgumentException e) {
                ConsolePrinter.printError(e.getMessage());
            } catch (RuntimeException e) {
                ConsolePrinter.printError("Неочаквана грешка: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("----- Превози -----");
        System.out.println("1. Нов превоз");
        System.out.println("2. Редакция на превоз");
        System.out.println("3. Изтриване на превоз");
        System.out.println("4. Преглед на превоз по ID (детайлно)");
        System.out.println("5. Търсене по дестинация");
        System.out.println("6. Търсене по период");
        System.out.println("0. Назад");
        System.out.print("Избор: ");
    }

    private void createTransport() {
        String originPoint = ConsolePrinter.readLine(scanner, "Начална точка: ");
        String destinationPoint = ConsolePrinter.readLine(scanner, "Крайна точка: ");
        LocalDateTime departureDate = ConsolePrinter.readDateTime(scanner, "Дата на тръгване");
        LocalDateTime arrivalDate = ConsolePrinter.readDateTime(scanner, "Дата на пристигане");
        BigDecimal price = ConsolePrinter.readBigDecimal(scanner, "Цена: ");
        CargoType cargoType = selectEnum("Вид товар:", CargoType.values());

        BigDecimal cargoWeight = null;
        if (cargoType == CargoType.GOODS) {
            cargoWeight = ConsolePrinter.readBigDecimal(scanner, "Тегло на товара (кг): ");
        }

        PaymentStatus paymentStatus = selectEnum("Статус на плащане:", PaymentStatus.values());

        Long clientId = ConsolePrinter.readLong(scanner, "ID на клиент: ");
        Long vehicleId = ConsolePrinter.readLong(scanner, "ID на МПС: ");
        Long driverId = ConsolePrinter.readLong(scanner, "ID на шофьор: ");

        Transport transport = new Transport();
        transport.setOriginPoint(originPoint);
        transport.setDestinationPoint(destinationPoint);
        transport.setDepartureDate(departureDate);
        transport.setArrivalDate(arrivalDate);
        transport.setPrice(price);
        transport.setCargoType(cargoType);
        transport.setCargoWeight(cargoWeight);
        transport.setPaymentStatus(paymentStatus);
        transport.setClient(idReference(new Client(), clientId));
        transport.setVehicle(idReference(new Truck(), vehicleId));
        transport.setDriver(idReference(new Driver(), driverId));

        transportService.createTransport(transport);
        ConsolePrinter.printSuccess("Превозът беше създаден.");
    }

    private void updateTransport() {
        Long id = ConsolePrinter.readLong(scanner, "ID на превоза: ");
        Transport existing = transportService.getByIdWithDetails(id);
        if (existing == null) {
            ConsolePrinter.printError("Няма превоз с ID " + id + ".");
            return;
        }

        System.out.println("Текущи данни:");
        printTransportDetails(existing);

        String originPoint = ConsolePrinter.readOptionalLine(scanner, "Нова начална точка");
        if (originPoint != null) {
            existing.setOriginPoint(originPoint);
        }

        String destinationPoint = ConsolePrinter.readOptionalLine(scanner, "Нова крайна точка");
        if (destinationPoint != null) {
            existing.setDestinationPoint(destinationPoint);
        }

        if (confirm("Смяна на дата на тръгване?")) {
            existing.setDepartureDate(ConsolePrinter.readDateTime(scanner, "Нова дата на тръгване"));
        }

        if (confirm("Смяна на дата на пристигане?")) {
            existing.setArrivalDate(ConsolePrinter.readDateTime(scanner, "Нова дата на пристигане"));
        }

        BigDecimal price = ConsolePrinter.readOptionalBigDecimal(scanner, "Нова цена");
        if (price != null) {
            existing.setPrice(price);
        }

        if (confirm("Смяна на вид товар?")) {
            CargoType cargoType = selectEnum("Нов вид товар:", CargoType.values());
            existing.setCargoType(cargoType);
            existing.setCargoWeight(cargoType == CargoType.GOODS
                    ? ConsolePrinter.readBigDecimal(scanner, "Тегло на товара (кг): ")
                    : null);
        }

        if (confirm("Смяна на статус на плащане?")) {
            existing.setPaymentStatus(selectEnum("Нов статус на плащане:", PaymentStatus.values()));
        }

        if (confirm("Смяна на клиент?")) {
            Long clientId = ConsolePrinter.readLong(scanner, "Ново ID на клиент: ");
            existing.setClient(idReference(new Client(), clientId));
        }

        if (confirm("Смяна на МПС?")) {
            Long vehicleId = ConsolePrinter.readLong(scanner, "Ново ID на МПС: ");
            existing.setVehicle(idReference(new Truck(), vehicleId));
        }

        if (confirm("Смяна на шофьор?")) {
            Long driverId = ConsolePrinter.readLong(scanner, "Ново ID на шофьор: ");
            existing.setDriver(idReference(new Driver(), driverId));
        }

        transportService.updateTransport(existing);
        ConsolePrinter.printSuccess("Превозът беше обновен.");
    }

    private void deleteTransport() {
        Long id = ConsolePrinter.readLong(scanner, "ID на превоза за изтриване: ");
        transportService.deleteTransport(id);
        ConsolePrinter.printSuccess("Превозът беше изтрит.");
    }

    private void viewTransport() {
        Long id = ConsolePrinter.readLong(scanner, "ID на превоза: ");
        Transport transport = transportService.getByIdWithDetails(id);
        if (transport == null) {
            ConsolePrinter.printError("Няма превоз с ID " + id + ".");
            return;
        }
        printTransportDetails(transport);
    }

    private void searchByDestination() {
        String destination = ConsolePrinter.readLine(scanner, "Дестинация: ");
        List<Transport> transports = transportService.getByDestination(destination);
        if (transports.isEmpty()) {
            ConsolePrinter.printInfo("Няма превози до \"" + destination + "\".");
            return;
        }
        transports.forEach(this::printTransportSummary);
    }

    private void searchByPeriod() {
        LocalDateTime from = ConsolePrinter.readDateTime(scanner, "От дата");
        LocalDateTime to = ConsolePrinter.readDateTime(scanner, "До дата");
        List<Transport> transports = transportService.getByDateRange(from, to);
        if (transports.isEmpty()) {
            ConsolePrinter.printInfo("Няма превози в посочения период.");
            return;
        }
        transports.forEach(this::printTransportSummary);
    }

    // List view: only Transport's own fields. Safe to use with entities loaded via plain
    // getById-style queries (getByDestination, getByDateRange) that do not fetch-join relations.
    private void printTransportSummary(Transport t) {
        System.out.printf("ID: %d | %s -> %s | Тръгване: %s | Пристигане: %s | Цена: %s | Товар: %s | Плащане: %s%n",
                t.getId(), t.getOriginPoint(), t.getDestinationPoint(),
                t.getDepartureDate(), t.getArrivalDate(), t.getPrice(),
                t.getCargoType(), t.getPaymentStatus());
    }

    // Detail view: requires an entity loaded via getByIdWithDetails (fetch-joined
    // client/vehicle/driver), otherwise accessing these lazy associations would throw
    // LazyInitializationException once the loading session is closed.
    private void printTransportDetails(Transport t) {
        printTransportSummary(t);
        System.out.println("Клиент: " + t.getClient().getName());
        System.out.println("МПС: " + t.getVehicle().getRegistrationNumber());
        System.out.println("Шофьор: " + t.getDriver().getName());
    }

    // Vehicle is abstract, so a concrete subclass is needed to instantiate a placeholder.
    // The subtype is irrelevant here: the association has no cascade, so Hibernate only
    // reads the id off the object to populate the foreign key column, never persists it.
    private <T> T idReference(T entity, Long id) {
        if (entity instanceof Client client) {
            client.setId(id);
        } else if (entity instanceof Vehicle vehicle) {
            vehicle.setId(id);
        } else if (entity instanceof Driver driver) {
            driver.setId(id);
        }
        return entity;
    }

    private <T extends Enum<T>> T selectEnum(String title, T[] values) {
        System.out.println(title);
        for (int i = 0; i < values.length; i++) {
            System.out.println((i + 1) + ". " + values[i]);
        }
        while (true) {
            int choice = ConsolePrinter.readInt(scanner, "Избор: ");
            if (choice >= 1 && choice <= values.length) {
                return values[choice - 1];
            }
            ConsolePrinter.printError("Невалиден избор. Опитайте отново.");
        }
    }

    private boolean confirm(String question) {
        String answer = ConsolePrinter.readLine(scanner, question + " (y/n): ");
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("да");
    }
}
