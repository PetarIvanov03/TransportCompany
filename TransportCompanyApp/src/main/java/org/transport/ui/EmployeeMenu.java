package org.transport.ui;

import org.transport.entity.Driver;
import org.transport.entity.Employee;
import org.transport.entity.TransportCompany;
import org.transport.entity.enums.DriverQualification;
import org.transport.service.EmployeeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

// Console menu for CRUD operations over employees and drivers
public class EmployeeMenu implements Menu {

    private static final DateTimeFormatter HIRE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final EmployeeService employeeService;
    private final Scanner scanner;

    public EmployeeMenu(EmployeeService employeeService, Scanner scanner) {
        this.employeeService = employeeService;
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
                    case "1" -> createEmployee();
                    case "2" -> updateEmployee();
                    case "3" -> deleteEmployee();
                    case "4" -> viewEmployeeWithCompany();
                    case "5" -> listBySalary();
                    case "6" -> driversByQualification();
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
        System.out.println("----- Служители -----");
        System.out.println("1. Нов служител");
        System.out.println("2. Редакция на служител");
        System.out.println("3. Изтриване на служител");
        System.out.println("4. Преглед на служител по ID (с фирма)");
        System.out.println("5. Списък по заплата");
        System.out.println("6. Шофьори по квалификация");
        System.out.println("0. Назад");
        System.out.print("Избор: ");
    }

    private void createEmployee() {
        int type = selectEmployeeType();
        String name = ConsolePrinter.readLine(scanner, "Име: ");
        BigDecimal salary = ConsolePrinter.readBigDecimal(scanner, "Заплата: ");
        LocalDate hireDate = readOptionalHireDate("Дата на наемане");
        Long companyId = ConsolePrinter.readLong(scanner, "ID на компания: ");

        Employee employee;
        if (type == 2) {
            Driver driver = new Driver();
            driver.setQualifications(readQualifications());
            employee = driver;
        } else {
            employee = new Employee();
        }

        employee.setName(name);
        employee.setSalary(salary);
        employee.setHireDate(hireDate);

        TransportCompany company = new TransportCompany();
        company.setId(companyId);
        employee.setCompany(company);

        employeeService.createEmployee(employee);
        ConsolePrinter.printSuccess("Служителят беше създаден.");
    }

    private int selectEmployeeType() {
        System.out.println("Тип служител:");
        System.out.println("1. Обикновен служител");
        System.out.println("2. Шофьор");
        while (true) {
            int choice = ConsolePrinter.readInt(scanner, "Избор: ");
            if (choice == 1 || choice == 2) {
                return choice;
            }
            ConsolePrinter.printError("Невалиден избор. Опитайте отново.");
        }
    }

    private void updateEmployee() {
        Long id = ConsolePrinter.readLong(scanner, "ID на служителя: ");
        Employee existing = employeeService.getById(id);
        if (existing == null) {
            ConsolePrinter.printError("Няма служител с ID " + id + ".");
            return;
        }

        System.out.println("Текущи данни:");
        printEmployee(existing);

        String name = ConsolePrinter.readOptionalLine(scanner, "Ново име");
        if (name != null) {
            existing.setName(name);
        }

        BigDecimal salary = ConsolePrinter.readOptionalBigDecimal(scanner, "Нова заплата");
        if (salary != null) {
            existing.setSalary(salary);
        }

        LocalDate hireDate = readOptionalHireDate("Нова дата на наемане");
        if (hireDate != null) {
            existing.setHireDate(hireDate);
        }

        if (existing instanceof Driver driver && confirm("Смяна на квалификациите?")) {
            driver.setQualifications(readQualifications());
        }

        employeeService.updateEmployee(existing);
        ConsolePrinter.printSuccess("Служителят беше обновен.");
    }

    private void deleteEmployee() {
        Long id = ConsolePrinter.readLong(scanner, "ID на служителя за изтриване: ");
        employeeService.deleteEmployee(id);
        ConsolePrinter.printSuccess("Служителят беше изтрит.");
    }

