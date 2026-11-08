package com.spring.aurora.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.spring.aurora.sms.OverdueCustomers;
import com.spring.aurora.sms.SmsService;

/**
 * Text reminders to inactive customers (docs/specs/sms-inactive-customers.md). Sending and the
 * opt-out switch are POSTs: a GET carries no CSRF token.
 */
@Controller
@RequestMapping("/sms")
public class SmsController {

    private static final Logger logger = LoggerFactory.getLogger(SmsController.class);

    private final SmsService sms;

    public SmsController(SmsService sms) {
        this.sms = sms;
    }

    /** The overdue customers, why any are skipped, and the message to send. */
    @GetMapping("/compose")
    public String compose(Model model) {
        model.addAttribute("candidates", this.sms.candidates(LocalDate.now(), LocalDateTime.now()));
        model.addAttribute("template", SmsService.DEFAULT_TEMPLATE);
        model.addAttribute("maxLength", SmsService.MAX_LENGTH);
        model.addAttribute("configured", this.sms.isConfigured());
        model.addAttribute("cooldownDays", SmsService.COOLDOWN_DAYS);
        model.addAttribute("graceDays", OverdueCustomers.GRACE_DAYS);
        model.addAttribute("defaultIntervalDays", OverdueCustomers.DEFAULT_INTERVAL_DAYS);
        return "sms-compose";
    }

    @PostMapping("/send")
    public String send(@RequestParam(value = "customerIds", required = false) List<Long> customerIds,
            @RequestParam(value = "template", defaultValue = "") String template,
            Principal user, RedirectAttributes redirect) {
        if (!this.sms.isConfigured()) {
            redirect.addFlashAttribute("failmsg", "Texting isn't set up on this PC, so nothing was sent.");
            return "redirect:/sms/compose";
        }
        if (customerIds == null || customerIds.isEmpty()) {
            redirect.addFlashAttribute("failmsg", "Pick at least one customer to text.");
            return "redirect:/sms/compose";
        }
        if (template.isBlank() || template.length() > SmsService.MAX_LENGTH) {
            redirect.addFlashAttribute("failmsg", "The message must be 1 to " + SmsService.MAX_LENGTH + " characters.");
            return "redirect:/sms/compose";
        }

        List<SmsService.Outcome> outcomes = this.sms.send(customerIds, template.strip(), user.getName(),
                LocalDate.now(), LocalDateTime.now());
        long sent = outcomes.stream().filter(SmsService.Outcome::sent).count();
        long failed = outcomes.stream().filter(o -> !o.sent() && !"Skipped".equals(o.state())).count();
        long skipped = outcomes.size() - sent - failed;
        logger.info("Texts: {} sent, {} failed, {} skipped, by {}.", sent, failed, skipped, user.getName());

        redirect.addFlashAttribute("outcomes", outcomes);
        String summary = sent + (sent == 1 ? " text" : " texts") + " handed to the phone"
                + (failed > 0 ? ", " + failed + " failed" : "")
                + (skipped > 0 ? ", " + skipped + " skipped" : "") + ".";
        redirect.addFlashAttribute(failed > 0 ? "failmsg" : "msg", summary);
        return "redirect:/sms/list";
    }

    /** Every text Aurora asked the phone to send, newest first, plus the last send's results. */
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("messages", this.sms.log());
        model.addAttribute("configured", this.sms.isConfigured());
        return "sms-list";
    }

    /** The "Don't send texts" switch on the customer's page. */
    @PostMapping("/optOut")
    public String optOut(@RequestParam("customerId") long customerId,
            @RequestParam("optOut") boolean optOut, RedirectAttributes redirect) {
        this.sms.setOptOut(customerId, optOut);
        redirect.addFlashAttribute("msg", optOut ? "This customer won't be texted." : "This customer can be texted again.");
        return "redirect:/customers/view?customerId=" + customerId;
    }
}
