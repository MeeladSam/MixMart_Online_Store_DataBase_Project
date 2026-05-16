package com.mycompany.laptopstoregui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CustomerDAO {

    public boolean loginCustomer(String phoneNumber, String password) {
    String sql = """
        SELECT *
        FROM Customer
        WHERE phone_number = ? AND customer_password = ?
    """;

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {

        pst.setString(1, phoneNumber);
        pst.setString(2, password);

        try (ResultSet rs = pst.executeQuery()) {
            return rs.next();
        }

    } catch (Exception e) {
        System.out.println("Login failed");
        e.printStackTrace();
        return false;
    }
}

    public boolean registerCustomer(String phoneNumber, String name, String city, String password) {
    String insertCustomerSql = """
        INSERT INTO Customer (phone_number, Name, City, customer_password)
        VALUES (?, ?, ?, ?)
    """;

    String nextCartIdSql = "SELECT COALESCE(MAX(cart_id), 2000) + 1 AS next_id FROM ShoppingCart";

    String insertCartSql = """
        INSERT INTO ShoppingCart (cart_id, customer_phone, cart_status)
        VALUES (?, ?, 'Active')
    """;

    try (Connection conn = DBConnection.getConnection()) {

        conn.setAutoCommit(false);

        try (
            PreparedStatement customerStmt = conn.prepareStatement(insertCustomerSql);
            PreparedStatement nextCartStmt = conn.prepareStatement(nextCartIdSql);
            PreparedStatement cartStmt = conn.prepareStatement(insertCartSql)
        ) {
            customerStmt.setString(1, phoneNumber);
            customerStmt.setString(2, name);
            customerStmt.setString(3, city);
            customerStmt.setString(4, password);
            customerStmt.executeUpdate();

            int nextCartId = 2001;

            try (ResultSet rs = nextCartStmt.executeQuery()) {
                if (rs.next()) {
                    nextCartId = rs.getInt("next_id");
                }
            }

            cartStmt.setInt(1, nextCartId);
            cartStmt.setString(2, phoneNumber);
            cartStmt.executeUpdate();

            conn.commit();
            return true;
        } catch (Exception e) {
            conn.rollback();
            System.out.println("Registration failed");
            e.printStackTrace();
            return false;
        } finally {
            conn.setAutoCommit(true);
        }

    } catch (Exception e) {
        System.out.println("Database error during registration");
        e.printStackTrace();
        return false;
    }
}
    
public String getCustomerNameByPhone(String phone) {
    String sql = "SELECT Name FROM Customer WHERE phone_number = ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {

        pst.setString(1, phone);

        try (ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getString("Name");
            }
        }

    } catch (Exception e) {
        System.out.println("Failed to get customer name");
        e.printStackTrace();
    }

    return phone;
}
    
    
}