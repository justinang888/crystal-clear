package com.spring.aurora.controller;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.spring.aurora.entity.ContainerCustomerEntity;
import com.spring.aurora.model.Container;
import com.spring.aurora.model.Customer;
import com.spring.aurora.model.Order;
import com.spring.aurora.service.ContainerService;
import com.spring.aurora.service.CustomerService;
import com.spring.aurora.service.OrderService;

@Controller
@RequestMapping("/container")
public class ContainerController {

    @Autowired
    private ContainerService containerService;
    @Autowired private CustomerService customerService;
    @Autowired private OrderService orderService;

    @RequestMapping(value = "/return", method = RequestMethod.GET)
    public String returnContainer(@RequestParam long cid, Model model) {
        Container container = new Container();
        container.setCustomerId(cid);
        container.setStatus("R");
        Customer customer = this.customerService.view(cid);
        model.addAttribute("container", container);
        model.addAttribute("customer", customer);
        return "return-container";
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveContainerActivity(@ModelAttribute("container") Container container,
            BindingResult result, Model model,
            final RedirectAttributes redirect){
        if (result.hasErrors()) {
            return "return-container";
        } else {
            redirect.addFlashAttribute("msg", "Returned containers logged successfully!");
        }
        container.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        container.setOrderId(0L);
        this.containerService.insert(container);

        Customer customer = this.customerService.view(container.getCustomerId());
        int currentTotalRound = customer.getTotalRound();
        int currentTotalSlim = customer.getTotalSlim();

        currentTotalRound = currentTotalRound - container.getRoundCount();
        currentTotalSlim = currentTotalSlim - container.getSlimCount();

        customer.setTotalRound(currentTotalRound);
        customer.setTotalSlim(currentTotalSlim);

        this.customerService.update(customer);

        return "redirect:/customers/view?customerId=" +container.getCustomerId();
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String listDebts(@RequestParam String cid, Model model) {

        return "list-debts";
    }

    @RequestMapping(value = "/history", method = RequestMethod.GET)
    public String getHistory(Model model, @RequestParam(value="d", defaultValue="today", required=false) String d ) {

        List<ContainerCustomerEntity> cceList = new ArrayList<>();

        Date date = ("today".equalsIgnoreCase(d)) ? Date.valueOf(LocalDate.now()) : Date.valueOf(LocalDate.parse(d));
        String datePicked = new SimpleDateFormat("MMM dd YYYY").format(date);
        model.addAttribute("datePicked", datePicked);

        List<Container> containersList = this.containerService.findContainerActivityByDate(date);

        System.out.println("Containers List: " + containersList);

        List<Long> custIds = new ArrayList<>();

        for (Container containerActivity : containersList) {

            if (!custIds.contains(containerActivity.getCustomerId())) {
                custIds.add(containerActivity.getCustomerId());
            }
        }

        int grandTotalDeliveredRound = 0;
        int grandTotalDeliveredSlim = 0;
        int grandTotalReturnedRound = 0;
        int grandTotalReturnedSlim = 0;

        for (Long custId : custIds) {

            Customer customer = this.customerService.view(custId);

            int deliveredRoundCount = 0;
            int returnedRoundCount = 0;
            int deliveredSlimCount = 0;
            int returnedSlimCount = 0;

            for (Container container : containersList) {
                if (container.getCustomerId() == custId) {

                    if (container.getStatus().equalsIgnoreCase("B")) {
                        deliveredRoundCount += container.getRoundCount();
                        deliveredSlimCount += container.getSlimCount();
                    } else {
                        returnedRoundCount += container.getRoundCount();
                        returnedSlimCount += container.getSlimCount();
                    }
                }


            }

            ContainerCustomerEntity cce = new ContainerCustomerEntity(customer, deliveredRoundCount,
                    deliveredSlimCount, returnedRoundCount, returnedSlimCount);

            grandTotalDeliveredRound += deliveredRoundCount;
            grandTotalDeliveredSlim += deliveredSlimCount;
            grandTotalReturnedRound += returnedRoundCount;
            grandTotalReturnedSlim += returnedSlimCount;

            cceList.add(cce);
        }

        model.addAttribute("containerDaily", cceList);
        model.addAttribute("totalRoundDelivered", grandTotalDeliveredRound);
        model.addAttribute("totalSlimDelivered", grandTotalDeliveredSlim);
        model.addAttribute("totalRoundReturned", grandTotalReturnedRound);
        model.addAttribute("totalSlimReturned", grandTotalReturnedSlim);

        return "container-history";
    }

    @RequestMapping(value = "/listAllOld", method = RequestMethod.GET)
    public String listAllContainersOld(Model model, @RequestParam(value="mode", defaultValue="normal", required=false) String mode) {

        List<Customer> customers = this.customerService.findAll();
        Map<Long, Object> containersMap = customers.stream()
                .filter(customer -> this.getSlimTotal(customer.getCustomerId()) != 0 || this.getRoundTotal(customer.getCustomerId()) != 0)
                .collect(Collectors.toMap(Customer::getCustomerId,
                        customer -> new ContainerCustomerEntity(
                                customer, this.getSlimTotal(customer.getCustomerId()), this.getRoundTotal(customer.getCustomerId()))));
        model.addAttribute("containersMap", containersMap);

        int runningRound = 0;
        int runningSlim = 0;

        for (Customer c : customers) {
            runningRound += this.getRoundTotal(c.getCustomerId());
            runningSlim += this.getSlimTotal(c.getCustomerId());
        }

        model.addAttribute("runningRound", runningRound);
        model.addAttribute("runningSlim", runningSlim);

        if (mode.equalsIgnoreCase("preview")) {
            return "container-totals-print-preview";
        } else {
            return "container-totals";
        }
    }

    @RequestMapping(value = "/listAllOld2", method = RequestMethod.GET)
    public String listAllContainersOld2(Model model, @RequestParam(value="mode", defaultValue="normal", required=false) String mode) {

        List<Customer> customers = this.customerService.findAll();
        Map<Long, Object> containersMap = new HashMap<>();
        int runningRound = 0;
        int runningSlim = 0;
        Date dateToday = Date.valueOf(LocalDate.now());

        for (Customer c : customers) {

            int customerTotalRound = 0;
            int customerTotalSlim = 0;
            List<Order> orders = this.orderService.findAllByCustomerId(c.getCustomerId());

            for (Order o : orders) {
                customerTotalRound = customerTotalRound + o.getRoundRefillOnlyCount() - Integer.parseInt(o.getRoundReturned());
                customerTotalSlim = customerTotalSlim + o.getSlimRefillOnlyCount() - Integer.parseInt(o.getSlimReturned());
                runningRound = runningRound + o.getRoundRefillOnlyCount() - Integer.parseInt(o.getRoundReturned());
                runningSlim = runningSlim + o.getSlimRefillOnlyCount() - Integer.parseInt(o.getSlimReturned());
            }

            customerTotalRound = customerTotalRound - this.getRoundReturnsByCustomer(c.getCustomerId());
            customerTotalSlim = customerTotalSlim - this.getSlimReturnsByCustomer(c.getCustomerId());
            runningRound = runningRound - this.getRoundReturnsByCustomer(c.getCustomerId());
            runningSlim = runningSlim - this.getSlimReturnsByCustomer(c.getCustomerId());

            ContainerCustomerEntity cce = new ContainerCustomerEntity(c, customerTotalSlim, customerTotalRound);

            if (cce.getRoundTotal() != 0 || cce.getSlimTotal() != 0) {
                containersMap.put(c.getCustomerId(), cce);
            }
        }

        model.addAttribute("containersMap", containersMap);
        model.addAttribute("runningRound", runningRound);
        model.addAttribute("runningSlim", runningSlim);
        model.addAttribute("dateToday", dateToday);

        if (mode.equalsIgnoreCase("preview")) {
            return "container-totals-print-preview";
        } else {
            return "container-totals";
        }
    }

    @RequestMapping(value = "/listAll", method = RequestMethod.GET)
    public String listAllContainers(Model model, @RequestParam(value="mode", defaultValue="normal", required=false) String mode) {

        List<Customer> customers = this.customerService.findAll();
        List<Customer> customerContainerTotals = new ArrayList<>();

        int runningRound = 0;
        int runningSlim = 0;
        Date dateToday = Date.valueOf(LocalDate.now());

        for (Customer c : customers) {
            if (c.getTotalRound() != 0 || c.getTotalSlim() != 0) {
                customerContainerTotals.add(c);
                runningRound = runningRound + c.getTotalRound();
                runningSlim = runningSlim + c.getTotalSlim();
            }
        }

        model.addAttribute("containersList", customerContainerTotals);
        model.addAttribute("runningRound", runningRound);
        model.addAttribute("runningSlim", runningSlim);
        model.addAttribute("dateToday", dateToday);

        if (mode.equalsIgnoreCase("preview")) {
            return "container-totals-print-preview";
        } else {
            return "container-totals";
        }
    }

    public int getSlimReturnsByCustomer(long cid) {

        List<Container> containers = this.containerService.findAllByCustomerId(cid);
        int total = 0;

        for (Container c : containers) {
            if (c.getStatus().equalsIgnoreCase("R")) {
                total += c.getSlimCount();
            }
        }

        return total;
    }

    public int getRoundReturnsByCustomer(long cid) {

        List<Container> containers = this.containerService.findAllByCustomerId(cid);
        int total = 0;

        for (Container c : containers) {
            if (c.getStatus().equalsIgnoreCase("R")) {
                total += c.getRoundCount();
            }
        }

        return total;
    }


    public int getSlimTotal(long cid) {

        List<Container> containers = this.containerService.findAllByCustomerId(cid);
        int total = 0;

        for (Container c : containers) {
            if (c.getStatus().equalsIgnoreCase("B")) {
                total += c.getSlimCount();
            } else {
                total -= c.getSlimCount();
            }
        }

        return total;
    }

    public int getRoundTotal(long cid) {

        List<Container> containers = this.containerService.findAllByCustomerId(cid);
        int total = 0;

        for (Container c : containers) {
            if (c.getStatus().equalsIgnoreCase("B")) {
                total += c.getRoundCount();
            } else {
                total -= c.getRoundCount();
            }
        }

        return total;
    }
}
