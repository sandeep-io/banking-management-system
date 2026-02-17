package BankingManagementSystem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Scanner;

public class Accounts {

    private final Connection connection;
    private final Scanner scanner;

    public Accounts(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    /* ---------------- OPEN ACCOUNT ---------------- */
    public long openAccount(String email) {

        email = email.trim();

        if (accountExists(email)) {
            throw new RuntimeException("Account already exists for this email!");
        }

        System.out.print("Enter Full Name: ");
        String fullName = scanner.nextLine().trim();

        System.out.print("Enter Initial Deposit: ");
        BigDecimal balance = scanner.nextBigDecimal();
        scanner.nextLine(); // clear buffer

        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Initial deposit cannot be negative!");
        }

        System.out.print("Enter Security Pin: ");
        String pin = scanner.nextLine().trim();

        if (pin.length() < 4) {
            throw new RuntimeException("PIN must be at least 4 digits!");
        }

        long accountNumber = generateAccountNumber();

        String query = """
                INSERT INTO accounts (account_number, full_name, email, balance, security_pin)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setLong(1, accountNumber);
            stmt.setString(2, fullName);
            stmt.setString(3, email);
            stmt.setBigDecimal(4, balance);
            stmt.setString(5, pin);

            stmt.executeUpdate();
            System.out.println("Account created successfully!");
            return accountNumber;

        } catch (SQLException e) {
            throw new RuntimeException("Account creation failed", e);
        }
    }

    /* ---------------- GET ACCOUNT NUMBER ---------------- */
    public long getAccountNumber(String email) {

        email = email.trim();

        String query = "SELECT account_number FROM accounts WHERE email = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getLong("account_number");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Account not found!");
    }

    /* ---------------- ACCOUNT EXISTS ---------------- */
    public boolean accountExists(String email) {

        email = email.trim();

        String query = "SELECT 1 FROM accounts WHERE email = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /* ---------------- GENERATE ACCOUNT NUMBER ---------------- */
    private long generateAccountNumber() {

        String query = "SELECT MAX(account_number) AS max_acc FROM accounts";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next() && rs.getLong("max_acc") > 0) {
                return rs.getLong("max_acc") + 1;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 10000100L;
    }
}
