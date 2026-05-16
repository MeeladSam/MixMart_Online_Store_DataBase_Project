package com.mycompany.laptopstoregui;

public class SellingRequestInfo {
    int requestId;
    String customerPhone;
    String productName;
    double price;
    String imageUrl;
    String requestStatus;
    String submittedAt;
    String description;

    public SellingRequestInfo(int requestId, String customerPhone, String productName,
                              double price, String imageUrl, String requestStatus,
                              String submittedAt, String description) {
        this.requestId = requestId;
        this.customerPhone = customerPhone;
        this.productName = productName;
        this.price = price;
        this.imageUrl = imageUrl;
        this.requestStatus = requestStatus;
        this.submittedAt = submittedAt;
        this.description = description;
    }
}