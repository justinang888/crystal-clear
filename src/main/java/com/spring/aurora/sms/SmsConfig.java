package com.spring.aurora.sms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The phone to text through, from the environment like the database login:
 * AURORA_SMS_URL (for example http://192.168.1.50:8080), AURORA_SMS_USERNAME and
 * AURORA_SMS_PASSWORD, as shown in the phone's SMS Gateway app. Without a URL texting is
 * switched off. The demo registers its own fake phone first.
 */
@Configuration
public class SmsConfig {

    @Bean
    @ConditionalOnMissingBean(SmsGateway.class)
    SmsGateway smsGateway(@Value("${aurora.sms.url:}") String url,
            @Value("${aurora.sms.username:}") String username,
            @Value("${aurora.sms.password:}") String password) {
        return url.isBlank() ? new NotConfiguredSmsGateway() : new AndroidSmsGateway(url, username, password);
    }
}
