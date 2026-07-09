package org.transport.ui;

import org.transport.service.ClientService;
import org.transport.service.CompanyService;
import org.transport.service.EmployeeService;
import org.transport.service.ReportService;
import org.transport.service.TransportService;
import org.transport.service.VehicleService;
import org.transport.util.HibernateUtil;

import java.util.Scanner;

// Entry point of the console UI: owns the shared Scanner and dispatches to sub-menus
public class ConsoleApp {

    private final Scanner scanner = new Scanner(System.in);

    private final CompanyMenu companyMenu = new CompanyMenu(new CompanyService(), scanner);
    private final ClientMenu clientMenu = new ClientMenu(new ClientService(), scanner);
    private final VehicleMenu vehicleMenu = new VehicleMenu(new VehicleService(), scanner);
    private final EmployeeMenu employeeMenu = new EmployeeMenu(new EmployeeService(), scanner);
    private final TransportMenu transportMenu = new TransportMenu(new TransportService(), scanner);
    private final ReportMenu reportMenu = new ReportMenu(new ReportService(), scanner);

    public static void main(String[] args) {
        new ConsoleApp().run();
    }

    public void run() {
        boolean exit = false;
        while (!exit) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> companyMenu.show();
                case "2" -> clientMenu.show();
                case "3" -> vehicleMenu.show();
                case "4" -> employeeMenu.show();
                case "5" -> transportMenu.show();
                case "6" -> reportMenu.show();
                case "0" -> exit = true;
                default -> ConsolePrinter.printError("Невалиден избор.");
            }
        }
        HibernateUtil.shutdown();
        ConsolePrinter.printSuccess("Довиждане!");
        scanner.close();
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("===== Транспортна компания =====");
        System.out.println("1. Компании");
        System.out.println("2. Клиенти");
        System.out.println("3. МПС");
        System.out.println("4. Служители");
        System.out.println("5. Превози");
        System.out.println("6. Справки");
        System.out.println("0. Изход");
        System.out.print("Избор: ");
    }
}
