package com.mycompany.laptopstoregui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SellingRequestDAO {

    private int getNextRequestId(Connection conn) throws Exception {
        String sql = "SELECT COALESCE(MAX(request_id), 10000) + 1 AS next_id FROM SellingRequest";

        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("next_id");
            }
        }

        return 10001;
    }

    private int getNextProductId(Connection conn) throws Exception {
        String sql = "SELECT COALESCE(MAX(product_id), 1000) + 1 AS next_id FROM Product";

        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("next_id");
            }
        }

        return 1001;
    }

    public boolean submitSellingRequest(String customerPhone,
                                        String productName,
                                        String description,
                                        double price,
                                        String imageUrl) {

        String sql = """
            INSERT INTO SellingRequest
            (request_id, customer_phone, category_id, product_name, description,
             price, image_url, request_status)
            VALUES (?, ?, 4, ?, ?, ?, ?, 'Pending')
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            int requestId = getNextRequestId(conn);

            pst.setInt(1, requestId);
            pst.setString(2, customerPhone);
            pst.setString(3, productName);
            pst.setString(4, description);
            pst.setDouble(5, price);
            pst.setString(6, imageUrl);

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Failed to submit selling request");
            e.printStackTrace();
            return false;
        }
    }

    public List<SellingRequestInfo> getRequestsByCustomerPhone(String customerPhone) {
        List<SellingRequestInfo> requests = new ArrayList<>();

        String sql = """
            SELECT request_id, customer_phone, product_name, description, price,
                   image_url, request_status, submitted_at
            FROM SellingRequest
            WHERE customer_phone = ?
            ORDER BY request_id DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, customerPhone);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    requests.add(new SellingRequestInfo(
                            rs.getInt("request_id"),
                            rs.getString("customer_phone"),
                            rs.getString("product_name"),
                            rs.getDouble("price"),
                            rs.getString("image_url"),
                            rs.getString("request_status"),
                            String.valueOf(rs.getTimestamp("submitted_at")),
                            rs.getString("description")
                    ));
                }
            }

        } catch (Exception e) {
            System.out.println("Failed to load selling requests");
            e.printStackTrace();
        }

        return requests;
    }

    public List<SellingRequestInfo> getAllPendingRequests() {
        List<SellingRequestInfo> requests = new ArrayList<>();

        String sql = """
            SELECT request_id, customer_phone, product_name, description, price,
                   image_url, request_status, submitted_at
            FROM SellingRequest
            WHERE request_status = 'Pending'
            ORDER BY request_id DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                requests.add(new SellingRequestInfo(
                        rs.getInt("request_id"),
                        rs.getString("customer_phone"),
                        rs.getString("product_name"),
                        rs.getDouble("price"),
                        rs.getString("image_url"),
                        rs.getString("request_status"),
                        String.valueOf(rs.getTimestamp("submitted_at")),
                        rs.getString("description")
                ));
            }

        } catch (Exception e) {
            System.out.println("Failed to load pending selling requests");
            e.printStackTrace();
        }

        return requests;
    }

    public boolean acceptRequest(SellingRequestInfo request) {
        String insertProductSql = """
            INSERT INTO Product
            (product_id, product_name, description, price, image_url, discount,
             availability_status, category_id, stock_quantity)
            VALUES (?, ?, ?, ?, ?, 0, 'Available', 4, 1)
        """;

        String updateRequestSql = """
            UPDATE SellingRequest
            SET request_status = 'Approved'
            WHERE request_id = ?
              AND request_status = 'Pending'
        """;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement productStmt = conn.prepareStatement(insertProductSql);
                 PreparedStatement requestStmt = conn.prepareStatement(updateRequestSql)) {

                int productId = getNextProductId(conn);

                String description = request.description;

                if (description == null || description.trim().isEmpty()) {
                    description = "Used product submitted by customer " + request.customerPhone;
                }

                productStmt.setInt(1, productId);
                productStmt.setString(2, request.productName);
                productStmt.setString(3, description);
                productStmt.setDouble(4, request.price);
                productStmt.setString(5, request.imageUrl);
                productStmt.executeUpdate();

                requestStmt.setInt(1, request.requestId);
                int updatedRows = requestStmt.executeUpdate();

                if (updatedRows == 0) {
                    conn.rollback();
                    return false;
                }

                conn.commit();
                return true;

            } catch (Exception e) {
                conn.rollback();
                System.out.println("Failed to accept selling request");
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception e) {
            System.out.println("Database error while accepting selling request");
            e.printStackTrace();
            return false;
        }
    }

    public boolean rejectRequest(int requestId) {
        String sql = """
            UPDATE SellingRequest
            SET request_status = 'Rejected'
            WHERE request_id = ?
              AND request_status = 'Pending'
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, requestId);
            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Failed to reject selling request");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePendingRequest(int requestId,
                                        String customerPhone,
                                        String productName,
                                        String description,
                                        double price,
                                        String imageUrl) {

        String sql = """
            UPDATE SellingRequest
            SET product_name = ?,
                description = ?,
                price = ?,
                image_url = ?
            WHERE request_id = ?
              AND customer_phone = ?
              AND request_status = 'Pending'
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, productName);
            pst.setString(2, description);
            pst.setDouble(3, price);
            pst.setString(4, imageUrl);
            pst.setInt(5, requestId);
            pst.setString(6, customerPhone);

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Failed to update pending selling request");
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletePendingRequest(int requestId, String customerPhone) {
        String sql = """
            DELETE FROM SellingRequest
            WHERE request_id = ?
              AND customer_phone = ?
              AND request_status = 'Pending'
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, requestId);
            pst.setString(2, customerPhone);

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Failed to delete pending selling request");
            e.printStackTrace();
            return false;
        }
    }
}