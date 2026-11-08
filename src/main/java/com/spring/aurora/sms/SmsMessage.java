package com.spring.aurora.sms;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

/** One text Aurora asked the phone to send, whether or not it went: the texting log. */
@Entity
@Table(name = "sms_message")
@Data
@NoArgsConstructor
public class SmsMessage {

    /** Set when Aurora could not hand the text to the phone (not reachable, refused, no reply). */
    public static final String ERROR = "Error";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sms_message_id")
    private long smsMessageId;

    @Column(name = "customer_id")
    private long customerId;

    /** The number as sent: +639XXXXXXXXX. */
    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "body")
    private String body;

    /** The phone's state for the message (Pending, Processed, Sent, Delivered, Failed), or {@link #ERROR}. */
    @Column(name = "state")
    private String state;

    @Column(name = "gateway_id")
    private String gatewayId;

    @Column(name = "error")
    private String error;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;
}
