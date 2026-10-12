package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public final class Main {

    private Main() {
    }
    public static void main(String[] args) {
        Path inputPath = Path.of("data", "input.csv");
        Path outputPath = Path.of("out", "report.txt");

        for (int i = 0; i < args.length; i++) {
            if ("--help".equals(args[i])) {
                System.out.printf("Використання: java -jar lab01.jar [--help] [--input <файл>] [--output <файл>] [--version]%n");
                return;
            } else if ("--version".equals(args[i])) {
                System.out.printf("Версія: 1.0.0%n");
                return;
            } else if ("--input".equals(args[i]) && i + 1 < args.length) {
                inputPath = Path.of(args[++i]);
            } else if ("--output".equals(args[i]) && i + 1 < args.length) {
                outputPath = Path.of(args[++i]);
            }
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(inputPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.printf("Помилка читання вхідного файла: %s%n", e.getMessage());
            return;
        }
        List<String> errors = new ArrayList<>();
        int validCount = 0;
        double totalRevenue = 0.0;
        double sumTicketPrice = 0.0;
        int maxSoldTickets = Integer.MIN_VALUE;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] fields = line.split(";", -1);

            if (fields.length != 5) {
                errors.add(String.format("Рядок %d: неправильна кількість полів (очікується 5)", i + 1));
                continue;
            }

            if (fields[0].isBlank()) {
                errors.add(String.format("Рядок %d: порожня назва фільму", i + 1));
                continue;
            }

            try {
                int hall = Integer.parseInt(fields[1]);
                double ticketPrice = Double.parseDouble(fields[2]);
                int sold = Integer.parseInt(fields[3]);
                int durationMin = Integer.parseInt(fields[4]);

                if (hall <= 0 || ticketPrice < 0 || sold < 0 || durationMin <= 0) {
                    errors.add(String.format("Рядок %d: містить від'ємне або нульове значення там, де це не має сенсу", i + 1));
                    continue;
                }
                validCount++;
                totalRevenue += (ticketPrice * sold);
                sumTicketPrice += ticketPrice;
                maxSoldTickets = Math.max(maxSoldTickets, sold);

            } catch (NumberFormatException e) {
                errors.add(String.format("Рядок %d: числове поле має помилковий формат", i + 1));
            }
        }
        double averageTicketPrice = (validCount > 0) ? sumTicketPrice / validCount : 0.0;
        StringBuilder report = new StringBuilder();
        report.append(String.format(Locale.ROOT, "--- Звіт Кінотеатру ---%n"));
        report.append(String.format(Locale.ROOT, "Коректних записів: %d%n", validCount));
        report.append(String.format(Locale.ROOT, "Загальний виторг: %.2f%n", totalRevenue));
        report.append(String.format(Locale.ROOT, "Середня ціна квитка: %.2f%n", averageTicketPrice));
        if (validCount > 0) {
            report.append(String.format(Locale.ROOT, "Найбільша кількість проданих квитків: %d%n", maxSoldTickets));
        }
        report.append(String.format(Locale.ROOT, "%n--- Помилки (%d) ---%n", errors.size()));
        for (String error : errors) {
            report.append(error).append(System.lineSeparator());
        }
        String finalReport = report.toString();
        System.out.print(finalReport);
        try {
            if (outputPath.getParent() != null) {
                Files.createDirectories(outputPath.getParent());
            }
            Files.writeString(outputPath, finalReport, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.printf("Помилка створення файлу звіту: %s%n", outputPath);
        }
    }
}