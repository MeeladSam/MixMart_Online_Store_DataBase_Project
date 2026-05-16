package com.mycompany.laptopstoregui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<LaptopStoreGUI.Product> getAllProducts() {
        List<LaptopStoreGUI.Product> products = new ArrayList<>();

        String sql = """
            SELECT 
                p.product_id,
                p.product_name,
                p.description,
                p.price,
                p.discount,
                p.image_url,
                p.stock_quantity,
                c.category_name
            FROM Product p
            JOIN Category c ON p.category_id = c.category_id
            WHERE p.availability_status IN ('Available', 'OutOfStock')
            ORDER BY p.product_id
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                LaptopStoreGUI.Product product =
                        new LaptopStoreGUI.Product(
                                rs.getInt("product_id"),
                                rs.getString("product_name"),
                                rs.getString("category_name"),
                                rs.getDouble("price"),
                                rs.getBoolean("discount"),
                                4.5
                        );

                product.description = rs.getString("description");
                product.imagePath = rs.getString("image_url");
                product.stockQuantity = rs.getInt("stock_quantity");

                products.add(product);
            }

        } catch (Exception e) {
            System.out.println("Failed to load products from database");
            e.printStackTrace();
        }

        return products;
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

    public boolean addProduct(String productName,
                              String description,
                              double price,
                              boolean discount,
                              int categoryId,
                              String imageUrl,
                              int stockQuantity) {

        String availabilityStatus = stockQuantity > 0 ? "Available" : "OutOfStock";

        String sql = """
            INSERT INTO Product
            (product_id, product_name, description, price, image_url, discount,
             availability_status, category_id, stock_quantity)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            int productId = getNextProductId(conn);

            pst.setInt(1, productId);
            pst.setString(2, productName);
            pst.setString(3, description);
            pst.setDouble(4, price);
            pst.setString(5, imageUrl);
            pst.setBoolean(6, discount);
            pst.setString(7, availabilityStatus);
            pst.setInt(8, categoryId);
            pst.setInt(9, stockQuantity);

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Failed to add product");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateProduct(int productId,
                                 String productName,
                                 String description,
                                 double price,
                                 boolean discount,
                                 String imageUrl,
                                 int stockQuantity) {

        String availabilityStatus = stockQuantity > 0 ? "Available" : "OutOfStock";

        String sql = """
            UPDATE Product
            SET product_name = ?,
                description = ?,
                price = ?,
                discount = ?,
                image_url = ?,
                stock_quantity = ?,
                availability_status = ?
            WHERE product_id = ?
              AND availability_status <> 'Hidden'
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, productName);
            pst.setString(2, description);
            pst.setDouble(3, price);
            pst.setBoolean(4, discount);
            pst.setString(5, imageUrl);
            pst.setInt(6, stockQuantity);
            pst.setString(7, availabilityStatus);
            pst.setInt(8, productId);

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Failed to update product");
            e.printStackTrace();
            return false;
        }
    }

    public LaptopStoreGUI.Product getProductById(int productId) {
        String sql = """
            SELECT 
                p.product_id,
                p.product_name,
                p.description,
                p.price,
                p.discount,
                p.image_url,
                p.stock_quantity,
                c.category_name
            FROM Product p
            JOIN Category c ON p.category_id = c.category_id
            WHERE p.product_id = ?
              AND p.availability_status <> 'Hidden'
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, productId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    LaptopStoreGUI.Product product =
                            new LaptopStoreGUI.Product(
                                    rs.getInt("product_id"),
                                    rs.getString("product_name"),
                                    rs.getString("category_name"),
                                    rs.getDouble("price"),
                                    rs.getBoolean("discount"),
                                    4.5
                            );

                    product.description = rs.getString("description");
                    product.imagePath = rs.getString("image_url");
                    product.stockQuantity = rs.getInt("stock_quantity");

                    return product;
                }
            }

        } catch (Exception e) {
            System.out.println("Failed to get product by id");
            e.printStackTrace();
        }

        return null;
    }

    public boolean deleteProductById(int productId) {
        String deleteWishlistSql = """
            DELETE FROM Wishlist
            WHERE product_id = ?
        """;

        String deleteCartSql = """
            DELETE FROM ShoppingCartItem
            WHERE product_id = ?
        """;

        String deletePendingOrderItemSql = """
            DELETE oi
            FROM OrderItem oi
            JOIN Orders o ON oi.order_id = o.order_id
            WHERE oi.product_id = ?
              AND o.order_status = 'Pending'
        """;

        String updatePendingOrderTotalsSql = """
            UPDATE Orders o
            SET total_amount = COALESCE((
                SELECT SUM(oi.quantity * oi.unit_price)
                FROM OrderItem oi
                WHERE oi.order_id = o.order_id
            ), 0)
            WHERE o.order_status = 'Pending'
        """;

        String updatePaymentTotalsSql = """
            UPDATE Payment p
            JOIN Orders o ON p.order_id = o.order_id
            SET p.amount = o.total_amount
            WHERE o.order_status = 'Pending'
        """;

        String deleteEmptyPendingPaymentsSql = """
            DELETE p
            FROM Payment p
            JOIN Orders o ON p.order_id = o.order_id
            WHERE o.order_status = 'Pending'
              AND o.total_amount = 0
        """;

        String deleteEmptyPendingOrdersSql = """
            DELETE FROM Orders
            WHERE order_status = 'Pending'
              AND total_amount = 0
        """;

        String hideProductSql = """
            UPDATE Product
            SET availability_status = 'Hidden'
            WHERE product_id = ?
        """;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (
                    PreparedStatement wishlistStmt = conn.prepareStatement(deleteWishlistSql);
                    PreparedStatement cartStmt = conn.prepareStatement(deleteCartSql);
                    PreparedStatement pendingItemStmt = conn.prepareStatement(deletePendingOrderItemSql);
                    PreparedStatement orderTotalsStmt = conn.prepareStatement(updatePendingOrderTotalsSql);
                    PreparedStatement paymentTotalsStmt = conn.prepareStatement(updatePaymentTotalsSql);
                    PreparedStatement emptyPaymentsStmt = conn.prepareStatement(deleteEmptyPendingPaymentsSql);
                    PreparedStatement emptyOrdersStmt = conn.prepareStatement(deleteEmptyPendingOrdersSql);
                    PreparedStatement hideProductStmt = conn.prepareStatement(hideProductSql)
            ) {
                wishlistStmt.setInt(1, productId);
                wishlistStmt.executeUpdate();

                cartStmt.setInt(1, productId);
                cartStmt.executeUpdate();

                pendingItemStmt.setInt(1, productId);
                pendingItemStmt.executeUpdate();

                orderTotalsStmt.executeUpdate();
                paymentTotalsStmt.executeUpdate();

                emptyPaymentsStmt.executeUpdate();
                emptyOrdersStmt.executeUpdate();

                hideProductStmt.setInt(1, productId);
                int hiddenRows = hideProductStmt.executeUpdate();

                conn.commit();
                return hiddenRows > 0;

            } catch (Exception e) {
                conn.rollback();
                System.out.println("Failed to delete/hide product");
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception e) {
            System.out.println("Database error while deleting/hiding product");
            e.printStackTrace();
            return false;
        }
    }
}