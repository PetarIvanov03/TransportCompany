package org.transport.ui;

import org.transport.entity.Driver;
import org.transport.entity.Transport;
import org.transport.service.FileExportService;
import org.transport.service.ReportService;
import org.transport.service.TransportService;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// Console menu for read-only reports (справки)
public class ReportMenu implements Menu {

    private final ReportService reportService;
    private final Scanner scanner;

    // Both have no external dependencies (TransportService has a public no-arg constructor,
    // same as ReportService), so they're built directly here — same convention *DAOImpl classes
    // already use inside the Service layer. ConsoleApp's constructor call is left untouched.
    private final TransportService transportService = new TransportService();
    private final FileExportService fileExportService = new FileExportService();

    public ReportMenu(ReportService reportService, Scanner scanner) {
        this.reportService = reportService;
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
                    case "1" -> showTotalTransportCount();
                    case "2" -> showTotalRevenue();
                    case "3" -> showTransportCountPerDriver();
                    case "4" -> showRevenuePerDriver();
                    case "5" -> showCompanyRevenueForPeriod();
                    case "6" -> exportTransports();
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
        System.out.println("----- Справки -----");
        System.out.println("1. Общ брой превози");
        System.out.println("2. Обща сума на превозите");
        System.out.println("3. Брой превози по шофьор");
        System.out.println("4. Приходи по шофьор");
        System.out.println("5. Приходи на компания за период");
        System.out.println("6. Експорт на превози във файл (CSV)");
        System.out.println("0. Назад");
        System.out.print("Избор: ");
    }

    private void showTotalTransportCount() {
        long count = reportService.getTotalTransportCount();
        ConsolePrinter.printInfo("Общ брой превози: " + count);
    }

    private void showTotalRevenue() {
        BigDecimal total = reportService.getTotalRevenue();
        ConsolePrinter.printInfo("Обща сума на превозите: " + total + " лв.");
    }

    // Driver keys come straight from a GROUP BY projection (t.driver) within the still-open
    // session, so driver.getName() (a base field) is safe. Do NOT touch
    // getQualifications()/getCompany() here — ReportService closes the session right after
    // .list(), so anything beyond Driver/Employee's own base fields is no longer accessible.
    private void showTransportCountPerDriver() {
        Map<Driver, Long> counts = reportService.getTransportCountPerDriver();
        if (counts.isEmpty()) {
            ConsolePrinter.printInfo("Няма данни.");
            return;
        }
        for (Map.Entry<Driver, Long> entry : counts.entrySet()) {
            System.out.printf("%s: %d превоза%n", entry.getKey().getName(), entry.getValue());
        }
    }

    // Same caveat as showTransportCountPerDriver — base fields on Driver only.
    private void showRevenuePerDriver() {
        Map<Driver, BigDecimal> revenues = reportService.getRevenuePerDriver();
        if (revenues.isEmpty()) {
            ConsolePrinter.printInfo("Няма данни.");
            return;
        }
        for (Map.Entry<Driver, BigDecimal> entry : revenues.entrySet()) {
            System.out.printf("%s: %s лв.%n", entry.getKey().getName(), entry.getValue());
        }
    }

    private void showCompanyRevenueForPeriod() {
        Long companyId = ConsolePrinter.readLong(scanner, "ID на компания: ");
        LocalDateTime from = ConsolePrinter.readDateTime(scanner, "От дата");
        LocalDateTime to = ConsolePrinter.readDateTime(scanner, "До дата");
        BigDecimal revenue = reportService.getCompanyRevenueForPeriod(companyId, from, to);
        ConsolePrinter.printInfo("Приходи на компанията за периода: " + revenue + " лв.");
    }

    private void exportTransports() {
        List<Transport> transports = selectTransportsForExport();
        if (transports.isEmpty()) {
            ConsolePrinter.printInfo("Няма превози за експортиране.");
            return;
        }

        String filePath = ConsolePrinter.readLine(scanner, "Път до файл (напр. export.csv): ");
        try {
            fileExportService.exportToFile(transports, filePath);
            ConsolePrinter.printSuccess("Експортът е успешен: " + filePath);
        } catch (IOException e) {
            ConsolePrinter.printError("Неуспешен запис на файл: " + e.getMessage());
        }
    }

    private List<Transport> selectTransportsForExport() {
        System.out.println("Филтър за експорт:");
        System.out.println("1. По дестинация");
        System.out.println("2. По период");
        System.out.println("3. Всички");
        while (true) {
            int choice = ConsolePrinter.readInt(scanner, "Избор: ");
            switch (choice) {
                case 1 -> {
                    String destination = ConsolePrinter.readLine(scanner, "Дестинация: ");
                    return transportService.getByDestination(destination);
                }
                case 2 -> {
                    LocalDateTime from = ConsolePrinter.readDateTime(scanner, "От дата");
                    LocalDateTime to = ConsolePrinter.readDateTime(scanner, "До дата");
                    return transportService.getByDateRange(from, to);
                }
                case 3 -> {
                    return transportService.getAll();
                }
                default -> ConsolePrinter.printError("Невалиден избор. Опитайте отново.");
            }
        }
    }
}
