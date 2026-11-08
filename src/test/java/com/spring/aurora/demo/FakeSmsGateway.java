package com.spring.aurora.demo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.spring.aurora.sms.SmsGateway;

/**
 * The demo's phone: accepts every text and sends nothing, so the texting screens can be tried
 * without a real phone or SIM. Numbers ending in 0000 act like a phone that can't be reached,
 * to show what a failure looks like.
 */
public class FakeSmsGateway implements SmsGateway {

    public record Sent(String phoneNumber, String text) {
    }

    private final List<Sent> sent = new CopyOnWriteArrayList<>();
    private final AtomicInteger ids = new AtomicInteger();

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public Result send(String phoneNumber, String text) {
        if (phoneNumber.endsWith("0000")) {
            return Result.failed("Couldn't reach the phone (demo: numbers ending in 0000 always fail).");
        }
        this.sent.add(new Sent(phoneNumber, text));
        return Result.accepted("Pending", "demo-" + this.ids.incrementAndGet());
    }

    public List<Sent> sent() {
        return List.copyOf(this.sent);
    }
}
