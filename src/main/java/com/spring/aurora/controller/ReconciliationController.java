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
 * The owner's monthly bank reconciliation (docs/specs/owner-role-bank-reconciliation.md).
 * SecurityConfig lets only ROLE_OWNER reach these addresses.
 */
@Controller
@RequestMapping("/expenses/reconciliation")
public class ReconciliationController {

    public static final int NOTE_MAX_LENGTH = 1000;

    /** The ?d= format the monthly reports use: "03-2026". */
    private static final DateTimeFormatter PARAM = DateTimeFormatter.ofPattern("MM-yyyy");
    /** The form's month field, as an HTML month input sends it: "2026-03". */
    private static final DateTimeFormatter FORM = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter LABEL = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    private final ExpenseDao expenses;
    private final BankReconciliationRepository reconciliations;

    public ReconciliationController(ExpenseDao expenses, BankReconciliationRepository reconciliations) {
        this.expenses = expenses;
        this.reconciliations = reconciliations;
    }

    @GetMapping
    public String reconciliation(@RequestParam(value = "d", defaultValue = "today") String d, Model model) {
        YearMonth month = "today".equalsIgnoreCase(d) ? YearMonth.now() : YearMonth.parse(d, PARAM);
        List<Expense> monthExpenses = this.expenses.findAllBetween(Date.valueOf(month.atDay(1)), Date.valueOf(month.atEndOfMonth()));
        Map<String, BigDecimal> byType = ExpenseTotals.byType(monthExpenses);

        model.addAttribute("month", month.atDay(1));
        model.addAttribute("monthLabel", month.format(LABEL));
        model.addAttribute("monthParam", month.format(PARAM));
        model.addAttribute("totalsByType", byType);
        model.addAttribute("total", ExpenseTotals.total(monthExpenses));
        model.addAttribute("noteMaxLength", NOTE_MAX_LENGTH);

        BankReconciliation entry = this.reconciliations.findById(month.atDay(1)).orElse(null);
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

    @PostMapping("/save")
    public String save(@RequestParam("month") String monthField,
            @RequestParam(value = "statementAmount", defaultValue = "") String amountField,
            @RequestParam(value = "note", defaultValue = "") String noteField,
            Principal user, RedirectAttributes redirect) {
        YearMonth month;
        try {
            month = YearMonth.parse(monthField, FORM);
        } catch (DateTimeParseException e) {
            redirect.addFlashAttribute("failmsg", "Pick the month to reconcile.");
            return "redirect:/expenses/reconciliation";
        }
        String back = "redirect:/expenses/reconciliation?d=" + month.format(PARAM);

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

        BankReconciliation entry = this.reconciliations.findById(month.atDay(1)).orElseGet(BankReconciliation::new);
        entry.setMonth(month.atDay(1));
        entry.setStatementAmount(amount);
        entry.setNote(note.isEmpty() ? null : note);
        entry.setUpdatedAt(LocalDateTime.now().withNano(0));
        entry.setUpdatedBy(user.getName());
        this.reconciliations.save(entry);

        redirect.addFlashAttribute("msg", "Reconciliation for " + month.format(LABEL) + " saved.");
        return back;
    }
}
