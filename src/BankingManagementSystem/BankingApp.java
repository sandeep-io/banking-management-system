package BankingManagementSystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class BankingApp {

    private static final String URL = "jdbc:mysql://localhost:3306/banking_system";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Sandy@04";

    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL Driver not found!");
            return;
        }

        try (
                Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                Scanner scanner = new Scanner(System.in)
        ) {

            User user = new User(connection, scanner);
            Accounts accounts = new Accounts(connection, scanner);
            AccountManager accountManager = new AccountManager(connection, scanner);

            while (true) {
                System.out.println("\n*** WELCOME TO BANKING SYSTEM ***");
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("Enter your choice: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // clear buffer

                switch (choice) {

                    case 1 -> user.register();

                    case 2 -> handleLogin(scanner, user, accounts, accountManager);

                    case 3 -> {
                        System.out.println("THANK YOU FOR USING BANKING SYSTEM!");
                        return;
                    }

                    default -> System.out.println("Invalid choice!");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* ---------------- LOGIN FLOW ---------------- */
    private static void handleLogin(
            Scanner scanner,
            User user,
            Accounts accounts,
            AccountManager accountManager
    ) {

        String email = user.login();
        if (email == null) {
            System.out.println("Incorrect Email or Password!");
            return;
        }

        System.out.println("User Logged In!");

        long accountNumber;

        if (!accounts.accountExists(email)) {
            System.out.println("\n1. Open Bank Account");
            System.out.println("2. Logout");
            System.out.print("Choice: ");

            int ch = scanner.nextInt();
            scanner.nextLine();

            if (ch != 1) return;

            accountNumber = accounts.openAccount(email);
            System.out.println("Account Created Successfully!");
            System.out.println("Your Account Number: " + accountNumber);
        } else {
            accountNumber = accounts.getAccountNumber(email);
        }

        accountMenu(scanner, accountManager, accountNumber);
    }

    /* ---------------- ACCOUNT MENU ---------------- */
    private static void accountMenu(
            Scanner scanner,
            AccountManager accountManager,
            long accountNumber
    ) {

        int choice = 0;

        while (choice != 5) {
            System.out.println("\n----- ACCOUNT MENU -----");
            System.out.println("1. Debit Money");
            System.out.println("2. Credit Money");
            System.out.println("3. Transfer Money");
            System.out.println("4. Check Balance");
            System.out.println("5. Logout");
            System.out.print("Enter choice: ");

            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> accountManager.debitMoney(accountNumber);
                case 2 -> accountManager.creditMoney(accountNumber);
                case 3 -> accountManager.transferMoney(accountNumber);
                case 4 -> accountManager.getBalance(accountNumber);
                case 5 -> System.out.println("Logged out successfully");
                default -> System.out.println("Invalid choice!");
            }
        }
    }
}

