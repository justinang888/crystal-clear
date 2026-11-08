package com.spring.aurora.sms;

/**
 * Hands one text to whatever sends it: the shop's Android phone ({@link AndroidSmsGateway}),
 * nothing when texting isn't set up ({@link NotConfiguredSmsGateway}), or a fake phone in the
 * demo and tests.
 */
public interface SmsGateway {

    /** False when there is no phone to send through; the screens then disable Send. */
    boolean isConfigured();

    /**
     * Asks for one text to be sent. Never throws for a sending problem: the result says what
     * happened, so one failure doesn't stop the rest of a batch.
     */
    Result send(String phoneNumber, String text);

    /**
     * What the phone answered. {@code state} is the phone's state for the message (for example
     * Pending) or {@link SmsMessage#ERROR}; {@code error} says why, for people to read.
     */
    record Result(String state, String gatewayId, String error) {

        public static Result accepted(String state, String gatewayId) {
            return new Result(state, gatewayId, null);
        }

        public static Result failed(String error) {
            return new Result(SmsMessage.ERROR, null, error);
        }

        public boolean ok() {
            return error == null;
        }
    }
}
