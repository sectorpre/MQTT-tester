package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVWriterExample {
    public static void main(String[] args) {
        // Sample data to write to CSV
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"delay", "qos", "instance-count", "subscribe qos", "messages per second", "out of count",
                "med 1", "med 2", "med 3", "med 4", "med 5", "messages lost"});

        // Path to the CSV file
        String csvFile = "data.csv";

        // Write data to CSV file
        try (FileWriter writer = new FileWriter(csvFile)) {
            for (String[] row : data) {
                writer.append(String.join(",", row));
                writer.append("\n");
            }
            System.out.println("Data has been written to " + csvFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}