package com.mycompany.laptopstoregui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {

    public int getCartIdByCustomerPhone(String customerPhone) {
        String sql = "SELECT cart_id FROM ShoppingCart WHERE customer_phone = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, customerPhone);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cart_id");
                }
            }

        } catch (Exception e) {
            System.out.println("Failed to get cart id");
            e.printStackTrace();
        }

        return -1;
    }

    private int getNextCartItemId(Connection conn) throws Exception {
        String sql = "SELECT COALESCE(MAX(cart_item_id), 3000) + 1 AS next_id FROM ShoppingCartItem";

        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("next_id");
            }
        }

        return 3001;
    }

    public boolean addToCart(String customerPhone, int productId) {
        String productSql = """
            SELECT stock_quantity
            FROM Product
            WHERE product_id = ?
              AND availability_status <> 'Hidden'
        """;

        String checkSql = """
            SELECT cart_item_id, quantity
            FROM ShoppingCartItem
            WHERE cart_id = ? AND product_id = ?
        """;

        String updateSql = """
            UPDATE ShoppingCartItem
            SET quantity = quantity + 1
            WHERE cart_item_id = ?
        """;

        String insertSql = """
            INSERT INTO ShoppingCartItem
            (cart_item_id, cart_id, product_id, quantity)
            VALUES (?, ?, ?, 1)
        """;

        try (Connection conn = DBConnection.getConnection()) {
            int cartId = getCartIdByCustomerPhone(customerPhone);

            if (cartId == -1) {
                System.out.println("No cart found for customer: " + customerPhone);
                return false;
            }

            int stockQuantity;

            try (PreparedStatement productStmt = conn.prepareStatement(productSql)) {
                productStmt.setInt(1, productId);

                try (ResultSet rs = productStmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Product is not available");
                        return false;
                    }

                    stockQuantity = rs.getInt("stock_quantity");

                    if (stockQuantity <= 0) {
                        System.out.println("Product is out of stock");
                        return false;
                    }
                }
            }

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, cartId);
                checkStmt.setInt(2, productId);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        int cartItemId = rs.getInt("cart_item_id");
                        int currentQuantity = rs.getInt("quantity");

                        if (currentQuantity + 1 > stockQuantity) {
                            System.out.println("Cannot add more than available stock");
                            return false;
                        }

                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setInt(1, cartItemId);
                            return updateStmt.executeUpdate() > 0;
                        }

                    } else {
                        int nextCartItemId = getNextCartItemId(conn);

                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setInt(1, nextCartItemId);
                            insertStmt.setInt(2, cartId);
                            insertStmt.setInt(3, productId);
                            return insertStmt.executeUpdate() > 0;
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Failed to add product to cart");
            e.printStackTrace();
            return false;
        }
    }

    public List<LaptopStoreGUI.CartItem> getCartItems(String customerPhone) {
        List<LaptopStoreGUI.CartItem> items = new ArrayList<>();

        String sql = """
            SELECT 
                p.product_id,
                p.product_name,
                p.description,
                p.price,
                p.discount,
                p.image_url,
                p.stock_quantity,
                c.category_name,
                sci.quantity
            FROM ShoppingCart sc
            JOIN ShoppingCartItem sci ON sc.cart_id = sci.cart_id
            JOIN Product p ON sci.product_id = p.product_id
            JOIN Category c ON p.category_id = c.category_id
            WHERE sc.customer_phone = ?
              AND p.availability_status <> 'Hidden'
            ORDER BY sci.cart_item_id DESC
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

                    LaptopStoreGUI.CartItem cartItem =
                            new LaptopStoreGUI.CartItem(product, rs.getInt("quantity"));

                    items.add(cartItem);
                }
            }

        } catch (Exception e) {
            System.out.println("Failed to load cart items");
            e.printStackTrace();
        }

        return items;
    }

    public boolean removeFromCart(String customerPhone, int productId) {
        String sql = """
            DELETE sci
            FROM ShoppingCartItem sci
            JOIN ShoppingCart sc ON sci.cart_id = sc.cart_id
            WHERE sc.customer_phone = ?
              AND sci.product_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, customerPhone);
            pst.setInt(2, productId);

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Failed to remove product from cart");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCartItemQuantity(String customerPhone, int productId, int newQuantity) {
        if (newQuantity <= 0) {
            return removeFromCart(customerPhone, productId);
        }

        String stockSql = """
            SELECT stock_quantity
            FROM Product
            WHERE product_id = ?
              AND availability_status <> 'Hidden'
        """;

        String updateSql = """
            UPDATE ShoppingCartItem sci
            JOIN ShoppingCart sc ON sci.cart_id = sc.cart_id
            SET sci.quantity = ?
            WHERE sc.customer_phone = ?
              AND sci.product_id = ?
        """;

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement stockStmt = conn.prepareStatement(stockSql)) {
                stockStmt.setInt(1, productId);

                try (ResultSet rs = stockStmt.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }

                    int stockQuantity = rs.getInt("stock_quantity");

                    if (newQuantity > stockQuantity) {
                        return false;
                    }
                }
            }

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, newQuantity);
                updateStmt.setString(2, customerPhone);
                updateStmt.setInt(3, productId);

                return updateStmt.executeUpdate() > 0;
            }

        } catch (Exception e) {
            System.out.println("Failed to update cart item quantity");
            e.printStackTrace();
            return false;
        }
    }
}