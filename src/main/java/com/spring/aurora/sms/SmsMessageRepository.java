package com.spring.aurora.sms;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface SmsMessageRepository extends CrudRepository<SmsMessage, Long> {

    /** The texting log, newest first. */
    List<SmsMessage> findAllByOrderByCreatedAtDescSmsMessageIdDesc();

    /** Texts since a moment, any state; the cooldown leaves out the ones that never reached the phone. */
    List<SmsMessage> findAllByCreatedAtAfter(LocalDateTime since);
}
