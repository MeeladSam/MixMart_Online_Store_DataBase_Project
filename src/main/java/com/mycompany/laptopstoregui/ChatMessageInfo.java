package com.mycompany.laptopstoregui;

public class ChatMessageInfo {
    String customerPhone;
    String senderType;
    String messageText;
    String sentAt;

    public ChatMessageInfo(String customerPhone,
                           String senderType,
                           String messageText,
                           String sentAt) {
        this.customerPhone = customerPhone;
        this.senderType = senderType;
        this.messageText = messageText;
        this.sentAt = sentAt;
    }
}