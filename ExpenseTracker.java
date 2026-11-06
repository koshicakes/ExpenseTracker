import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class ExpenseTracker {

    private static final Path FILE = Paths.get("expenses.csv");
    private static final List<Expense> expenses = new ArrayList<>();

    static class Expense {
        final LocalDate date;
        final String category;
        final String description;
        final double amount;

        Expense(LocalDate date, String category, String description, double amount) {
            this.date = date;
            this.category = category;
            this.description = description;
            this.amount = amount;
        }

        String toCsv() {
            return date + "," + category + "," + description + "," + amount;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        load();

        System.out.println("=== Expense Tracker ===");
        boolean running = true;

        while (running) {
            System.out.println("\n1) Add expense");
            System.out.println("2) View all expenses");
            System.out.println("3) Summary by category");
            System.out.println("4) Delete an expense");
            System.out.println("5) Quit");
            System.out.print("Selection: ");

            switch (scanner.nextLine().trim()) {
                case "1": addExpense(scanner); break;
                case "2": listExpenses(); break;
                case "3": showSummary(); break;
                case "4": deleteExpense(scanner); break;
                case "5": running = false; break;
                default:  System.out.println("Please enter 1-5.");
            }
        }

        System.out.println("Goodbye!");
        scanner.close();
    }

    private static void addExpense(Scanner scanner) {
        System.out.print("Category (e.g. Food, Transport): ");
        String category = clean(scanner.nextLine());
        if (category.isEmpty()) category = "General";

        System.out.print("Description: ");
        String description = clean(scanner.nextLine());

        double amount = readAmount(scanner);

        expenses.add(new Expense(LocalDate.now(), category, description, amount));
        save();
        System.out.println("Expense added.");
    }

    private static void listExpenses() {
        if (expenses.isEmpty()) {
            System.out.println("No expenses yet.");
            return;
        }
        System.out.println();
        for (int i = 0; i < expenses.size(); i++) {
            Expense e = expenses.get(i);
            System.out.printf("%2d. %s  %-12s %-22s %10.2f%n",
                    i + 1, e.date, e.category, e.description, e.amount);
        }
    }

    private static void showSummary() {
        if (expenses.isEmpty()) {
            System.out.println("No expenses yet.");
            return;
        }
        Map<String, Double> totals = new TreeMap<>();
        double grandTotal = 0;
        for (Expense e : expenses) {
            totals.merge(e.category, e.amount, Double::sum);
            grandTotal += e.amount;
        }

        System.out.println();
        for (Map.Entry<String, Double> entry : totals.entrySet()) {
            System.out.printf("%-15s %10.2f%n", entry.getKey(), entry.getValue());
        }
        System.out.println("-------------------------------");
        System.out.printf("%-15s %10.2f%n", "TOTAL", grandTotal);
    }

    private static void deleteExpense(Scanner scanner) {
        if (expenses.isEmpty()) {
            System.out.println("No expenses to delete.");
            return;
        }
        listExpenses();
        System.out.print("Number to delete: ");
        try {
            int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (index >= 0 && index < expenses.size()) {
                expenses.remove(index);
                save();
                System.out.println("Deleted.");
                return;
            }
        } catch (NumberFormatException ignored) {
            // fall through to the error message
        }
        System.out.println("Invalid number.");
    }

    private static double readAmount(Scanner scanner) {
        while (true) {
            System.out.print("Amount: ");
            try {
                double amount = Double.parseDouble(scanner.nextLine().trim());
                if (amount > 0) return amount;
            } catch (NumberFormatException ignored) {
                // ask again
            }
            System.out.println("Please enter a positive number.");
        }
    }

    /** Commas would break the CSV format, so replace them. */
    private static String clean(String text) {
        return text.trim().replace(",", " ");
    }

    private static void load() {
        if (!Files.exists(FILE)) return;
        try {
            for (String line : Files.readAllLines(FILE)) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    expenses.add(new Expense(LocalDate.parse(parts[0]), parts[1],
                            parts[2], Double.parseDouble(parts[3])));
                }
            }
        } catch (Exception e) {
            System.out.println("Could not fully load saved expenses: " + e.getMessage());
        }
    }

    private static void save() {
        List<String> lines = new ArrayList<>();
        for (Expense e : expenses) {
            lines.add(e.toCsv());
        }
        try {
            Files.write(FILE, lines);
        } catch (IOException e) {
            System.out.println("Could not save expenses: " + e.getMessage());
        }
    }
}