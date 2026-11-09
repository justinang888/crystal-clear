package com.spring.aurora.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.spring.aurora.dao.ExpenseDao;
import com.spring.aurora.model.BankReconciliation;
import com.spring.aurora.model.BankReconciliationRepository;
import com.spring.aurora.model.Expense;
import com.spring.aurora.model.ExpensePaymentType;
import com.spring.aurora.util.ExpenseTotals;

/**
 * The owner's bank reconciliation, for a month or a single day
 * (docs/specs/owner-role-bank-reconciliation.md). SecurityConfig lets only ROLE_OWNER reach
 * these addresses. ?d=MM-yyyy opens a month (the default: this month); ?day=yyyy-MM-dd a day.
 */
@Controller
@RequestMapping("/expenses/reconciliation")
public class ReconciliationController {

    public static final int NOTE_MAX_LENGTH = 1000;

    /** The ?d= format the monthly reports use: "03-2026". */
    private static final DateTimeFormatter MONTH_PARAM = DateTimeFormatter.ofPattern("MM-yyyy");
    /** The form's month field, as an HTML month input sends it: "2026-03". */
    private static final DateTimeFormatter MONTH_FIELD = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH);

    private final ExpenseDao expenses;
    private final BankReconciliationRepository reconciliations;

    public ReconciliationController(ExpenseDao expenses, BankReconciliationRepository reconciliations) {
        this.expenses = expenses;
        this.reconciliations = reconciliations;
    }

    /** A day or a month: its kind, first and last day, its label and the address that opens it. */
    private record Period(String kind, LocalDate start, LocalDate end, String label, String address) {

        static Period month(YearMonth month) {
            return new Period(BankReconciliation.MONTH, month.atDay(1), month.atEndOfMonth(), month.format(MONTH_LABEL),
                    "/expenses/reconciliation?d=" + month.format(MONTH_PARAM));
        }

        static Period day(LocalDate day) {
            return new Period(BankReconciliation.DAY, day, day, day.format(DAY_LABEL), "/expenses/reconciliation?day=" + day);
        }
    }

    @GetMapping
    public String reconciliation(@RequestParam(value = "d", defaultValue = "today") String d,
            @RequestParam(value = "day", required = false) String day, Model model) {
        Period period = day != null && !day.isBlank()
                ? Period.day(LocalDate.parse(day))
                : Period.month("today".equalsIgnoreCase(d) ? YearMonth.now() : YearMonth.parse(d, MONTH_PARAM));
        List<Expense> periodExpenses = this.expenses.findAllBetween(Date.valueOf(period.start()), Date.valueOf(period.end()));
        Map<String, BigDecimal> byType = ExpenseTotals.byType(periodExpenses);

        model.addAttribute("period", period.kind());
        model.addAttribute("periodStart", period.start());
        model.addAttribute("periodLabel", period.label());
        // The month the period falls in, for the month picker and the Month tab.
        YearMonth month = YearMonth.from(period.start());
        model.addAttribute("month", month.atDay(1));
        model.addAttribute("monthLabel", month.format(MONTH_LABEL));
        model.addAttribute("monthParam", month.format(MONTH_PARAM));
        model.addAttribute("totalsByType", byType);
        model.addAttribute("total", ExpenseTotals.total(periodExpenses));
        model.addAttribute("noteMaxLength", NOTE_MAX_LENGTH);

        BankReconciliation entry = this.reconciliations
                .findById(new BankReconciliation.Key(period.kind(), period.start())).orElse(null);
        Map<String, Object> shown = null;
        BigDecimal difference = null;
        if (entry != null) {
            shown = new HashMap<>();
            shown.put("statementAmount", entry.getStatementAmount());
            shown.put("note", entry.getNote());
            shown.put("updatedAt", entry.getUpdatedAt());
            shown.put("updatedBy", entry.getUpdatedBy());
            // What the bank says went out by transfer, minus what Aurora has recorded as Bank Transfer.
            difference = entry.getStatementAmount().subtract(byType.get(ExpensePaymentType.BANK_TRANSFER.name()));
        }
        model.addAttribute("entry", shown);
        model.addAttribute("difference", difference);
        return "bank-reconciliation";
    }

    /** Saves a day's entry (?day=yyyy-MM-dd) or a month's (?month=yyyy-MM), replacing any before it. */
    @PostMapping("/save")
    public String save(@RequestParam(value = "month", required = false) String monthField,
            @RequestParam(value = "day", required = false) String dayField,
            @RequestParam(value = "statementAmount", defaultValue = "") String amountField,
            @RequestParam(value = "note", defaultValue = "") String noteField,
            Principal user, RedirectAttributes redirect) {
        Period period;
        try {
            period = dayField != null && !dayField.isBlank()
                    ? Period.day(LocalDate.parse(dayField))
                    : Period.month(YearMonth.parse(monthField == null ? "" : monthField, MONTH_FIELD));
        } catch (DateTimeParseException e) {
            redirect.addFlashAttribute("failmsg", "Pick the day or month to reconcile.");
            return "redirect:/expenses/reconciliation";
        }
        String back = "redirect:" + period.address();

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountField.strip().replace(",", ""));
        } catch (NumberFormatException e) {
            redirect.addFlashAttribute("failmsg", "Enter the bank statement amount, for example 2500.00.");
            return back;
        }
        if (amount.signum() < 0 || amount.scale() > 2) {
            redirect.addFlashAttribute("failmsg", "The bank statement amount must be zero or more, with at most two decimals.");
            return back;
        }
        String note = noteField.strip();
        if (note.length() > NOTE_MAX_LENGTH) {
            redirect.addFlashAttribute("failmsg", "The note can be at most " + NOTE_MAX_LENGTH + " characters.");
            return back;
        }

        BankReconciliation entry = this.reconciliations
                .findById(new BankReconciliation.Key(period.kind(), period.start()))
                .orElseGet(BankReconciliation::new);
        entry.setPeriod(period.kind());
        entry.setPeriodStart(period.start());
        entry.setStatementAmount(amount);
        entry.setNote(note.isEmpty() ? null : note);
        entry.setUpdatedAt(LocalDateTime.now().withNano(0));
        entry.setUpdatedBy(user.getName());
        this.reconciliations.save(entry);

        redirect.addFlashAttribute("msg", "Reconciliation for " + period.label() + " saved.");
        return back;
    }
}
