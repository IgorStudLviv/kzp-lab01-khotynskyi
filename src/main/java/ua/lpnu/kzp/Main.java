package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Main {

    private static final String VERSION = "1.0.0";

    public static void main(String[] args) {
        String inputPathStr = "data/input.csv";
        String outputPathStr = "out/report.txt";

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--help":
                case "-help":
                    printHelp();
                    return;
                case "--version":
                    System.out.println("Кінотеатр Лабораторна робота №1. Версія " + VERSION);
                    return;
                case "--input":
                    if (i + 1 < args.length) {
                        inputPathStr = args[++i];
                    }
                    break;
                case "--output":
                    if (i + 1 < args.length) {
                        outputPathStr = args[++i];
                    }
                    break;
                default:
                    break;
            }
        }

        Path inputPath = Paths.get(inputPathStr);
        Path outputPath = Paths.get(outputPathStr);

        if (!Files.exists(inputPath)) {
            System.err.println("Помилка: Вхідний файл не знайдено за шляхом: " + inputPath);
            return;
        }

        try {
            List<String> lines = Files.readAllLines(inputPath, StandardCharsets.UTF_8);
            processCinemaData(lines, outputPath);
        } catch (IOException e) {
            System.err.println("Помилка читання файлу: " + e.getMessage());
        }
    }

    private static void printHelp() {
        System.out.println("Використання: java -jar lab01-1.0.0.jar [опції]");
        System.out.println("Опції:");
        System.out.println("  --help, -help     Показати цю довідку");
        System.out.println("  --version         Показати версію програми");
        System.out.println("  <шлях>            Шлях до вхідного CSV файлу (за замовчуванням data/input.csv)");
        System.out.println("  --output <шлях>   Шлях до файлу звіту (за замовчуванням out/report.txt)");
    }

    private static void processCinemaData(List<String> lines, Path outputPath) throws IOException {
        int validCount = 0;
        double totalRevenue = 0.0;
        double totalTicketPriceSum = 0.0;
        int maxTicketsSold = -1;

        List<String> errors = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split(";", -1);
            if (parts.length < 3) {
                errors.add("Рядок " + (i + 1) + ": недостатньо полів у рядку");
                continue;
            }

            String movieTitle = parts[0].trim();
            if (movieTitle.isEmpty()) {
                errors.add("Рядок " + (i + 1) + ": порожня назва фільму");
                continue;
            }

            try {
                int ticketsSold = Integer.parseInt(parts[1].trim());
                double ticketPrice = Double.parseDouble(parts[2].trim());

                if (ticketsSold <= 0 || ticketPrice <= 0) {
                    errors.add("Рядок " + (i + 1) + ": містить від'ємне або нульове значення там, де це не має сенсу");
                    continue;
                }

                validCount++;
                double revenue = ticketsSold * ticketPrice;
                totalRevenue += revenue;
                totalTicketPriceSum += ticketPrice;

                if (ticketsSold > maxTicketsSold) {
                    maxTicketsSold = ticketsSold;
                }

            } catch (NumberFormatException e) {
                errors.add("Рядок " + (i + 1) + ": числове поле має помилковий формат");
            }
        }

        double averagePrice = (validCount > 0) ? (totalTicketPriceSum / validCount) : 0.0;

        StringBuilder report = new StringBuilder();
        report.append("--- Звіт Кінотеатру ---\n");
        report.append(String.format(Locale.US, "Коректних записів: %d\n", validCount));
        report.append(String.format(Locale.US, "Загальний виторг: %.2f\n", totalRevenue));
        report.append(String.format(Locale.US, "Середня ціна квитка: %.2f\n", averagePrice));
        report.append(String.format(Locale.US, "Найбільша кількість проданих квитків: %d\n", (maxTicketsSold == -1 ? 0 : maxTicketsSold)));

        report.append("\n--- Помилки (").append(errors.size()).append(") ---\n");
        for (String err : errors) {
            report.append(err).append("\n");
        }

        System.out.print(report.toString());

        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
        Files.writeString(outputPath, report.toString(), StandardCharsets.UTF_8);
    }
}