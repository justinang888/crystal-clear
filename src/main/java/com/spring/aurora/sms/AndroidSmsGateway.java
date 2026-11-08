package com.spring.aurora.sms;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Sends texts through an Android phone on the shop's Wi-Fi running SMS Gateway for Android in
 * local server mode (https://docs.sms-gate.app/getting-started/local-server/): the phone serves a
 * small web API and sends each text through its own SIM. No internet is involved.
 *
 * <p>The send request is the one in that page's example: {@code POST <phone>/message} with basic
 * authentication and {@code {"textMessage": {"text": ...}, "phoneNumbers": [...]}}. The docs don't
 * show the local response, so {@code id} and {@code state} are read when present (as in the
 * gateway's cloud API) and the text counts as accepted on any 2xx. Check both on the phone's own
 * API page, {@code <phone>/docs}, before relying on this with a new app version.
 *
 * <p>The password is only ever put in the Authorization header: never in a message or log.
 */
public class AndroidSmsGateway implements SmsGateway {

    /** Before the phone has said anything: it may be asleep or off the Wi-Fi. */
    static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    /** The phone answers as soon as it has queued the text, not when it is delivered. */
    static final Duration READ_TIMEOUT = Duration.ofSeconds(20);

    private final String baseUrl;
    private final RestClient client;

    public AndroidSmsGateway(String baseUrl, String username, String password) {
        this(baseUrl, username, password, CONNECT_TIMEOUT, READ_TIMEOUT);
    }

    AndroidSmsGateway(String baseUrl, String username, String password, Duration connectTimeout, Duration readTimeout) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        JdkClientHttpRequestFactory requests = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(connectTimeout).build());
        requests.setReadTimeout(readTimeout);
        this.client = RestClient.builder()
                .baseUrl(this.baseUrl)
                .requestFactory(requests)
                .defaultHeaders(headers -> headers.setBasicAuth(username, password))
                .build();
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public Result send(String phoneNumber, String text) {
        try {
            Map<?, ?> reply = this.client.post()
                    .uri("/message")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("textMessage", Map.of("text", text), "phoneNumbers", List.of(phoneNumber)))
                    .retrieve()
                    .body(Map.class);
            Object id = reply == null ? null : reply.get("id");
            Object state = reply == null ? null : reply.get("state");
            return Result.accepted(state == null ? "Pending" : state.toString(), id == null ? null : id.toString());
        } catch (RestClientResponseException e) {
            int status = e.getStatusCode().value();
            String hint = status == 401 || status == 403
                    ? ": check the username and password shown in the phone's SMS Gateway app"
                    : "";
            return Result.failed("The phone refused the text (HTTP " + status + ")" + hint + ".");
        } catch (ResourceAccessException e) {
            return Result.failed("Couldn't reach the phone at " + this.baseUrl
                    + ". Check that it is on the shop Wi-Fi with SMS Gateway's local server started.");
        } catch (RestClientException e) {
            return Result.failed("The phone's answer couldn't be read.");
        }
    }
}
