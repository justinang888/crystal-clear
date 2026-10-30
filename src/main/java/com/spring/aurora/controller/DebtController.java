package com.spring.aurora.controller;

import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.spring.aurora.entity.DebtCustomerEntity;
import com.spring.aurora.model.ArSummary;
import com.spring.aurora.model.ArSummaryRepository;
import com.spring.aurora.model.Customer;
import com.spring.aurora.model.Debt;
import com.spring.aurora.service.CustomerService;
import com.spring.aurora.service.DebtService;
import com.spring.aurora.service.PaymentService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/debts")
@AllArgsConstructor
public class DebtController {

    private DebtService debtService;
    private PaymentService paymentService;
    private CustomerService customerService;
    private ArSummaryRepository arSummaryRepo;

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String newDebt(@RequestParam long cid, Model model) {
        Debt debt = new Debt();
        debt.setCreatedAt(Date.valueOf(LocalDate.now()));
        debt.setCustomerId(cid);
        Customer customer = this.customerService.view(cid);
        model.addAttribute("debt", debt);
        model.addAttribute("customer", customer);
        return "new-debt";
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveDebt(@ModelAttribute("debt") Debt debt,
            BindingResult result, Model model,
            final RedirectAttributes redirect){
        if (result.hasErrors()) {
            return "new-debt";
        } else {
            redirect.addFlashAttribute("msg", "New debt logged successfully!");
        }
        this.debtService.insert(debt);
        return "redirect:/debts/list?cid=" +debt.getCustomerId();
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String listDebts(@RequestParam long cid, Model model) {
        List<Debt> debts = this.debtService.findAllByCustomerId(cid);
        Customer customer = this.customerService.view(cid);
        model.addAttribute("debts", debts);
        model.addAttribute("debtsTotal", this.getDebtsTotal(cid));
        model.addAttribute("customer", customer);
        return "list-debts";
    }

    @RequestMapping(value = "/listAllOld", method = RequestMethod.GET)
    public String listAllDebts(Model model, @RequestParam(value="mode", defaultValue="normal", required=false) String mode) {
        List<Customer> customers = this.customerService.findAll();
        Date dateToday = Date.valueOf(LocalDate.now());
        Map<Long, Object> debtsMap = customers.stream()
                .filter(customer -> this.getDebtsTotal(customer.getCustomerId()) > 0.0)
                .collect(Collectors.toMap(Customer::getCustomerId,
                        customer -> new DebtCustomerEntity(
                                customer.getName(),
                                this.getDebtsTotal(customer.getCustomerId()),
                                0.00 // this.paymentService.getPaymentsTotalByCustomerId(customer.getCustomerId()))
                                ))
                        );
        model.addAttribute("debtsMap", debtsMap);
        Double arTotal = this.getTotalArsAsOfToday();
        model.addAttribute("arTotal", arTotal);
        model.addAttribute("dateToday", dateToday);

        if (mode.equalsIgnoreCase("preview")) {
            return "list-debts-all-print-preview-old";
        } else {
            return "list-debts-all-old";
        }

    }

    @GetMapping("/listAll")
    public String listArSummary(@RequestParam(value="mode", defaultValue="normal", required=false) String mode, Model model) {
        List<ArSummary> arSummary = this.arSummaryRepo.findAll();
        arSummary.forEach(ars -> ars.setCustomer(this.customerService.findByCustomerId(ars.getCustomerId())));

        double arTotal = arSummary.stream()
                .collect(Collectors.summingDouble(ar -> ar.getArAmount().doubleValue()));
        Predicate<ArSummary> removeZero = ar -> ar.getArAmount().compareTo(BigDecimal.ZERO) != 0;
        model.addAttribute("debtsMap", arSummary.stream().filter(removeZero).collect(toList()));
        model.addAttribute("arTotal", arTotal);
        model.addAttribute("dateToday", LocalDate.now());

        if (mode.equalsIgnoreCase("preview")) {
            return "list-debts-all-print-preview";
        } else {
            return "list-debts-all";
        }
    }

    private Double getTotalArsAsOfToday() {
        Double totalARs = this.debtService.findTotalARs();
        Double totalPmts = this.paymentService.getTotalPayments();
        return totalARs - totalPmts;
    }

    private double getDebtsTotal(long customerId) {
        double debtsSubTotal = this.debtService.findDebtsTotalByCustomerId(customerId);
        double paymentsSubTotal = this.paymentService.getPaymentsTotalByCustomerId(customerId);
        return debtsSubTotal - paymentsSubTotal;
    }
}
