package com.spring.aurora.sms;

/** Used when AURORA_SMS_URL isn't set: the texting screens open, but nothing can be sent. */
public class NotConfiguredSmsGateway implements SmsGateway {

    @Override
    public boolean isConfigured() {
        return false;
    }

    @Override
    public Result send(String phoneNumber, String text) {
        return Result.failed("Texting isn't set up on this PC.");
    }
}
