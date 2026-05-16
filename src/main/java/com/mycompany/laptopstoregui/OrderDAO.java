package com.mycompany.laptopstoregui;

import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import javax.swing.JOptionPane;

public class OrderDAO {

    private double getDiscountRate(LaptopStoreGUI.Product product) {
    if (!product.discount) {
        return 0.0;
    }

    if (product.price < 50) {
        return 0.1;   // 10%
    } else if (product.price <= 200) {
        return 0.20;   // 20%
    } else {
        return 0.3;   // 30%
    }
}

private double getFinalPrice(LaptopStoreGUI.Product product) {
    double discountRate = getDiscountRate(product);
    return product.price - (product.price * discountRate);
}
    
    private int getNextOrderId(Connection conn) throws Exception {
        String sql = "SELECT COALESCE(MAX(order_id), 4000) + 1 AS next_id FROM Orders";

        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("next_id");
            }
        }

        return 4001;
    }

    private int getNextOrderItemId(Connection conn) throws Exception {
        String sql = "SELECT COALESCE(MAX(order_item_id), 5000) + 1 AS next_id FROM OrderItem";

        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("next_id");
            }
        }

        return 5001;
    }

    private int getNextPaymentId(Connection conn) throws Exception {
        String sql = "SELECT COALESCE(MAX(payment_id), 6000) + 1 AS next_id FROM Payment";

        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("next_id");
            }
        }

        return 6001;
    }

    public boolean createOrder(String customerPhone,
                               String receiverName,
                               String receiverPhone,
                               String city,
                               String street,
                               String paymentMethod,
                               List<LaptopStoreGUI.CartItem> cartItems) {

        if (cartItems == null || cartItems.isEmpty()) {
            return false;
        }

        String insertOrderSql = """
            INSERT INTO Orders
            (order_id, customer_phone, total_amount, order_status, receiver_name, receiver_phone, city, street)
            VALUES (?, ?, ?, 'Pending', ?, ?, ?, ?)
        """;

        String insertOrderItemSql = """
            INSERT INTO OrderItem
            (order_item_id, order_id, product_id, quantity, unit_price)
            VALUES (?, ?, ?, ?, ?)
        """;

        String insertPaymentSql = """
            INSERT INTO Payment
            (payment_id, order_id, payment_method, payment_status, amount)
            VALUES (?, ?, ?, 'Pending', ?)
        """;

        String clearCartSql = """
            DELETE sci
            FROM ShoppingCartItem sci
            JOIN ShoppingCart sc ON sci.cart_id = sc.cart_id
            WHERE sc.customer_phone = ?
        """;
        
        String updateStockSql = """
    UPDATE Product
    SET stock_quantity = stock_quantity - ?
    WHERE product_id = ?
""";

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);
            
            String checkStockSql = """
    SELECT product_name, stock_quantity
    FROM Product
    WHERE product_id = ?
      AND availability_status = 'Available'
""";

