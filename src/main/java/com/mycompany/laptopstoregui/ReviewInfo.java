package com.mycompany.laptopstoregui;

public class ReviewInfo {
    int reviewId;
    String customerPhone;
    int productId;
    int orderId;
    int rating;
    String comment;
    String reviewDate;
    String reviewType;

    public ReviewInfo(int reviewId, String customerPhone, int productId, int orderId,
                      int rating, String comment, String reviewDate, String reviewType) {
        this.reviewId = reviewId;
        this.customerPhone = customerPhone;
        this.productId = productId;
        this.orderId = orderId;
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = reviewDate;
        this.reviewType = reviewType;
    }
}