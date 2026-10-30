package com.spring.aurora.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.spring.aurora.model.Order;
import com.spring.aurora.service.OrderService;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    DataSource dataSource;

    @Autowired
    private OrderService orderService;

    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String home(Locale locale, Model model) {
        logger.info("Welcome home! The client locale is {}.", locale);

        Date date = new Date();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);

        String formattedDate = dateFormat.format(date);

        model.addAttribute("serverTime", formattedDate);

        return "home";
    }

    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public String viewDashboard(Model model) {
        logger.info("View the dashboard.");

        List<Order> ordersToday = new ArrayList<>();
        new ArrayList<>();
        java.sql.Date date = java.sql.Date.valueOf(LocalDate.now());
        ordersToday = this.orderService.findAllOrdersToday(date);
        int pendingCount = 0;
        int runningRound = 0;
        int runningSlim = 0;

        for (Order order : ordersToday) {
            if (order.getStatus().equalsIgnoreCase("pending")) {
                pendingCount++;
            }

            runningRound = runningRound + order.getRoundRefillOnlyCount() - Integer.parseInt(order.getRoundReturned());
            runningSlim = runningSlim + order.getSlimRefillOnlyCount() - Integer.parseInt(order.getSlimReturned());
        }

        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("runningRound", runningRound);
        model.addAttribute("runningSlim", runningSlim);
        return "view-dashboard";
    }

    @RequestMapping(value = "/help", method = RequestMethod.GET)
    public String help(Model model) {
        return "help-page";
    }

    @RequestMapping(value = "/loginx", method = RequestMethod.GET)
    public String getLoginPage(@RequestParam(value = "error", required = false) boolean error,
            @RequestParam boolean logout, Model model) {
        logger.debug("Received request to show login page");

        /*
         * Add an error message to the model if login is unsuccessful The
         * 'error' parameter is set to true based on the when the authentication
         * has failed. We declared this under the authentication-failure-url
         * attribute inside the spring-security.xml See below: <form-login
         * login-page="/krams/auth/login"
         * authentication-failure-url="/krams/auth/login?error=true"
         * default-target-url="/krams/main/common"/>
         */
        if (error == true) {
            // Assign an error message
            model.addAttribute("error", "You have entered an invalid username or password");
        }
        if (logout == true) {
            model.addAttribute("msg", "You've been logged out successfully.");
        }

        return "login";
    }

    // @RequestMapping(value = "/error403", method = RequestMethod.GET)
    // public String getDeniedPage(Model model) {
    // logger.debug("Received request to show denied page");
    // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    // if (!(auth instanceof AnonymousAuthenticationToken)) {
    // UserDetails userDetail = (UserDetails) auth.getPrincipal();
    // model.addAttribute("username", userDetail.getUsername());
    // }
    // // This will resolve to /WEB-INF/jsp/403.jsp
    // return "error403";
    // }

    /*
     * @RequestMapping(value = "/home", method = RequestMethod.POST) public
     * String login(@RequestParam String username, @RequestParam String
     * password, Model model) { User u = this.userService.loginUser(username,
     * password);
     * 
     * if (u == null) { return "login"; } else { model.addAttribute("username",
     * username); model.addAttribute("user", u); return "user"; } }
     */

    @RequestMapping ("/alter")
    public String alter(Model model) throws SQLException {
        //String alterStmt = "ALTER TABLE `aurora`.`orders` ADD COLUMN `slim_buy_count` INT(11) NOT NULL DEFAULT '0' AFTER `cont_round_count`, ADD COLUMN `round_buy_count` INT(11) NOT NULL DEFAULT '0' AFTER `slim_buy_count`";

        String alterStmt = "ALTER TABLE `aurora`.`orders` CHANGE COLUMN `cont_slim_count` `slim_refill_only_count` INT(11) NOT NULL DEFAULT '0' , CHANGE COLUMN `cont_round_count` `round_refill_only_count` INT(11) NOT NULL DEFAULT '0' , ADD COLUMN `slim_container_only_count` INT(11) NOT NULL DEFAULT '0' AFTER `round_refill_only_count`, ADD COLUMN `round_container_only_count` INT(11) NOT NULL DEFAULT '0' AFTER `slim_container_only_count`, ADD COLUMN `slim_refill_with_container_count` INT(11) NOT NULL DEFAULT '0' AFTER `round_container_only_count`, ADD COLUMN `round_refill_with_container_count` INT(11) NOT NULL DEFAULT '0' AFTER `slim_refill_with_container_count`, ADD COLUMN `slim_free_count` INT(11) NOT NULL AFTER `round_refill_with_container_count`, ADD COLUMN `round_free_count` INT(11) NOT NULL AFTER `slim_free_count`";

        Connection conn = this.dataSource.getConnection();
        Statement stmt = conn.createStatement();
        boolean res = stmt.execute(alterStmt);
        model.addAttribute("result", res);
        logger.info("alter table result:="+res);
        return "redirect:/customers/list";

    }
}
