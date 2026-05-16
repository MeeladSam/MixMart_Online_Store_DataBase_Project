package com.mycompany.laptopstoregui;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    private int getNextReviewId(Connection conn) throws Exception {
        String sql = "SELECT COALESCE(MAX(review_id), 8000) + 1 AS next_id FROM Review";

        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("next_id");
            }
        }

        return 8001;
    }

    public boolean canReviewProduct(String customerPhone, int productId) {
        String sql = """
            SELECT 1
            FROM Orders o
            JOIN OrderItem oi ON o.order_id = oi.order_id
            WHERE o.customer_phone = ?
              AND oi.product_id = ?
              AND o.order_status = 'Delivered'
            LIMIT 1
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, customerPhone);
            pst.setInt(2, productId);

            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            System.out.println("Failed to check product review permission");
            e.printStackTrace();
            return false;
        }
    }

    public boolean canReviewWebsite(String customerPhone) {
        String sql = """
            SELECT 1
            FROM Orders
            WHERE customer_phone = ?
              AND order_status IN ('Shipped', 'Delivered')
            LIMIT 1
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, customerPhone);

            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            System.out.println("Failed to check website review permission");
            e.printStackTrace();
            return false;
        }
    }

    public boolean addOrUpdateProductReview(String customerPhone, int productId,
                                            int rating, String comment) {
        String findOrderSql = """
            SELECT o.order_id
            FROM Orders o
            JOIN OrderItem oi ON o.order_id = oi.order_id
            WHERE o.customer_phone = ?
              AND oi.product_id = ?
              AND o.order_status = 'Delivered'
            ORDER BY o.order_id DESC
            LIMIT 1
        """;

        String checkReviewSql = """
            SELECT review_id
            FROM Review
            WHERE customer_phone = ?
              AND product_id = ?
              AND review_type = 'Product'
        """;

        String updateSql = """
            UPDATE Review
            SET rating = ?, comment = ?, review_date = NOW()
            WHERE review_id = ?
        """;

        String insertSql = """
            INSERT INTO Review
            (review_id, customer_phone, product_id, order_id, rating, comment, review_type)
            VALUES (?, ?, ?, ?, ?, ?, 'Product')
        """;

        try (Connection conn = DBConnection.getConnection()) {

            int orderId = -1;

            try (PreparedStatement findOrderStmt = conn.prepareStatement(findOrderSql)) {
                findOrderStmt.setString(1, customerPhone);
                findOrderStmt.setInt(2, productId);

                try (ResultSet rs = findOrderStmt.executeQuery()) {
                    if (rs.next()) {
                        orderId = rs.getInt("order_id");
                    } else {
                        return false;
                    }
                }
            }

            try (PreparedStatement checkStmt = conn.prepareStatement(checkReviewSql)) {
                checkStmt.setString(1, customerPhone);
                checkStmt.setInt(2, productId);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        int reviewId = rs.getInt("review_id");

                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setInt(1, rating);
                            updateStmt.setString(2, comment);
                            updateStmt.setInt(3, reviewId);

                            return updateStmt.executeUpdate() > 0;
                        }
                    }
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                int reviewId = getNextReviewId(conn);

                insertStmt.setInt(1, reviewId);
                insertStmt.setString(2, customerPhone);
                insertStmt.setInt(3, productId);
                insertStmt.setInt(4, orderId);
                insertStmt.setInt(5, rating);
                insertStmt.setString(6, comment);

                return insertStmt.executeUpdate() > 0;
            }

        } catch (Exception e) {
            System.out.println("Failed to add/update product review");
            e.printStackTrace();
            return false;
        }
    }

    public boolean addOrUpdateWebsiteReview(String customerPhone, int rating, String comment) {
        String findOrderSql = """
            SELECT order_id
            FROM Orders
            WHERE customer_phone = ?
              AND order_status IN ('Shipped', 'Delivered')
            ORDER BY order_id DESC
            LIMIT 1
        """;

        String checkReviewSql = """
            SELECT review_id
            FROM Review
            WHERE customer_phone = ?
              AND review_type = 'Website'
        """;

        String updateSql = """
            UPDATE Review
            SET rating = ?, comment = ?, review_date = NOW()
            WHERE review_id = ?
        """;

        String insertSql = """
            INSERT INTO Review
            (review_id, customer_phone, product_id, order_id, rating, comment, review_type)
            VALUES (?, ?, NULL, ?, ?, ?, 'Website')
        """;

        try (Connection conn = DBConnection.getConnection()) {

            int orderId = -1;

            try (PreparedStatement findOrderStmt = conn.prepareStatement(findOrderSql)) {
                findOrderStmt.setString(1, customerPhone);

                try (ResultSet rs = findOrderStmt.executeQuery()) {
                    if (rs.next()) {
                        orderId = rs.getInt("order_id");
                    } else {
                        return false;
                    }
                }
            }

            try (PreparedStatement checkStmt = conn.prepareStatement(checkReviewSql)) {
                checkStmt.setString(1, customerPhone);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        int reviewId = rs.getInt("review_id");

                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setInt(1, rating);
                            updateStmt.setString(2, comment);
                            updateStmt.setInt(3, reviewId);

                            return updateStmt.executeUpdate() > 0;
                        }
                    }
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                int reviewId = getNextReviewId(conn);

                insertStmt.setInt(1, reviewId);
                insertStmt.setString(2, customerPhone);
                insertStmt.setInt(3, orderId);
                insertStmt.setInt(4, rating);
                insertStmt.setString(5, comment);

                return insertStmt.executeUpdate() > 0;
            }

        } catch (Exception e) {
            System.out.println("Failed to add/update website review");
            e.printStackTrace();
            return false;
        }
    }

    public List<ReviewInfo> getProductReviews(int productId) {
        List<ReviewInfo> reviews = new ArrayList<>();

        String sql = """
            SELECT review_id, customer_phone, product_id, order_id,
                   rating, comment, review_date, review_type
            FROM Review
            WHERE product_id = ?
              AND review_type = 'Product'
            ORDER BY review_date DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, productId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    reviews.add(new ReviewInfo(
                            rs.getInt("review_id"),
                            rs.getString("customer_phone"),
                            rs.getInt("product_id"),
                            rs.getInt("order_id"),
                            rs.getInt("rating"),
                            rs.getString("comment"),
                            String.valueOf(rs.getTimestamp("review_date")),
                            rs.getString("review_type")
                    ));
                }
            }

        } catch (Exception e) {
            System.out.println("Failed to load product reviews");
            e.printStackTrace();
        }

        return reviews;
    }
    
    public boolean hasWebsiteReview(String customerPhone) {
    String sql = """
        SELECT 1
        FROM Review
        WHERE customer_phone = ?
          AND review_type = 'Website'
        LIMIT 1
    """;

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {

        pst.setString(1, customerPhone);

        try (ResultSet rs = pst.executeQuery()) {
            return rs.next();
        }

    } catch (Exception e) {
        System.out.println("Failed to check website review");
        e.printStackTrace();
        return false;
    }
}
    public ReviewInfo getWebsiteReview(String customerPhone) {
    String sql = """
        SELECT review_id, customer_phone, product_id, order_id,
               rating, comment, review_date, review_type
        FROM Review
        WHERE customer_phone = ?
          AND review_type = 'Website'
        LIMIT 1
    """;

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {

        pst.setString(1, customerPhone);

        try (ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return new ReviewInfo(
                        rs.getInt("review_id"),
                        rs.getString("customer_phone"),
                        rs.getInt("product_id"),
                        rs.getInt("order_id"),
                        rs.getInt("rating"),
                        rs.getString("comment"),
                        String.valueOf(rs.getTimestamp("review_date")),
                        rs.getString("review_type")
                );
            }
        }

    } catch (Exception e) {
        System.out.println("Failed to load website review");
        e.printStackTrace();
    }

    return null;
}

public ReviewInfo getProductReview(String customerPhone, int productId) {
    String sql = """
        SELECT review_id, customer_phone, product_id, order_id,
               rating, comment, review_date, review_type
        FROM Review
        WHERE customer_phone = ?
          AND product_id = ?
          AND review_type = 'Product'
        LIMIT 1
    """;

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {

        pst.setString(1, customerPhone);
        pst.setInt(2, productId);

        try (ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return new ReviewInfo(
                        rs.getInt("review_id"),
                        rs.getString("customer_phone"),
                        rs.getInt("product_id"),
                        rs.getInt("order_id"),
                        rs.getInt("rating"),
                        rs.getString("comment"),
                        String.valueOf(rs.getTimestamp("review_date")),
                        rs.getString("review_type")
                );
            }
        }

    } catch (Exception e) {
        System.out.println("Failed to load product review");
        e.printStackTrace();
    }

    return null;
}

public Double getAverageProductRating(int productId) {
    String sql = """
        SELECT AVG(rating) AS avg_rating
        FROM Review
        WHERE product_id = ?
          AND review_type = 'Product'
    """;

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {

        pst.setInt(1, productId);

        try (ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                double avg = rs.getDouble("avg_rating");

                if (rs.wasNull()) {
                    return null;
                }

                return avg;
            }
        }

    } catch (Exception e) {
        System.out.println("Failed to calculate average product rating");
        e.printStackTrace();
    }

    return null;
}
    public List<String> getAllWebsiteReviewsForAdmin() {
    List<String> reviews = new ArrayList<>();

    String sql = """
        SELECT 
            customer_phone,
            rating,
            comment,
            review_date
        FROM Review
        WHERE review_type = 'Website'
        ORDER BY review_date DESC
    """;

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        while (rs.next()) {
            String comment = rs.getString("comment");

            if (comment == null || comment.trim().isEmpty()) {
                comment = "No comment";
            }

            String review =
                    "Customer Phone: " + rs.getString("customer_phone") + "\n"
                    + "Rating: " + rs.getInt("rating") + " / 5\n"
                    + "Comment: " + comment + "\n"
                    + "Date: " + rs.getTimestamp("review_date") + "\n"
                    + "----------------------------------\n";

            reviews.add(review);
        }

    } catch (Exception e) {
        System.out.println("Failed to load website reviews");
        e.printStackTrace();
    }

    return reviews;
}
    
    public List<String> getAllProductReviewsForAdmin() {
    List<String> reviews = new ArrayList<>();

    String sql = """
        SELECT 
            r.customer_phone,
            r.product_id,
            p.product_name,
            r.rating,
            r.comment,
            r.review_date
        FROM Review r
        JOIN Product p ON r.product_id = p.product_id
        WHERE r.review_type = 'Product'
        ORDER BY r.review_date DESC
    """;

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        while (rs.next()) {
            String comment = rs.getString("comment");

            if (comment == null || comment.trim().isEmpty()) {
                comment = "No comment";
            }

            String review =
                    "Product ID: " + rs.getInt("product_id") + "\n"
                    + "Product Name: " + rs.getString("product_name") + "\n"
                    + "Customer Phone: " + rs.getString("customer_phone") + "\n"
                    + "Rating: " + rs.getInt("rating") + " / 5\n"
                    + "Comment: " + comment + "\n"
                    + "Date: " + rs.getTimestamp("review_date") + "\n"
                    + "----------------------------------\n";

            reviews.add(review);
        }

    } catch (Exception e) {
        System.out.println("Failed to load product reviews");
        e.printStackTrace();
    }

    return reviews;
}
    
    
}