try (PreparedStatement checkStockStmt = conn.prepareStatement(checkStockSql)) {
    for (LaptopStoreGUI.CartItem item : cartItems) {
        checkStockStmt.setInt(1, item.product.id);

        try (ResultSet rs = checkStockStmt.executeQuery()) {
            if (!rs.next()) {
                conn.rollback();
                JOptionPane.showMessageDialog(
                        null,
                        "Product is no longer available: " + item.product.name,
                        "Stock Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return false;
            }

            int stock = rs.getInt("stock_quantity");
            String productName = rs.getString("product_name");

            if (item.quantity > stock) {
                conn.rollback();
                JOptionPane.showMessageDialog(
                        null,
                        "Not enough stock for: " + productName
                                + "\nAvailable: " + stock
                                + "\nRequested: " + item.quantity,
                        "Stock Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return false;
            }
        }
    }
}
            
            

            try (
    PreparedStatement orderStmt = conn.prepareStatement(insertOrderSql);
    PreparedStatement orderItemStmt = conn.prepareStatement(insertOrderItemSql);
    PreparedStatement paymentStmt = conn.prepareStatement(insertPaymentSql);
    PreparedStatement clearCartStmt = conn.prepareStatement(clearCartSql);
    PreparedStatement updateStockStmt = conn.prepareStatement(updateStockSql)
            ) {
                int orderId = getNextOrderId(conn);
                int orderItemId = getNextOrderItemId(conn);
                int paymentId = getNextPaymentId(conn);

double total = 0;

for (LaptopStoreGUI.CartItem item : cartItems) {
    double finalPrice = getFinalPrice(item.product);
    total += finalPrice * item.quantity;
}

                orderStmt.setInt(1, orderId);
                orderStmt.setString(2, customerPhone);
                orderStmt.setDouble(3, total);
                orderStmt.setString(4, receiverName);
                orderStmt.setString(5, receiverPhone);
                orderStmt.setString(6, city);
                orderStmt.setString(7, street);
                orderStmt.executeUpdate();

for (LaptopStoreGUI.CartItem item : cartItems) {
    double finalPrice = getFinalPrice(item.product);

    orderItemStmt.setInt(1, orderItemId++);
    orderItemStmt.setInt(2, orderId);
    orderItemStmt.setInt(3, item.product.id);
    orderItemStmt.setInt(4, item.quantity);
    orderItemStmt.setDouble(5, finalPrice);
    orderItemStmt.addBatch();

    updateStockStmt.setInt(1, item.quantity);
    updateStockStmt.setInt(2, item.product.id);
    updateStockStmt.addBatch();
}

orderItemStmt.executeBatch();
updateStockStmt.executeBatch();

                paymentStmt.setInt(1, paymentId);
                paymentStmt.setInt(2, orderId);
                paymentStmt.setString(3, paymentMethod);
                paymentStmt.setDouble(4, total);
                paymentStmt.executeUpdate();

                clearCartStmt.setString(1, customerPhone);
                clearCartStmt.executeUpdate();

                conn.commit();
                return true;

            } catch (Exception e) {
                conn.rollback();
                System.out.println("Failed to create order");
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception e) {
            System.out.println("Database error during checkout");
            e.printStackTrace();
            return false;
        }
    }
    public List<OrderInfo> getOrdersByCustomerPhone(String customerPhone) {
        updateDeliveredOrders();
    List<OrderInfo> orders = new ArrayList<>();

    String sql = """
        SELECT 
            o.order_id,
            o.total_amount,
            o.order_date,
            o.order_status,
            o.receiver_name,
            o.receiver_phone,
            o.city,
            o.street,
            p.payment_method,
            p.payment_status
        FROM Orders o
        LEFT JOIN Payment p ON o.order_id = p.order_id
        WHERE o.customer_phone = ?
        ORDER BY o.order_id DESC
    """;

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {

        pst.setString(1, customerPhone);

        try (ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                OrderInfo order = new OrderInfo(
                        rs.getInt("order_id"),
                        rs.getDouble("total_amount"),
                        String.valueOf(rs.getTimestamp("order_date")),
                        rs.getString("order_status"),
                        rs.getString("receiver_name"),
                        rs.getString("receiver_phone"),
                        rs.getString("city"),
                        rs.getString("street"),
                        rs.getString("payment_method"),
                        rs.getString("payment_status")
                );

                orders.add(order);
            }
        }

    } catch (Exception e) {
        System.out.println("Failed to load customer orders");
        e.printStackTrace();
    }

    return orders;
}
    private int getNextShipmentId(Connection conn) throws Exception {
    String sql = "SELECT COALESCE(MAX(shipment_id), 7000) + 1 AS next_id FROM Shipment";

    try (PreparedStatement pst = conn.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        if (rs.next()) {
            return rs.getInt("next_id");
        }
    }

    return 7001;
}