    private void viewEmployeeWithCompany() {
        Long id = ConsolePrinter.readLong(scanner, "ID на служителя: ");
        Employee employee = employeeService.getByIdWithCompany(id);
        if (employee == null) {
            ConsolePrinter.printError("Няма служител с ID " + id + ".");
            return;
        }
        printEmployeeWithCompany(employee);
    }

    private void listBySalary() {
        List<Employee> employees = employeeService.getAllSortedBySalary();
        if (employees.isEmpty()) {
            ConsolePrinter.printInfo("Няма регистрирани служители.");
            return;
        }
        employees.forEach(this::printEmployee);
    }

    private void driversByQualification() {
        DriverQualification qualification = selectEnum("Изберете квалификация:", DriverQualification.values());
        List<Employee> drivers = employeeService.getDriversByQualification(qualification);
        if (drivers.isEmpty()) {
            ConsolePrinter.printInfo("Няма шофьори с тази квалификация.");
            return;
        }
        for (Employee e : drivers) {
            printEmployee(e);
            if (e instanceof Driver driver) {
                // Fetched via a dedicated query (EmployeeService.getQualifications), so this
                // works regardless of whether the collection was fetch-joined by the caller.
                printQualifications(employeeService.getQualifications(driver.getId()));
            }
        }
    }

    // Base fields only (id, name, salary, hireDate) + a role tag. Never touches company or
    // qualifications: both are lazy and this is used for views loaded via plain getById-style
    // queries (getAllSortedBySalary, getById) that don't fetch-join either.
    private void printEmployee(Employee e) {
        String role = e instanceof Driver ? "Шофьор" : "Служител";
        System.out.printf("ID: %d | Име: %s | Заплата: %s | Дата на наемане: %s | Тип: %s%n",
                e.getId(), e.getName(), e.getSalary(),
                e.getHireDate() != null ? e.getHireDate() : "-", role);
    }

    // Detail view: requires an entity loaded via getByIdWithCompany (fetch-joined company).
    private void printEmployeeWithCompany(Employee e) {
        printEmployee(e);
        TransportCompany company = e.getCompany();
        System.out.println("Компания: " + (company != null ? company.getName() : "-"));

        if (e instanceof Driver driver) {
            printQualifications(employeeService.getQualifications(driver.getId()));
        }
    }

    private void printQualifications(List<DriverQualification> qualifications) {
        System.out.println("Квалификации: " + qualifications);
    }

    private Set<DriverQualification> readQualifications() {
        DriverQualification[] values = DriverQualification.values();
        System.out.println("Налични квалификации:");
        for (int i = 0; i < values.length; i++) {
            System.out.println((i + 1) + ". " + values[i]);
        }
        while (true) {
            String input = ConsolePrinter.readLine(scanner,
                    "Изберете квалификации (номера, разделени със запетая, Enter за никакви): ");
            if (input.isEmpty()) {
                return new LinkedHashSet<>();
            }
            Set<DriverQualification> selected = new LinkedHashSet<>();
            boolean valid = true;
            for (String token : input.split(",")) {
                try {
                    int index = Integer.parseInt(token.trim());
                    if (index < 1 || index > values.length) {
                        valid = false;
                        break;
                    }
                    selected.add(values[index - 1]);
                } catch (NumberFormatException e) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                return selected;
            }
            ConsolePrinter.printError("Невалиден избор. Използвайте номера от списъка, разделени със запетая.");
        }
    }

    // hireDate is a LocalDate (no time component) and optional (no @NotNull on the entity), so
    // ConsolePrinter.readDateTime is a poor fit: it would force an irrelevant HH:mm and has no
    // skip semantics. A small local date-only parser is simpler and matches the field's nature.
    private LocalDate readOptionalHireDate(String prompt) {
        while (true) {
            System.out.print(prompt + " (формат yyyy-MM-dd, Enter за празно): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(input, HIRE_DATE_FORMAT);
            } catch (DateTimeParseException e) {
                ConsolePrinter.printError("Невалиден формат на дата. Пример: 2026-07-10");
            }
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
