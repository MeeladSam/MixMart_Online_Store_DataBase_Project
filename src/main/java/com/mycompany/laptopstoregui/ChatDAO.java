package com.mycompany.laptopstoregui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ChatDAO {

    public boolean sendMessage(String customerPhone, String senderType, String messageText) {
        String sql = """
            INSERT INTO ChatMessage (customer_phone, sender_type, message_text)
            VALUES (?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, customerPhone);
            pst.setString(2, senderType);
            pst.setString(3, messageText);

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Failed to send chat message");
            e.printStackTrace();
            return false;
        }
    }

    public List<ChatMessageInfo> getMessagesByCustomer(String customerPhone) {
        List<ChatMessageInfo> messages = new ArrayList<>();

        String sql = """
            SELECT customer_phone, sender_type, message_text, sent_at
            FROM ChatMessage
            WHERE customer_phone = ?
            ORDER BY sent_at ASC, message_id ASC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, customerPhone);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    messages.add(new ChatMessageInfo(
                            rs.getString("customer_phone"),
                            rs.getString("sender_type"),
                            rs.getString("message_text"),
                            String.valueOf(rs.getTimestamp("sent_at"))
                    ));
                }
            }

        } catch (Exception e) {
            System.out.println("Failed to load chat messages");
            e.printStackTrace();
        }

        return messages;
    }

    public List<String> getCustomersWithMessages() {
        List<String> customers = new ArrayList<>();

        String sql = """
            SELECT DISTINCT customer_phone
            FROM ChatMessage
            ORDER BY customer_phone
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                customers.add(rs.getString("customer_phone"));
            }

        } catch (Exception e) {
            System.out.println("Failed to load chat customers");
            e.printStackTrace();
        }

        return customers;
    }
    
    
    
    public List<ChatMessageInfo> getAllMessages() {
    List<ChatMessageInfo> messages = new ArrayList<>();

String sql = """
    SELECT customer_phone, sender_type, message_text, sent_at
    FROM ChatMessage
    ORDER BY sent_at ASC, message_id ASC
""";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        while (rs.next()) {
            messages.add(new ChatMessageInfo(
                    rs.getString("customer_phone"),
                    rs.getString("sender_type"),
                    rs.getString("message_text"),
                    String.valueOf(rs.getTimestamp("sent_at"))
            ));
        }

    } catch (Exception e) {
        System.out.println("Failed to load all chat messages");
        e.printStackTrace();
    }

    return messages;
}
    
    
    public List<String> getAllCustomers() {
    List<String> customers = new ArrayList<>();

    String sql = """
        SELECT phone_number
        FROM Customer
        ORDER BY phone_number
    """;

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        while (rs.next()) {
            customers.add(rs.getString("phone_number"));
        }

    } catch (Exception e) {
        System.out.println("Failed to load all customers");
        e.printStackTrace();
    }

    return customers;
}
    
    public boolean sendMessageToAllCustomers(String messageText) {
    String customersSql = """
        SELECT phone_number
        FROM Customer
        ORDER BY phone_number
    """;

    String insertSql = """
        INSERT INTO ChatMessage (customer_phone, sender_type, message_text)
        VALUES (?, 'Admin', ?)
    """;

    try (Connection conn = DBConnection.getConnection()) {
        conn.setAutoCommit(false);

        try (
            PreparedStatement customersStmt = conn.prepareStatement(customersSql);
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            ResultSet rs = customersStmt.executeQuery()
        ) {
            int count = 0;

            while (rs.next()) {
                String customerPhone = rs.getString("phone_number");

                insertStmt.setString(1, customerPhone);
insertStmt.setString(2, "[ALL] " + messageText);
insertStmt.addBatch();

                count++;
            }

            if (count == 0) {
                conn.rollback();
                return false;
            }

            insertStmt.executeBatch();
            conn.commit();
            return true;

        } catch (Exception e) {
            conn.rollback();
            System.out.println("Failed to send message to all customers");
            e.printStackTrace();
            return false;
        } finally {
            conn.setAutoCommit(true);
        }

    } catch (Exception e) {
        System.out.println("Database error while sending message to all customers");
        e.printStackTrace();
        return false;
    }
}
    
    
}