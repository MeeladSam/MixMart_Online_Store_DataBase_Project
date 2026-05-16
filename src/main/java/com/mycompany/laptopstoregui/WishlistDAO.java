package com.mycompany.laptopstoregui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class WishlistDAO {

    private int getNextWishlistId(Connection conn) throws Exception {
        String sql = "SELECT COALESCE(MAX(wishlist_id), 8000) + 1 AS next_id FROM Wishlist";

        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("next_id");
            }
        }

        return 8001;
    }

    public boolean addToWishlist(String customerPhone, int productId) {
        String productSql = """
            SELECT product_id
            FROM Product
            WHERE product_id = ?
              AND availability_status <> 'Hidden'
        """;

        String checkSql = """
            SELECT wishlist_id
            FROM Wishlist
            WHERE customer_phone = ?
              AND product_id = ?
        """;

        String insertSql = """
            INSERT INTO Wishlist
            (wishlist_id, customer_phone, product_id)
            VALUES (?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection()) {

            try (PreparedStatement productStmt = conn.prepareStatement(productSql)) {
                productStmt.setInt(1, productId);

                try (ResultSet rs = productStmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Product is not available for wishlist");
                        return false;
                    }
                }
            }

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, customerPhone);
                checkStmt.setInt(2, productId);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                int wishlistId = getNextWishlistId(conn);

                insertStmt.setInt(1, wishlistId);
                insertStmt.setString(2, customerPhone);
                insertStmt.setInt(3, productId);

                return insertStmt.executeUpdate() > 0;
            }

        } catch (Exception e) {
            System.out.println("Failed to add product to wishlist");
            e.printStackTrace();
            return false;
        }
    }

    public List<LaptopStoreGUI.Product> getWishlistProducts(String customerPhone) {
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
            FROM Wishlist w
            JOIN Product p ON w.product_id = p.product_id
            JOIN Category c ON p.category_id = c.category_id
            WHERE w.customer_phone = ?
              AND p.availability_status <> 'Hidden'
            ORDER BY w.wishlist_id DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, customerPhone);

            try (ResultSet rs = pst.executeQuery()) {
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
            }

        } catch (Exception e) {
            System.out.println("Failed to load wishlist products");
            e.printStackTrace();
        }

        return products;
    }

    public boolean removeFromWishlist(String customerPhone, int productId) {
        String sql = """
            DELETE FROM Wishlist
            WHERE customer_phone = ?
              AND product_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, customerPhone);
            pst.setInt(2, productId);

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Failed to remove product from wishlist");
            e.printStackTrace();
            return false;
        }
    }
}