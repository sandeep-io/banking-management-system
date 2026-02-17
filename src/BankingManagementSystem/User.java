package BankingManagementSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class User {

    private final Connection connection;
    private final Scanner scanner;

    public User(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    /* ---------------- REGISTER USER ---------------- */
    public void register() {

        System.out.print("Full Name: ");
        String fullName = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            System.out.println("All fields are required!");
            return;
        }

        if (userExists(email)) {
            System.out.println("User already exists with this email!");
            return;
        }

        String query = "INSERT INTO user (full_name, email, password) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, password);

            stmt.executeUpdate();
            System.out.println("Registration successful!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* ---------------- LOGIN ---------------- */
    public String login() {

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (email.isEmpty() || password.isEmpty()) {
            System.out.println("Email and Password cannot be empty!");
            return null;
        }

        String query = "SELECT 1 FROM user WHERE email = ? AND password = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return email;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /* ---------------- CHECK USER EXISTS ---------------- */
    public boolean userExists(String email) {

        String query = "SELECT 1 FROM user WHERE email = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, email.trim());
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
