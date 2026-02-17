package BankingManagementSystem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Scanner;

public class AccountManager {

    private final Connection connection;
    private final Scanner scanner;

    public AccountManager(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    /* ---------------- CREDIT MONEY ---------------- */
    public void creditMoney(long accountNumber) {

        System.out.print("Enter Amount: ");
        BigDecimal amount = scanner.nextBigDecimal();
        scanner.nextLine();

        System.out.print("Enter Security Pin: ");
        String pin = scanner.nextLine();

        String checkQuery = "SELECT 1 FROM Accounts WHERE account_number = ? AND security_pin = ?";
        String creditQuery = "UPDATE Accounts SET balance = balance + ? WHERE account_number = ?";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setLong(1, accountNumber);
                checkStmt.setString(2, pin);

                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    System.out.println("Invalid Security Pin!");
                    return;
                }
            }

            try (PreparedStatement creditStmt = connection.prepareStatement(creditQuery)) {
                creditStmt.setBigDecimal(1, amount);
                creditStmt.setLong(2, accountNumber);

                if (creditStmt.executeUpdate() > 0) {
                    connection.commit();
                    System.out.println("Rs." + amount + " credited successfully");
                } else {
                    connection.rollback();
                    System.out.println("Transaction Failed!");
                }
            }

        } catch (SQLException e) {
            rollbackQuietly();
            e.printStackTrace();
        } finally {
            resetAutoCommit();
        }
    }

    /* ---------------- DEBIT MONEY ---------------- */
    public void debitMoney(long accountNumber) {

        System.out.print("Enter Amount: ");
        BigDecimal amount = scanner.nextBigDecimal();
        scanner.nextLine();

        System.out.print("Enter Security Pin: ");
        String pin = scanner.nextLine();

        String selectQuery = "SELECT balance FROM Accounts WHERE account_number = ? AND security_pin = ?";
        String debitQuery = "UPDATE Accounts SET balance = balance - ? WHERE account_number = ?";

        try {
            connection.setAutoCommit(false);

            BigDecimal balance;

            try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
                selectStmt.setLong(1, accountNumber);
                selectStmt.setString(2, pin);
                ResultSet rs = selectStmt.executeQuery();

                if (!rs.next()) {
                    System.out.println("Invalid Security Pin!");
                    return;
                }
                balance = rs.getBigDecimal("balance");
            }

            if (amount.compareTo(balance) > 0) {
                System.out.println("Insufficient Balance!");
                return;
            }

            try (PreparedStatement debitStmt = connection.prepareStatement(debitQuery)) {
                debitStmt.setBigDecimal(1, amount);
                debitStmt.setLong(2, accountNumber);

                debitStmt.executeUpdate();
                connection.commit();
                System.out.println("Rs." + amount + " debited successfully");
            }

        } catch (SQLException e) {
            rollbackQuietly();
            e.printStackTrace();
        } finally {
            resetAutoCommit();
        }
    }

    /* ---------------- TRANSFER MONEY ---------------- */
    public void transferMoney(long senderAccount) {

        System.out.print("Enter Receiver Account Number: ");
        long receiverAccount = scanner.nextLong();

        System.out.print("Enter Amount: ");
        BigDecimal amount = scanner.nextBigDecimal();
        scanner.nextLine();

        System.out.print("Enter Security Pin: ");
        String pin = scanner.nextLine();

        String selectQuery = "SELECT balance FROM Accounts WHERE account_number = ? AND security_pin = ?";
        String debitQuery = "UPDATE Accounts SET balance = balance - ? WHERE account_number = ?";
        String creditQuery = "UPDATE Accounts SET balance = balance + ? WHERE account_number = ?";

        try {
            connection.setAutoCommit(false);

            BigDecimal balance;

            try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
                selectStmt.setLong(1, senderAccount);
                selectStmt.setString(2, pin);
                ResultSet rs = selectStmt.executeQuery();

                if (!rs.next()) {
                    System.out.println("Invalid Security Pin!");
                    return;
                }
                balance = rs.getBigDecimal("balance");
            }

            if (amount.compareTo(balance) > 0) {
                System.out.println("Insufficient Balance!");
                return;
            }

            try (
                    PreparedStatement debitStmt = connection.prepareStatement(debitQuery);
                    PreparedStatement creditStmt = connection.prepareStatement(creditQuery)
            ) {
                debitStmt.setBigDecimal(1, amount);
                debitStmt.setLong(2, senderAccount);

                creditStmt.setBigDecimal(1, amount);
                creditStmt.setLong(2, receiverAccount);

                debitStmt.executeUpdate();
                creditStmt.executeUpdate();

                connection.commit();
                System.out.println("Rs." + amount + " transferred successfully");
            }

        } catch (SQLException e) {
            rollbackQuietly();
            e.printStackTrace();
        } finally {
            resetAutoCommit();
        }
    }

    /* ---------------- CHECK BALANCE ---------------- */
    public void getBalance(long accountNumber) {

        System.out.print("Enter Security Pin: ");
        String pin = scanner.nextLine();

        String query = "SELECT balance FROM Accounts WHERE account_number = ? AND security_pin = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, accountNumber);
            stmt.setString(2, pin);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("Balance: Rs." + rs.getBigDecimal("balance"));
            } else {
                System.out.println("Invalid Security Pin!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* ---------------- HELPERS ---------------- */
    private void rollbackQuietly() {
        try {
            connection.rollback();
        } catch (SQLException ignored) {}
    }

    private void resetAutoCommit() {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException ignored) {}
    }
}
