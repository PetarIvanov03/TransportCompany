package org.transport.ui;

import org.transport.entity.Bus;
import org.transport.entity.Tanker;
import org.transport.entity.TransportCompany;
import org.transport.entity.Truck;
import org.transport.entity.Vehicle;
import org.transport.entity.enums.TankerCargoType;
import org.transport.service.VehicleService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

// Console menu for CRUD operations over the vehicle fleet (Bus/Truck/Tanker)
public class VehicleMenu implements Menu {

    private final VehicleService vehicleService;
    private final Scanner scanner;

    public VehicleMenu(VehicleService vehicleService, Scanner scanner) {
        this.vehicleService = vehicleService;
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
                    case "1" -> createVehicle();
                    case "2" -> updateVehicle();
                    case "3" -> deleteVehicle();
                    case "4" -> viewVehicle();
                    case "5" -> listVehicles();
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
        System.out.println("----- МПС -----");
        System.out.println("1. Ново МПС");
        System.out.println("2. Редакция на МПС");
        System.out.println("3. Изтриване на МПС");
        System.out.println("4. Преглед на МПС по ID");
        System.out.println("5. Списък с всички МПС");
        System.out.println("0. Назад");
        System.out.print("Избор: ");
    }

    private void createVehicle() {
        int type = selectVehicleType();
        String registrationNumber = ConsolePrinter.readLine(scanner, "Регистрационен номер: ");
        Long companyId = ConsolePrinter.readLong(scanner, "ID на компания: ");

        Vehicle vehicle = switch (type) {
            case 1 -> readBusFields();
            case 2 -> readTruckFields();
            default -> readTankerFields();
        };

        vehicle.setRegistrationNumber(registrationNumber);

        TransportCompany company = new TransportCompany();
        company.setId(companyId);
        vehicle.setCompany(company);

        vehicleService.createVehicle(vehicle);
        ConsolePrinter.printSuccess("МПС-то беше създадено.");
    }

    private int selectVehicleType() {
        System.out.println("Тип МПС:");
        System.out.println("1. Автобус");
        System.out.println("2. Камион");
        System.out.println("3. Цистерна");
        while (true) {
            int choice = ConsolePrinter.readInt(scanner, "Избор: ");
            if (choice >= 1 && choice <= 3) {
                return choice;
            }
            ConsolePrinter.printError("Невалиден избор. Опитайте отново.");
        }
    }

    private Bus readBusFields() {
        Bus bus = new Bus();
        bus.setSeatCapacity(ConsolePrinter.readInt(scanner, "Брой места: "));
        return bus;
    }

    private Truck readTruckFields() {
        Truck truck = new Truck();
        truck.setMaxLoadKg(ConsolePrinter.readBigDecimal(scanner, "Максимален товар (кг): "));
        return truck;
    }

    private Tanker readTankerFields() {
        Tanker tanker = new Tanker();
        tanker.setCapacityLiters(ConsolePrinter.readBigDecimal(scanner, "Капацитет (литри): "));
        tanker.setPermittedCargoType(selectEnum("Разрешен тип товар:", TankerCargoType.values()));
        return tanker;
    }

    private void updateVehicle() {
        Long id = ConsolePrinter.readLong(scanner, "ID на МПС: ");
        Vehicle existing = vehicleService.getById(id);
        if (existing == null) {
            ConsolePrinter.printError("Няма МПС с ID " + id + ".");
            return;
        }

        System.out.println("Текущи данни:");
        printVehicle(existing);

        String registrationNumber = ConsolePrinter.readOptionalLine(scanner, "Нов регистрационен номер");
        if (registrationNumber != null) {
            existing.setRegistrationNumber(registrationNumber);
        }

        if (existing instanceof Bus bus) {
            if (confirm("Смяна на брой места?")) {
                bus.setSeatCapacity(ConsolePrinter.readInt(scanner, "Нов брой места: "));
            }
        } else if (existing instanceof Truck truck) {
            BigDecimal maxLoadKg = ConsolePrinter.readOptionalBigDecimal(scanner, "Нов максимален товар (кг)");
            if (maxLoadKg != null) {
                truck.setMaxLoadKg(maxLoadKg);
            }
        } else if (existing instanceof Tanker tanker) {
            BigDecimal capacityLiters = ConsolePrinter.readOptionalBigDecimal(scanner, "Нов капацитет (литри)");
            if (capacityLiters != null) {
                tanker.setCapacityLiters(capacityLiters);
            }
            if (confirm("Смяна на разрешен тип товар?")) {
                tanker.setPermittedCargoType(selectEnum("Нов разрешен тип товар:", TankerCargoType.values()));
            }
        } else {
            ConsolePrinter.printError("Непознат тип МПС.");
            return;
        }

        vehicleService.updateVehicle(existing);
        ConsolePrinter.printSuccess("МПС-то беше обновено.");
    }

    private void deleteVehicle() {
        Long id = ConsolePrinter.readLong(scanner, "ID на МПС за изтриване: ");
        vehicleService.deleteVehicle(id);
        ConsolePrinter.printSuccess("МПС-то беше изтрито.");
    }

    private void viewVehicle() {
        Long id = ConsolePrinter.readLong(scanner, "ID на МПС: ");
        Vehicle vehicle = vehicleService.getById(id);
        if (vehicle == null) {
            ConsolePrinter.printError("Няма МПС с ID " + id + ".");
            return;
        }
        printVehicle(vehicle);
    }

    private void listVehicles() {
        List<Vehicle> vehicles = vehicleService.getAll();
        if (vehicles.isEmpty()) {
            ConsolePrinter.printInfo("Няма регистрирани МПС.");
            return;
        }
        vehicles.forEach(this::printVehicle);
    }

    // Prints only Vehicle's own fields (plus the concrete subtype's). Never touches company or
    // transports: both are lazy on Vehicle and getById does not fetch-join them, so accessing
    // them here would throw LazyInitializationException once the loading session is closed.
    private void printVehicle(Vehicle v) {
        if (v instanceof Bus bus) {
            System.out.printf("ID: %d | Рег. номер: %s | Тип: Автобус | Брой места: %d%n",
                    bus.getId(), bus.getRegistrationNumber(), bus.getSeatCapacity());
        } else if (v instanceof Truck truck) {
            System.out.printf("ID: %d | Рег. номер: %s | Тип: Камион | Максимален товар: %s кг%n",
                    truck.getId(), truck.getRegistrationNumber(), truck.getMaxLoadKg());
        } else if (v instanceof Tanker tanker) {
            System.out.printf("ID: %d | Рег. номер: %s | Тип: Цистерна | Капацитет: %s л | Разрешен товар: %s%n",
                    tanker.getId(), tanker.getRegistrationNumber(), tanker.getCapacityLiters(),
                    tanker.getPermittedCargoType());
        } else {
            ConsolePrinter.printError("Непознат тип МПС.");
        }
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
