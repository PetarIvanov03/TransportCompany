package org.transport.ui;

import org.transport.entity.Client;
import org.transport.service.ClientService;

import java.util.List;
import java.util.Scanner;

// Console menu for CRUD operations over clients
public class ClientMenu implements Menu {

    private final ClientService clientService;
    private final Scanner scanner;

    public ClientMenu(ClientService clientService, Scanner scanner) {
        this.clientService = clientService;
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
                    case "1" -> createClient();
                    case "2" -> updateClient();
                    case "3" -> deleteClient();
                    case "4" -> viewClient();
                    case "5" -> listClients();
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
        System.out.println("----- Клиенти -----");
        System.out.println("1. Нов клиент");
        System.out.println("2. Редакция на клиент");
        System.out.println("3. Изтриване на клиент");
        System.out.println("4. Преглед на клиент по ID");
        System.out.println("5. Списък с всички клиенти");
        System.out.println("0. Назад");
        System.out.print("Избор: ");
    }

    private void createClient() {
        String name = ConsolePrinter.readLine(scanner, "Име: ");
        String contactInfo = ConsolePrinter.readOptionalLine(scanner, "Контакти");

        Client client = new Client();
        client.setName(name);
        client.setContactInfo(contactInfo);

        clientService.createClient(client);
        ConsolePrinter.printSuccess("Клиентът беше създаден.");
    }

    private void updateClient() {
        Long id = ConsolePrinter.readLong(scanner, "ID на клиента: ");
        Client existing = clientService.getById(id);
        if (existing == null) {
            ConsolePrinter.printError("Няма клиент с ID " + id + ".");
            return;
        }

        System.out.println("Текущо име: " + existing.getName());
        String name = ConsolePrinter.readOptionalLine(scanner, "Ново име");
        if (name != null) {
            existing.setName(name);
        }

        System.out.println("Текущи контакти: " + existing.getContactInfo());
        String contactInfo = ConsolePrinter.readOptionalLine(scanner, "Нови контакти");
        if (contactInfo != null) {
            existing.setContactInfo(contactInfo);
        }

        clientService.updateClient(existing);
        ConsolePrinter.printSuccess("Клиентът беше обновен.");
    }

    private void deleteClient() {
        Long id = ConsolePrinter.readLong(scanner, "ID на клиента за изтриване: ");
        clientService.deleteClient(id);
        ConsolePrinter.printSuccess("Клиентът беше изтрит.");
    }

    private void viewClient() {
        Long id = ConsolePrinter.readLong(scanner, "ID на клиента: ");
        Client client = clientService.getById(id);
        if (client == null) {
            ConsolePrinter.printError("Няма клиент с ID " + id + ".");
            return;
        }
        printClient(client);
    }

    private void listClients() {
        List<Client> clients = clientService.getAll();
        if (clients.isEmpty()) {
            ConsolePrinter.printInfo("Няма регистрирани клиенти.");
            return;
        }
        clients.forEach(this::printClient);
    }

    private void printClient(Client client) {
        System.out.printf("ID: %d | Име: %s | Контакти: %s%n",
                client.getId(), client.getName(),
                client.getContactInfo() != null ? client.getContactInfo() : "-");
    }
}
