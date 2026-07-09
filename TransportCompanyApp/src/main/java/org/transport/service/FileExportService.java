package org.transport.service;

import org.transport.entity.Transport;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

// Handles serialisation of entities and reports to files (text, CSV, etc.)
public class FileExportService {

    private static final String HEADER =
            "id,originPoint,destinationPoint,departureDate,arrivalDate,price,cargoType,cargoWeight,paymentStatus,clientId,vehicleId,driverId";
    private static final String DELIMITER = ",";

    public void exportToFile(List<Transport> transports, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {

            writer.write(HEADER);
            writer.newLine();

            for (Transport t : transports) {
                writer.write(toCsvLine(t));
                writer.newLine();
            }
        }
    }

    private String toCsvLine(Transport t) {
        return String.join(DELIMITER,
                String.valueOf(t.getId()),
                t.getOriginPoint(),
                t.getDestinationPoint(),
                t.getDepartureDate().toString(),
                t.getArrivalDate().toString(),
                t.getPrice().toString(),
                t.getCargoType().name(),
                t.getCargoWeight() != null ? t.getCargoWeight().toString() : "",
                t.getPaymentStatus().name(),
                String.valueOf(t.getClient().getId()),
                String.valueOf(t.getVehicle().getId()),
                String.valueOf(t.getDriver().getId())
        );
    }
}