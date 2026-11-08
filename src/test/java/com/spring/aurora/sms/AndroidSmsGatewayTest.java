package com.spring.aurora.sms;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * The phone client against a stand-in for SMS Gateway for Android's local server, which answers
 * the way the app's docs describe. The real phone is checked by hand before the shop uses it.
 */
class AndroidSmsGatewayTest {

    private HttpServer phone;
    private final AtomicReference<String> method = new AtomicReference<>();
    private final AtomicReference<String> path = new AtomicReference<>();
    private final AtomicReference<String> auth = new AtomicReference<>();
    private final AtomicReference<String> contentType = new AtomicReference<>();
    private final AtomicReference<String> body = new AtomicReference<>();

    @AfterEach
    void stopPhone() {
        if (this.phone != null) {
            this.phone.stop(0);
        }
    }

    /** Starts the stand-in phone, answering every request with {@code status} and {@code reply}. */
    private String phoneAnswering(int status, String reply, Duration delay) throws IOException {
        this.phone = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        this.phone.createContext("/", (HttpExchange exchange) -> {
            this.method.set(exchange.getRequestMethod());
            this.path.set(exchange.getRequestURI().getPath());
            this.auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
            this.contentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            this.body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            try {
                Thread.sleep(delay.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            byte[] bytes = reply.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
        this.phone.start();
        return "http://127.0.0.1:" + this.phone.getAddress().getPort() + "/";
    }

    private static AndroidSmsGateway gateway(String url) {
        return new AndroidSmsGateway(url, "sms", "s3cret", Duration.ofSeconds(2), Duration.ofMillis(800));
    }

    @Test
    void sendsOneTextTheWayTheGatewayAppExpects() throws IOException {
        String url = phoneAnswering(202, "{\"id\":\"msg-1\",\"state\":\"Pending\"}", Duration.ZERO);

        SmsGateway.Result result = gateway(url).send("+639170000001", "Hi Ana, it's been 10 days.");

        assertThat(result).isEqualTo(SmsGateway.Result.accepted("Pending", "msg-1"));
        assertThat(result.ok()).isTrue();
        assertThat(this.method.get()).isEqualTo("POST");
        assertThat(this.path.get()).as("the trailing slash in the address is dropped").isEqualTo("/message");
        assertThat(this.auth.get()).isEqualTo("Basic " + Base64.getEncoder().encodeToString("sms:s3cret".getBytes()));
        assertThat(this.contentType.get()).startsWith("application/json");
        assertThat(this.body.get())
                .contains("\"textMessage\":{\"text\":\"Hi Ana, it's been 10 days.\"}")
                .contains("\"phoneNumbers\":[\"+639170000001\"]");
    }

    @Test
    void anEmptyOrUnexpectedAnswerStillCountsAsHandedOver() throws IOException {
        String url = phoneAnswering(200, "{}", Duration.ZERO);

        assertThat(gateway(url).send("+639170000001", "Hi")).isEqualTo(SmsGateway.Result.accepted("Pending", null));
    }

    @Test
    void aWrongPasswordSaysWhereToFindTheRightOne() throws IOException {
        String url = phoneAnswering(401, "", Duration.ZERO);

        SmsGateway.Result result = gateway(url).send("+639170000001", "Hi");

        assertThat(result.ok()).isFalse();
        assertThat(result.state()).isEqualTo(SmsMessage.ERROR);
        assertThat(result.error()).contains("HTTP 401").contains("username and password").doesNotContain("s3cret");
    }

    @Test
    void aPhoneErrorIsReported() throws IOException {
        String url = phoneAnswering(500, "{\"message\":\"boom\"}", Duration.ZERO);

        assertThat(gateway(url).send("+639170000001", "Hi").error()).isEqualTo("The phone refused the text (HTTP 500).");
    }

    @Test
    void aPhoneThatIsOffIsReported() throws IOException {
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            closedPort = socket.getLocalPort();
        }
        String url = "http://127.0.0.1:" + closedPort;

        SmsGateway.Result result = gateway(url).send("+639170000001", "Hi");

        assertThat(result.error()).startsWith("Couldn't reach the phone at " + url).doesNotContain("s3cret");
    }

    @Test
    void aPhoneThatNeverAnswersTimesOut() throws IOException {
        String url = phoneAnswering(200, "{}", Duration.ofSeconds(3));

        long started = System.nanoTime();
        SmsGateway.Result result = gateway(url).send("+639170000001", "Hi");

        assertThat(result.error()).startsWith("Couldn't reach the phone");
        assertThat(Duration.ofNanos(System.nanoTime() - started)).isLessThan(Duration.ofMillis(2500));
    }
}
