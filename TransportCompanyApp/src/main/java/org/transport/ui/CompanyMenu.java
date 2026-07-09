package org.transport.ui;

import org.transport.entity.Bus;
import org.transport.entity.Driver;
import org.transport.entity.Employee;
import org.transport.entity.Tanker;
import org.transport.entity.TransportCompany;
import org.transport.entity.Truck;
import org.transport.entity.Vehicle;
import org.transport.service.CompanyService;

import java.util.List;
import java.util.Scanner;

// Console menu for CRUD operations over transport companies
public class CompanyMenu implements Menu {

    private final CompanyService companyService;
    private final Scanner scanner;

    public CompanyMenu(CompanyService companyService, Scanner scanner) {
        this.companyService = companyService;
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
                    case "1" -> createCompany();
                    case "2" -> updateCompany();
                    case "3" -> deleteCompany();
                    case "4" -> viewCompanyWithVehicles();
                    case "5" -> viewCompanyWithEmployees();
                    case "6" -> listByName();
                    case "7" -> listByRevenue();
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
        System.out.println("----- Компании -----");
        System.out.println("1. Нова компания");
        System.out.println("2. Редакция на компания");
        System.out.println("3. Изтриване на компания");
        System.out.println("4. Преглед на компания по ID (МПС-та)");
        System.out.println("5. Преглед на компания по ID (служители)");
        System.out.println("6. Списък по име");
        System.out.println("7. Списък по приходи");
        System.out.println("0. Назад");
        System.out.print("Избор: ");
    }

    private void createCompany() {
        String name = ConsolePrinter.readLine(scanner, "Име: ");
        // address/contactInfo carry no @NotBlank on the entity, so a plain readLine (which
        // allows an empty string) is enough — no need for the "Enter to skip" semantics of
        // readOptionalLine here, since there is nothing to skip on a brand-new entity.
        String address = ConsolePrinter.readLine(scanner, "Адрес: ");
        String contactInfo = ConsolePrinter.readLine(scanner, "Контакти: ");

        TransportCompany company = new TransportCompany();
        company.setName(name);
        company.setAddress(address);
        company.setContactInfo(contactInfo);

        companyService.createCompany(company);
        ConsolePrinter.printSuccess("Компанията беше създадена.");
    }

    private void updateCompany() {
        Long id = ConsolePrinter.readLong(scanner, "ID на компанията: ");
        TransportCompany existing = companyService.getById(id);
        if (existing == null) {
            ConsolePrinter.printError("Няма компания с ID " + id + ".");
            return;
        }

        System.out.println("Текущи данни:");
        printCompany(existing);

        String name = ConsolePrinter.readOptionalLine(scanner, "Ново име");
        if (name != null) {
            existing.setName(name);
        }

        String address = ConsolePrinter.readOptionalLine(scanner, "Нов адрес");
        if (address != null) {
            existing.setAddress(address);
        }

        String contactInfo = ConsolePrinter.readOptionalLine(scanner, "Нови контакти");
        if (contactInfo != null) {
            existing.setContactInfo(contactInfo);
        }

        companyService.updateCompany(existing);
        ConsolePrinter.printSuccess("Компанията беше обновена.");
    }

    private void deleteCompany() {
        Long id = ConsolePrinter.readLong(scanner, "ID на компанията за изтриване: ");
        // employees/vehicles use cascade = PERSIST only (no REMOVE), so deleting a company that
        // still has related rows will likely fail with a foreign key constraint violation from
        // the DB. That surfaces as a RuntimeException, already handled by the generic catch in
        // show() — no cascade-delete logic is attempted here.
        companyService.deleteCompany(id);
        ConsolePrinter.printSuccess("Компанията беше изтрита.");
    }

    private void viewCompanyWithVehicles() {
        Long id = ConsolePrinter.readLong(scanner, "ID на компанията: ");
        TransportCompany company = companyService.getByIdWithVehicles(id);
        if (company == null) {
            ConsolePrinter.printError("Няма компания с ID " + id + ".");
            return;
        }

        printCompany(company);

        List<Vehicle> vehicles = company.getVehicles();
        if (vehicles == null || vehicles.isEmpty()) {
            ConsolePrinter.printInfo("Няма регистрирани МПС за тази компания.");
            return;
        }
        System.out.println("МПС:");
        vehicles.forEach(this::printVehicleSummary);
    }

    private void viewCompanyWithEmployees() {
        Long id = ConsolePrinter.readLong(scanner, "ID на компанията: ");
        TransportCompany company = companyService.getByIdWithEmployees(id);
        if (company == null) {
            ConsolePrinter.printError("Няма компания с ID " + id + ".");
            return;
        }

        printCompany(company);

        List<Employee> employees = company.getEmployees();
        if (employees == null || employees.isEmpty()) {
            ConsolePrinter.printInfo("Няма регистрирани служители за тази компания.");
            return;
        }
        System.out.println("Служители:");
        employees.forEach(this::printEmployeeSummary);
    }

    private void listByName() {
        List<TransportCompany> companies = companyService.getAllSortedByName();
        if (companies.isEmpty()) {
            ConsolePrinter.printInfo("Няма регистрирани компании.");
            return;
        }
        companies.forEach(this::printCompany);
    }

    private void listByRevenue() {
        List<TransportCompany> companies = companyService.getAllSortedByRevenue();
        if (companies.isEmpty()) {
            ConsolePrinter.printInfo("Няма регистрирани компании.");
            return;
        }
        companies.forEach(this::printCompany);
    }

    // Base fields only. Never touches employees/vehicles: both are lazy and this is used by
    // views loaded via plain getById-style queries (getById, getAllSortedByName,
    // getAllSortedByRevenue) that don't fetch-join either collection.
    private void printCompany(TransportCompany c) {
        System.out.printf("ID: %d | Име: %s | Адрес: %s | Контакти: %s%n",
                c.getId(),
                c.getName(),
                c.getAddress() != null && !c.getAddress().isBlank() ? c.getAddress() : "-",
                c.getContactInfo() != null && !c.getContactInfo().isBlank() ? c.getContactInfo() : "-");
    }

    // Just registration number + type label — full type-specific details (seat capacity, max
    // load, etc.) belong to VehicleMenu.printVehicle; duplicating that here isn't needed.
    private void printVehicleSummary(Vehicle v) {
        String type;
        if (v instanceof Bus) {
            type = "Автобус";
        } else if (v instanceof Truck) {
            type = "Камион";
        } else if (v instanceof Tanker) {
            type = "Цистерна";
        } else {
            type = "Непознат тип";
        }
        System.out.printf("  - %s (%s)%n", v.getRegistrationNumber(), type);
    }

    // name + salary + a "(Шофьор)" tag for Drivers. Never touches qualifications: it isn't
    // fetch-joined by getByIdWithEmployees and @ElementCollection is lazy by default.
    private void printEmployeeSummary(Employee e) {
        String driverTag = e instanceof Driver ? " (Шофьор)" : "";
        System.out.printf("  - %s | Заплата: %s%s%n", e.getName(), e.getSalary(), driverTag);
    }
}
