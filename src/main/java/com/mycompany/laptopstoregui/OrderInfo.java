package com.mycompany.laptopstoregui;

public class OrderInfo {
    int orderId;
    double totalAmount;
    String orderDate;
    String orderStatus;
    String receiverName;
    String receiverPhone;
    String city;
    String street;
    String paymentMethod;
    String paymentStatus;

    public OrderInfo(int orderId, double totalAmount, String orderDate,
                     String orderStatus, String receiverName, String receiverPhone,
                     String city, String street, String paymentMethod, String paymentStatus) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.city = city;
        this.street = street;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
    }
}