package com.mycompany.laptopstoregui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminStatsDAO {

    private double getDoubleValue(String sql) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (Exception e) {
            System.out.println("Failed to load double stat");
            e.printStackTrace();
        }

        return 0;
    }

    private int getIntValue(String sql) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            System.out.println("Failed to load int stat");
            e.printStackTrace();
        }

        return 0;
    }

    public String getStatisticsSummary() {
        double deliveredRevenue = getDoubleValue("""
            SELECT COALESCE(SUM(total_amount), 0)
            FROM Orders
            WHERE order_status = 'Delivered'
        """);

        double paidCardRevenue = getDoubleValue("""
            SELECT COALESCE(SUM(amount), 0)
            FROM Payment
            WHERE payment_status = 'Paid'
        """);

        double pendingAmount = getDoubleValue("""
            SELECT COALESCE(SUM(total_amount), 0)
            FROM Orders
            WHERE order_status = 'Pending'
        """);

        int totalOrders = getIntValue("""
            SELECT COUNT(*)
            FROM Orders
        """);

        int pendingOrders = getIntValue("""
            SELECT COUNT(*)
            FROM Orders
            WHERE order_status = 'Pending'
        """);

        int shippedOrders = getIntValue("""
            SELECT COUNT(*)
            FROM Orders
            WHERE order_status = 'Shipped'
        """);

        int deliveredOrders = getIntValue("""
            SELECT COUNT(*)
            FROM Orders
            WHERE order_status = 'Delivered'
        """);

        int totalProducts = getIntValue("""
            SELECT COUNT(*)
            FROM Product
            WHERE availability_status = 'Available'
        """);

        int outOfStockProducts = getIntValue("""
            SELECT COUNT(*)
            FROM Product
            WHERE availability_status = 'Available'
              AND stock_quantity = 0
        """);

        int lowStockProducts = getIntValue("""
            SELECT COUNT(*)
            FROM Product
            WHERE availability_status = 'Available'
              AND stock_quantity > 0
              AND stock_quantity <= 3
        """);

        int totalCustomers = getIntValue("""
            SELECT COUNT(*)
            FROM Customer
        """);

        int pendingSellingRequests = getIntValue("""
            SELECT COUNT(*)
            FROM SellingRequest
            WHERE request_status = 'Pending'
        """);

        int approvedSellingRequests = getIntValue("""
            SELECT COUNT(*)
            FROM SellingRequest
            WHERE request_status = 'Approved'
        """);

        int rejectedSellingRequests = getIntValue("""
            SELECT COUNT(*)
            FROM SellingRequest
            WHERE request_status = 'Rejected'
        """);

        double websiteAverageRating = getDoubleValue("""
            SELECT COALESCE(AVG(rating), 0)
            FROM Review
            WHERE review_type = 'Website'
        """);

        int websiteReviewsCount = getIntValue("""
            SELECT COUNT(*)
            FROM Review
            WHERE review_type = 'Website'
        """);

        double productAverageRating = getDoubleValue("""
            SELECT COALESCE(AVG(rating), 0)
            FROM Review
            WHERE review_type = 'Product'
        """);

        int productReviewsCount = getIntValue("""
            SELECT COUNT(*)
            FROM Review
            WHERE review_type = 'Product'
        """);
        
        int discountedProducts = getIntValue("""
    SELECT COUNT(*)
    FROM Product
    WHERE availability_status = 'Available'
      AND discount = 1
""");

double currentInventoryValue = getDoubleValue("""
    SELECT COALESCE(SUM(price * stock_quantity), 0)
    FROM Product
    WHERE availability_status = 'Available'
""");

        StringBuilder sb = new StringBuilder();

        sb.append("========= Money / Revenue =========\n");
        sb.append("Delivered Orders Revenue: $").append(String.format("%.2f", deliveredRevenue)).append("\n");
        sb.append("Paid Card Revenue: $").append(String.format("%.2f", paidCardRevenue)).append("\n");
        sb.append("Pending Orders Amount: $").append(String.format("%.2f", pendingAmount)).append("\n\n");

        sb.append("========= Orders =========\n");
        sb.append("Total Orders: ").append(totalOrders).append("\n");
        sb.append("Pending Orders: ").append(pendingOrders).append("\n");
        sb.append("Shipped Orders: ").append(shippedOrders).append("\n");
        sb.append("Delivered Orders: ").append(deliveredOrders).append("\n\n");

sb.append("========= Products =========\n");
sb.append("Available Products: ").append(totalProducts).append("\n");
sb.append("Discounted Products: ").append(discountedProducts).append("\n");
sb.append("Low Stock Products (1-3): ").append(lowStockProducts).append("\n");
sb.append("Out of Stock Products: ").append(outOfStockProducts).append("\n");
sb.append("Current Inventory Value: $").append(String.format("%.2f", currentInventoryValue)).append("\n\n");

        sb.append("========= Customers =========\n");
        sb.append("Total Customers: ").append(totalCustomers).append("\n\n");

        sb.append("========= Selling Requests =========\n");
        sb.append("Pending Requests: ").append(pendingSellingRequests).append("\n");
        sb.append("Approved Requests: ").append(approvedSellingRequests).append("\n");
        sb.append("Rejected Requests: ").append(rejectedSellingRequests).append("\n\n");

        sb.append("========= Reviews =========\n");
        sb.append("Website Average Rating: ").append(String.format("%.1f", websiteAverageRating)).append(" / 5\n");
        sb.append("Website Reviews Count: ").append(websiteReviewsCount).append("\n");
        sb.append("Product Average Rating: ").append(String.format("%.1f", productAverageRating)).append(" / 5\n");
        sb.append("Product Reviews Count: ").append(productReviewsCount).append("\n");

        return sb.toString();
    }
}