package com.spring.aurora.controller;

import com.spring.aurora.model.Expense;
import com.spring.aurora.service.ExpenseService;
import com.spring.aurora.util.ExpenseFormValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

@Controller
public class ExpenseController {
    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    ExpenseFormValidator expenseFormValidator;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(expenseFormValidator);
    }

    @RequestMapping(value = "/expenses", method = RequestMethod.GET)
    public String expenses(Model model){
        logger.debug("/expenses");
        return "redirect:/expenses/new";
    }

    @RequestMapping(value = "/expenses/new", method = RequestMethod.GET)
    public String newExpenseForm(Model model) {
        logger.debug("show new expense form.");
        Expense expense = new Expense();
        expense.setCreatedAt(Date.valueOf(LocalDate.now()));
        model.addAttribute("expense", expense);
        return "new-expense";
    }

    @RequestMapping(value = "/expenses/save", method = RequestMethod.POST)
    public String saveExpense(@ModelAttribute("expense") @Validated Expense expense,
                              BindingResult result, Model model,
                              final RedirectAttributes redirect) {
        logger.debug("saving expense");
        if (result.hasErrors()) {
            return "new-expense";
        } else {
            redirect.addFlashAttribute("msg", "Expense logged successfully!");
        }
        expenseService.save(expense);
        return "redirect:/expenses/list";
    }

    @RequestMapping(value = "/expenses/list", method = RequestMethod.GET)
    public ModelAndView listExpensesToday(@RequestParam(required = false, defaultValue = "today", value = "d") String d) {
        logger.debug("list today's expenses");
        Date date = ("today".equalsIgnoreCase(d)) ? Date.valueOf(LocalDate.now()) : Date.valueOf(LocalDate.parse(d));
        List<Expense> expenses = expenseService.findAllByDate(date);
        ModelAndView model = new ModelAndView("list-expenses");
        model.addObject("expenses", expenses);
        model.addObject("expensesTotal", expenseService.getTotalExpensesByDate(date));
        model.addObject("date", new SimpleDateFormat("MMM dd YYYY").format(date));
        return model;
    }
}