public boolean confirmOrderPaymentAndShipment(int orderId,
                                               String paymentMethod,
                                               String shippingCompany,
                                               String trackingNumber,
                                               int deliveryHours) {

    String paymentStatus = paymentMethod.equalsIgnoreCase("Card") ? "Paid" : "Pending";

    String updatePaymentSql = """
        UPDATE Payment
        SET payment_status = ?
        WHERE order_id = ?
    """;

    String updateOrderSql = """
        UPDATE Orders
        SET order_status = 'Shipped'
        WHERE order_id = ?
    """;

    String insertShipmentSql = """
        INSERT INTO Shipment
        (shipment_id, order_id, shipment_status, shipping_company, tracking_number, delivery_date)
        VALUES (?, ?, 'OutForDelivery', ?, ?, DATE_ADD(NOW(), INTERVAL ? MINUTE))
        ON DUPLICATE KEY UPDATE
            shipment_status = 'OutForDelivery',
            shipping_company = VALUES(shipping_company),
            tracking_number = VALUES(tracking_number),
            delivery_date = VALUES(delivery_date)
    """;

    try (Connection conn = DBConnection.getConnection()) {

        conn.setAutoCommit(false);

        try (
            PreparedStatement paymentStmt = conn.prepareStatement(updatePaymentSql);
            PreparedStatement orderStmt = conn.prepareStatement(updateOrderSql);
            PreparedStatement shipmentStmt = conn.prepareStatement(insertShipmentSql)
        ) {
            int shipmentId = getNextShipmentId(conn);

            paymentStmt.setString(1, paymentStatus);
            paymentStmt.setInt(2, orderId);
            paymentStmt.executeUpdate();

            orderStmt.setInt(1, orderId);
            orderStmt.executeUpdate();

            shipmentStmt.setInt(1, shipmentId);
            shipmentStmt.setInt(2, orderId);
            shipmentStmt.setString(3, shippingCompany);
            shipmentStmt.setString(4, trackingNumber);
            shipmentStmt.setInt(5, deliveryHours);
            shipmentStmt.executeUpdate();

            conn.commit();
            return true;

        } catch (Exception e) {
            conn.rollback();
            System.out.println("Failed to confirm order payment and shipment");
            e.printStackTrace();
            return false;
        } finally {
            conn.setAutoCommit(true);
        }

    } catch (Exception e) {
        System.out.println("Database error during confirmation");
        e.printStackTrace();
        return false;
    }
}

public void updateDeliveredOrders() {
    String sql = """
        UPDATE Orders o
        JOIN Shipment s ON o.order_id = s.order_id
        SET 
            o.order_status = 'Delivered',
            s.shipment_status = 'Delivered'
        WHERE o.order_status = 'Shipped'
          AND s.delivery_date <= NOW()
    """;

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {

        pst.executeUpdate();

    } catch (Exception e) {
        System.out.println("Failed to update delivered orders");
        e.printStackTrace();
    }
}


public boolean isFirstOrderForCustomer(String customerPhone, int orderId) {
    String sql = """
        SELECT MIN(order_id) AS first_order_id
        FROM Orders
        WHERE customer_phone = ?
    """;

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {

        pst.setString(1, customerPhone);

        try (ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("first_order_id") == orderId;
            }
        }

    } catch (Exception e) {
        System.out.println("Failed to check first order");
        e.printStackTrace();
    }

    return false;
}

public List<String> getAllOrdersForAdmin() {
    List<String> orders = new ArrayList<>();

    String sql = """
        SELECT 
            o.order_id,
            o.customer_phone,
            o.total_amount,
            o.order_date,
            o.order_status,
            o.receiver_name,
            o.receiver_phone,
            o.city,
            o.street,
            p.payment_method,
            p.payment_status
        FROM Orders o
        LEFT JOIN Payment p ON o.order_id = p.order_id
        ORDER BY o.order_date DESC, o.order_id DESC
    """;

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        while (rs.next()) {
            StringBuilder sb = new StringBuilder();

            sb.append("Order ID: ").append(rs.getInt("order_id")).append("\n");
            sb.append("Customer Phone: ").append(rs.getString("customer_phone")).append("\n");
            sb.append("Order Date: ").append(rs.getTimestamp("order_date")).append("\n");
            sb.append("Status: ").append(rs.getString("order_status")).append("\n");
            sb.append("Total: $").append(String.format("%.2f", rs.getDouble("total_amount"))).append("\n");
            sb.append("Receiver: ").append(rs.getString("receiver_name")).append("\n");
            sb.append("Receiver Phone: ").append(rs.getString("receiver_phone")).append("\n");
            sb.append("Address: ")
                    .append(rs.getString("city"))
                    .append(", ")
                    .append(rs.getString("street"))
                    .append("\n");

            String paymentMethod = rs.getString("payment_method");
            String paymentStatus = rs.getString("payment_status");

            sb.append("Payment: ")
                    .append(paymentMethod == null ? "Not added yet" : paymentMethod)
                    .append(" / ")
                    .append(paymentStatus == null ? "Not added yet" : paymentStatus)
                    .append("\n");

            sb.append("----------------------------------\n");

            orders.add(sb.toString());
        }

    } catch (Exception e) {
        System.out.println("Failed to load all orders for admin");
        e.printStackTrace();
    }

    return orders;
}

}