package com.spring.aurora.controller;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.spring.aurora.entity.DailySalesEntity;
import com.spring.aurora.entity.DailyTotalsEntity;
import com.spring.aurora.entity.OrderCustomerEntity;
import com.spring.aurora.entity.OrderProductEntity;
import com.spring.aurora.model.Container;
import com.spring.aurora.model.Customer;
import com.spring.aurora.model.CustomerPrice;
import com.spring.aurora.model.ExpensePaymentType;
import com.spring.aurora.model.PaymentTypes;
import com.spring.aurora.model.Debt;
import com.spring.aurora.model.Expense;
import com.spring.aurora.model.Order;
import com.spring.aurora.model.OrderProduct;
import com.spring.aurora.model.Payment;
import com.spring.aurora.model.Product;
import com.spring.aurora.service.ContainerService;
import com.spring.aurora.service.CustomerPriceService;
import com.spring.aurora.service.CustomerService;
import com.spring.aurora.service.DebtService;
import com.spring.aurora.service.ExpenseService;
import com.spring.aurora.service.OrderProductService;
import com.spring.aurora.service.OrderService;
import com.spring.aurora.dao.ContainerDao;
import com.spring.aurora.dao.ExpenseDao;
import com.spring.aurora.dao.PaymentDao;
import com.spring.aurora.service.PaymentService;
import com.spring.aurora.service.ProductService;
import com.spring.aurora.util.ReportUtil;
import com.spring.aurora.util.SalesTotals;

@Controller
@RequestMapping(value = "/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private static DecimalFormat df2 = new DecimalFormat("#.00");

    private static final String monthsWith31[] = {"01", "03", "05", "07", "08", "10", "12"};
    private static final String monthsStr[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    /** Container activity statuses that belong to an order: borrowed, bought, returned with the order. */
    private static final List<String> ORDER_CONTAINER_ACTIVITY = Arrays.asList("B", "BC", "RO");

    //	@Autowired
    //	OrderFormValidator orderFormValidator;
    //	@InitBinder
    //	protected void initBinder(WebDataBinder binder) {
    //		binder.setValidator(orderFormValidator);
    //	}

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ContainerService containerService;

    @Autowired
    private DebtService debtService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private PaymentService paymentService;

    // The daily sales report reads a range of days straight from these.
    @Autowired
    private ExpenseDao expenseDao;

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private ContainerDao containerDao;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderProductService orderProductService;

    @Autowired
    private CustomerPriceService customerPriceService;

    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @RequestMapping(value = "/neworder", method = RequestMethod.GET)
    public String newOrder(@RequestParam long customerId, Model model) {
        logger.debug("New Order form for customer: " + customerId);
        OrderProductEntity ope = new OrderProductEntity();
        Order order = new Order();
        order.setCustomerId(customerId);
        ope.setOrder(order);

        List<Product> productList = this.productService.findAll();
        List<CustomerPrice> cpList = this.customerPriceService.findAllByCustomerId(customerId);

        List<Product> overridenProductList = new ArrayList<>();
        boolean overriden = false;

        for (Product p : productList) {

            overriden = false;

            for (CustomerPrice cp : cpList) {
                if (p.getProductId() == cp.getProductId()) {
                    Product overridenProduct = withCustomerPrice(p, cp);
                    overridenProductList.add(overridenProduct);
                    overriden = true;
                    break;
                }
            }

            if (!overriden) {
                overridenProductList.add(p);
            }
        }

        if (overridenProductList.size() > 0) {
            model.addAttribute("productList", overridenProductList);
        } else {
            model.addAttribute("productList", productList);
        }

        List<OrderProduct> opList = new ArrayList<>();
        for (Product p : productList) {
            OrderProduct op = new OrderProduct();
            op.setOrderId(order.getOrderId());
            op.setProductId(p.getProductId());
            opList.add(op);
        }

        // Use this for update
        // List<OrderProduct> opList = orderProductService.findAllByOrderId(order.getOrderId());
        ope.setOpList(opList);
        ope.setSaveReturned("No");

        model.addAttribute("opeForm", ope);
        model.addAttribute("customerId", customerId);
        //model.addAttribute("newDrNumber", orderService.getNewDrNumber());


        //List<CustomerPrice> customerPriceList = customerPriceService.findAllByCustomerId(customerId);

        Customer customer = this.customerService.view(customerId);
        model.addAttribute("customerName", customer.getName());
        model.addAttribute("customerAddress", customer.getAddress());
        model.addAttribute("saveReturnedAnswers",new String[]{"Yes","No"});
        return "new-order";
    }

    //	@RequestMapping(value = "/neworder", method = RequestMethod.GET)
    //	public String newOrder(@RequestParam String customerId, Model model) {
    //		logger.debug("New Order form for customer: " + customerId);
    //		Order order = new Order();
    //		order.setCustomerId(customerId);
    //		model.addAttribute("orderForm", order);
    //		model.addAttribute("customerId", customerId);
    //		model.addAttribute("newDrNumber", orderService.getNewDrNumber());
    //
    //		List<Product> productList = productService.findAll();
    //		model.addAttribute("products", productList);
    //
    //		//List<CustomerPrice> customerPriceList = customerPriceService.findAllByCustomerId(customerId);
    //
    //		Customer customer = customerService.view(customerId);
    //		model.addAttribute("customerName", customer.getName());
    //		return "new-order";
    //	}

    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public String editOrder(Model model, long orderId) {
        logger.debug("Edit Order form.");

        OrderProductEntity ope = new OrderProductEntity();
        Order order = this.orderService.findOrderByOrderId(orderId);

        //        if (order.getRoundReturned().equalsIgnoreCase("0")) {
        //            order.setRoundReturned(null);
        //        }
        //        if (order.getSlimReturned().equalsIgnoreCase("0")) {
        //            order.setSlimReturned(null);
        //        }

        ope.setOrder(order);

        Customer customer = this.customerService.view(order.getCustomerId());
        model.addAttribute("customerName", customer.getName());
        model.addAttribute("customerAddress", customer.getAddress());

        List<Product> productList = this.productService.findAll();
        List<CustomerPrice> cpList = this.customerPriceService.findAllByCustomerId(order.getCustomerId());

        List<Product> overriddenProductList = new ArrayList<>();
        boolean overridden = false;

        for (Product p : productList) {

            overridden = false;

            for (CustomerPrice cp : cpList) {
                if (p.getProductId() == cp.getProductId()) {
                    Product overridenProduct = withCustomerPrice(p, cp);
                    overriddenProductList.add(overridenProduct);
                    overridden = true;
                    break;
                }
            }

            if (!overridden) {
                overriddenProductList.add(p);
            }
        }

        if (overriddenProductList.size() > 0) {
            model.addAttribute("productList", overriddenProductList);
        } else {
            model.addAttribute("productList", productList);
        }

        List<OrderProduct> opList = this.orderProductService.findAllByOrderId(orderId);

        List<OrderProduct> opListForDisplay = new ArrayList<>();

        for (Product p : productList) {

            boolean hasMatch = false;

            for (OrderProduct op : opList) {
                if (p.getProductId() == op.getProductId()) {
                    opListForDisplay.add(op);
                    hasMatch = true;
                    break;
                }
            }

            if (!hasMatch) {
                OrderProduct op = new OrderProduct();
                op.setOrderId(order.getOrderId());
                op.setProductId(p.getProductId());
                opListForDisplay.add(op);
            }
        }

        ope.setOpList(opListForDisplay);
        model.addAttribute("opeForm", ope);
        model.addAttribute("drNumber", order.getDeliveryReceiptNum());
        model.addAttribute("createdAt", order.getCreatedAt());
        model.addAttribute("status", order.getStatus());
        //model.addAttribute("orderForm", order);
        return "edit-order";
    }

    //	@RequestMapping(value = "/edit", method = RequestMethod.GET)
    //    public String editOrder(Model model, String orderId) {
    //        logger.debug("Edit Order form.");
    //
    //        //Customer customer = customerService.view(orderId);
    //        Order order = orderService.findOrderByOrderId(orderId);
    //        Customer customer = customerService.view(order.getCustomerId());
    //		model.addAttribute("customerName", customer.getName());
    //		model.addAttribute("drNumber", order.getDeliveryReceiptNum());
    //        model.addAttribute("orderForm", order);
    //        return "edit-order";
    //    }

    @RequestMapping(value = "/daily", method = RequestMethod.GET)
    public String dailySales(Model model, @RequestParam(value="d", defaultValue="today", required=false) String d,
            @RequestParam(required = false) String from, @RequestParam(required = false) String to,
            @RequestParam(value="mode", defaultValue="normal", required=false) String mode ) {
        logger.info("Daily sales report.");

        // One day (?d=yyyy-MM-dd, today by default) or a range of days (?from=&to=, both included).
        // A day is a one-day range: every record is matched by its calendar day, whatever its time.
        LocalDate first;
        LocalDate last;
        if (from != null && !from.isEmpty() && to != null && !to.isEmpty()) {
            first = LocalDate.parse(from);
            last = LocalDate.parse(to);
            if (last.isBefore(first)) {
                LocalDate swap = first;
                first = last;
                last = swap;
            }
        } else {
            first = "today".equalsIgnoreCase(d) ? LocalDate.now() : LocalDate.parse(d);
            last = first;
        }
        boolean oneDay = first.equals(last);
        Date firstDate = Date.valueOf(first);
        Date lastDate = Date.valueOf(last);
        SimpleDateFormat label = new SimpleDateFormat("MMM dd yyyy");
        String datePicked = oneDay ? label.format(firstDate) : label.format(firstDate) + " – " + label.format(lastDate);
        model.addAttribute("datePicked", datePicked);
        model.addAttribute("dateParam", d);
        model.addAttribute("from", first.toString());
        model.addAttribute("to", last.toString());
        // In a range each row also says which day it was.
        SimpleDateFormat when = new SimpleDateFormat(oneDay ? "h:mm a" : "MMM d, h:mm a");

        // The customers' names in one query, not one per row.
        Map<Long, String> names = new HashMap<>();
        for (Customer customer : this.customerService.findAll()) {
            names.put(customer.getCustomerId(), customer.getName());
        }

        // The totals follow the rules the Monthly Summary uses too (SalesTotals).
        SalesTotals totals = new SalesTotals();
        List<DailySalesEntity> dseList = new ArrayList<>();

        for (Order o : this.orderService.findAllOrdersBetween(firstDate, lastDate)) {
            if (totals.addOrder(o)) {
                DailySalesEntity dse = new DailySalesEntity();
                dse.setOrder(o);
                dse.setCustomerName(names.get(o.getCustomerId()));
                dse.setPaidCash(o.getAmountPaid());
                dse.setBalanceAmount(Double.parseDouble(df2.format(o.getTotalAmount() - o.getAmountPaid())));
                dse.setReturnedRound(o.getRoundReturned());
                dse.setReturnedSlim(o.getSlimReturned());
                dse.setRemarks(o.getRemarks());
                dse.setDateAndTime(when.format(o.getCreatedAt()));
                dseList.add(dse);
            }
        }

        for (Expense e : this.expenseDao.findAllBetween(firstDate, lastDate)) {
            totals.addExpense(e);
            DailySalesEntity dse = new DailySalesEntity();
            dse.setRemarks(e.getDescription());
            dse.setExpensePaymentType(e.getPaymentType() == null ? null : e.getPaymentType().getLabel());
            dse.setExpenseAmount(Double.parseDouble(df2.format(e.getAmount())));
            dseList.add(dse);
        }

        for (Payment p : this.paymentDao.findAllBetween(firstDate, lastDate)) {
            totals.addPayment(p);
            DailySalesEntity dse = new DailySalesEntity();
            dse.setCustomerName(names.get(p.getCustomerId()));
            dse.setRemarks(p.getRemarks());
            Double amount = Double.parseDouble(df2.format(p.getAmount()));
            switch (p.getPaymentType() == null ? "" : p.getPaymentType().toUpperCase()) {
                case PaymentTypes.CASH -> dse.setPaidCash(amount);
                case PaymentTypes.GCASH -> dse.setPaidGcash(amount);
                case PaymentTypes.BANK_TRANSFER -> dse.setPaidBankTransfer(amount);
                // Checks, and anything older that wasn't cash, as the report always counted them.
                default -> dse.setPaidCheck(amount);
            }
            dseList.add(dse);
        }

        for (Container c : this.containerDao.findContainerActivityBetween(firstDate, lastDate)) {
            if (totals.addContainerReturn(c)) {
                DailySalesEntity dse = new DailySalesEntity();
                dse.setCustomerName(names.get(c.getCustomerId()));
                dse.setRemarks("Returned containers.");
                dse.setReturnedRound(Integer.valueOf(c.getRoundCount()).toString());
                dse.setReturnedSlim(Integer.valueOf(c.getSlimCount()).toString());
                dseList.add(dse);
            }
        }

        model.addAttribute("dailySales", dseList);
        model.addAllAttributes(totals.attributes());

        if (mode.equalsIgnoreCase("preview")) {
            Collections.sort(dseList,
                    (DailySalesEntity dse1, DailySalesEntity dse2) -> {
                        Order o1 = dse1.getOrder();
                        Order o2 = dse2.getOrder();
                        String dr1 = o1 == null ? "" : o1.getDeliveryReceiptNum();
                        String dr2 = o2 == null ? "" : o2.getDeliveryReceiptNum();
                        return dr1.compareTo(dr2);
                    } );
            return "daily-sales-print-preview";
        } else {
            return "daily-sales";
        }
    }

    /**
     * The month day by day, on the same rules as Daily Sales (SalesTotals): each day's row equals
     * that day's Daily Sales, and the month's totals equal Daily Sales for the whole month.
     * ?d=MM-yyyy, this month by default.
     */
    @RequestMapping(value = "/monthly", method = RequestMethod.GET)
    public String monthlyTotals(Model model, @RequestParam(value = "d", required = false) String datePicked) {
        YearMonth month = datePicked == null || datePicked.isBlank() || "today".equalsIgnoreCase(datePicked)
                ? YearMonth.now()
                : YearMonth.parse(datePicked, DateTimeFormatter.ofPattern("MM-yyyy"));
        Date firstDate = Date.valueOf(month.atDay(1));
        Date lastDate = Date.valueOf(month.atEndOfMonth());

        SalesTotals monthTotals = new SalesTotals();
        Map<LocalDate, SalesTotals> byDay = new LinkedHashMap<>();
        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            byDay.put(month.atDay(day), new SalesTotals());
        }

        // One query each for the month, then sorted into days.
        for (Order o : this.orderService.findAllOrdersBetween(firstDate, lastDate)) {
            if (monthTotals.addOrder(o)) {
                byDay.get(o.getCreatedAt().toLocalDateTime().toLocalDate()).addOrder(o);
            }
        }
        for (Payment p : this.paymentDao.findAllBetween(firstDate, lastDate)) {
            monthTotals.addPayment(p);
            byDay.get(p.getCreatedAt().toLocalDate()).addPayment(p);
        }
        for (Expense e : this.expenseDao.findAllBetween(firstDate, lastDate)) {
            monthTotals.addExpense(e);
            byDay.get(e.getCreatedAt().toLocalDate()).addExpense(e);
        }
        for (Container c : this.containerDao.findContainerActivityBetween(firstDate, lastDate)) {
            if (monthTotals.addContainerReturn(c)) {
                byDay.get(c.getCreatedAt().toLocalDateTime().toLocalDate()).addContainerReturn(c);
            }
        }

        List<Map<String, Object>> days = new ArrayList<>();
        byDay.forEach((date, totals) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", date.toString());
            row.putAll(totals.attributes());
            days.add(row);
        });

        model.addAttribute("days", days);
        model.addAllAttributes(monthTotals.attributes());
        model.addAttribute("monthYear", monthsStr[month.getMonthValue() - 1] + " " + month.getYear());
        model.addAttribute("monthParam", month.format(DateTimeFormatter.ofPattern("MM-yyyy")));
        model.addAttribute("from", month.atDay(1).toString());
        model.addAttribute("to", month.atEndOfMonth().toString());
        return "monthly-totals";
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String listOrders(@RequestParam(required = false) String d,
            @RequestParam(required = false) String from, @RequestParam(required = false) String to, Model model) {
        logger.info("List all orders.");

        // One day (?d=yyyy-MM-dd, today by default) or a range of days (?from=&to=, both included).
        LocalDate first;
        LocalDate last;
        if (from != null && !from.isEmpty() && to != null && !to.isEmpty()) {
            first = LocalDate.parse(from);
            last = LocalDate.parse(to);
            if (last.isBefore(first)) {
                LocalDate swap = first;
                first = last;
                last = swap;
            }
        } else {
            first = d == null || d.isEmpty() ? LocalDate.now() : LocalDate.parse(d);
            last = first;
        }
        List<Order> orderList = this.orderService.findAllOrdersBetween(Date.valueOf(first), Date.valueOf(last));

        // One query for the customers' names, not one per order: a range can hold many orders.
        Map<Long, String> names = new HashMap<>();
        for (Customer customer : this.customerService.findAll()) {
            names.put(customer.getCustomerId(), customer.getName());
        }

        int pendingCount = 0;
        List<OrderCustomerEntity> orderCustomerEntityList = new ArrayList<>();
        for (Order order : orderList) {
            if (order.getStatus().equalsIgnoreCase("pending")) {
                pendingCount++;
            }
            OrderCustomerEntity oce = new OrderCustomerEntity();
            oce.setOrder(order);
            oce.setCustomerName(names.get(order.getCustomerId()));
            String formattedDate = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a").format(order.getCreatedAt());
            oce.setFormattedDate(formattedDate);
            orderCustomerEntityList.add(oce);
        }
        model.addAttribute("orders", orderCustomerEntityList);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("from", first.toString());
        model.addAttribute("to", last.toString());

        SimpleDateFormat label = new SimpleDateFormat("MMM dd yyyy");
        String datePicked = first.equals(last)
                ? label.format(Date.valueOf(first))
                : label.format(Date.valueOf(first)) + " – " + label.format(Date.valueOf(last));
        model.addAttribute("datePicked", datePicked);

        return "list-orders";
    }

    @RequestMapping(value = "/pending", method = RequestMethod.GET)
    public String listPendingOrders(Model model) {
        logger.info("List all pending orders.");

        int pendingCount = 0;

        List<Order> orderList = new ArrayList<>();
        List<OrderCustomerEntity> orderCustomerEntityList = new ArrayList<>();

        orderList = this.orderService.findAll();

        for (Order order : orderList) {

            if (order.getStatus().equalsIgnoreCase("pending")) {
                pendingCount++;

                OrderCustomerEntity oce = new OrderCustomerEntity();
                oce.setOrder(order);
                Customer customer = this.customerService.view(order.getCustomerId());
                oce.setCustomerName(customer.getName());
                String formattedDate = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a").format(order.getCreatedAt());
                oce.setFormattedDate(formattedDate);
                orderCustomerEntityList.add(oce);
            }
        }
        model.addAttribute("orders", orderCustomerEntityList);
        model.addAttribute("pendingCount", pendingCount);

        return "list-pending-orders";
    }

    @RequestMapping("/{customerId}")
    public List<Order> findAllOrdersByCustomerId(@PathVariable String customerId) {
        List<Order> orders = new ArrayList<>();

        return orders;
    }

    @RequestMapping("/{customerId}/new")
    public Order saveOrderByCustomer(Order order) {
        return order;
    }

    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public String cancelOrder(Order order, BindingResult result, @RequestParam long orderId,
            final RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "list-orders";
        }

        order = this.orderService.findOrderByOrderId(orderId);
        // Only pending orders can be cancelled (the order list disables Cancel for the others),
        // so a repeated request can't undo the container balance twice.
        if (!"Pending".equalsIgnoreCase(order.getStatus())) {
            redirectAttributes.addFlashAttribute("msg", "Only pending orders can be cancelled.");
            return "redirect:/orders/list";
        }

        this.orderService.cancelOrder(orderId);

        // Take back the containers this order added to the customer's balance when it was created.
        Customer customer = this.customerService.view(order.getCustomerId());
        customer.setTotalRound(customer.getTotalRound() - order.getRoundRefillOnlyCount() + Integer.parseInt(order.getRoundReturned()));
        customer.setTotalSlim(customer.getTotalSlim() - order.getSlimRefillOnlyCount() + Integer.parseInt(order.getSlimReturned()));
        this.customerService.update(customer);

        redirectAttributes.addFlashAttribute("msg", "Order has been cancelled.");
        return "redirect:/orders/list";
    }

    @RequestMapping(value = "/deliver", method = RequestMethod.POST)
    public String deliverOrder(Order order, BindingResult result, @RequestParam long orderId,
            final RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "list-orders";
        } else {

            order = this.orderService.findOrderByOrderId(orderId);
            this.saveDebt(order.getAmountPaid(), order.getTotalAmount(), order.getCustomerId(), order.getOrderId(), Date.valueOf(LocalDate.now()), order.getOrderId());

            // An order edited while pending already has container activity: replace it, don't add to it.
            this.deleteOrderContainerActivity(order.getCustomerId(), order.getOrderId());

            int slimOutGoingCount = order.getSlimRefillOnlyCount() + order.getSlimFreeCount();
            int roundOutGoingCount = order.getRoundRefillOnlyCount() + order.getRoundFreeCount();

            int slimBuyCount = order.getSlimContainerOnlyCount() + order.getSlimRefillWithContainerCount();
            int roundBuyCount = order.getRoundContainerOnlyCount() + order.getRoundRefillWithContainerCount();

            this.saveContainerActivity(slimOutGoingCount, roundOutGoingCount, slimBuyCount, roundBuyCount,
                    Integer.parseInt(order.getSlimReturned()), Integer.parseInt(order.getRoundReturned()),
                    order.getCustomerId(), order.getOrderId(), true);

            this.orderService.setToDelivered(orderId);
            redirectAttributes.addFlashAttribute("msg", "Order has been set to delivered.");
        }

        return "redirect:/orders/list";
    }

    @RequestMapping(value = "/deliverPending", method = RequestMethod.POST)
    public String deliverPendingOrder(Order order, BindingResult result, @RequestParam long orderId,
            final RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "list-orders";
        } else {

            order = this.orderService.findOrderByOrderId(orderId);
            this.saveDebt(order.getAmountPaid(), order.getTotalAmount(), order.getCustomerId(), order.getOrderId(), Date.valueOf(LocalDate.now()), order.getOrderId());

            // An order edited while pending already has container activity: replace it, don't add to it.
            this.deleteOrderContainerActivity(order.getCustomerId(), order.getOrderId());

            int slimOutGoingCount = order.getSlimRefillOnlyCount() + order.getSlimFreeCount();
            int roundOutGoingCount = order.getRoundRefillOnlyCount() + order.getRoundFreeCount();

            int slimBuyCount = order.getSlimContainerOnlyCount() + order.getSlimRefillWithContainerCount();
            int roundBuyCount = order.getRoundContainerOnlyCount() + order.getRoundRefillWithContainerCount();

            this.saveContainerActivity(slimOutGoingCount, roundOutGoingCount, slimBuyCount, roundBuyCount,
                    Integer.parseInt(order.getSlimReturned()), Integer.parseInt(order.getRoundReturned()),
                    order.getCustomerId(), order.getOrderId(), true);

            this.orderService.setToDelivered(orderId);
            redirectAttributes.addFlashAttribute("msg", "Order has been set to delivered.");
        }

        return "redirect:/orders/pending";
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveOrderProductEntity(@ModelAttribute("opeForm") @Validated OrderProductEntity orderProductEntity,
            BindingResult result, Model model,
            final RedirectAttributes redirectAttributes) {
        logger.debug("Save order.");

        Order order = orderProductEntity.getOrder();
        if (order.getRoundReturned().equalsIgnoreCase("")) {
            order.setRoundReturned("0");
        }

        if (order.getSlimReturned().equalsIgnoreCase("")) {
            order.setSlimReturned("0");
        }

        if (result.hasErrors()) {
            //model.addAttribute("customerId", order.getCustomerId());
            //model.addAttribute("newDrNumber", orderService.getNewDrNumber());

            //Customer customer = customerService.view(order.getCustomerId());
            //model.addAttribute("customerName", customer.getName());
            return "ErrorPage";
        } else {
            // Add message to flash scope
            redirectAttributes.addFlashAttribute("css", "success");
            //order.setDeliveryReceiptNum(orderService.getNewDrNumber());
            order.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            order.setStatus("Pending");

            Order insertedOrder = this.orderService.insert(order);

            // Update some customer details including container balances
            Customer customer = this.customerService.view(order.getCustomerId());
            int currentTotalRound = customer.getTotalRound();
            int currentTotalSlim = customer.getTotalSlim();

            currentTotalRound = currentTotalRound + insertedOrder.getRoundRefillOnlyCount() - Integer.parseInt(insertedOrder.getRoundReturned());
            currentTotalSlim = currentTotalSlim + insertedOrder.getSlimRefillOnlyCount() - Integer.parseInt(insertedOrder.getSlimReturned());

            customer.setTotalRound(currentTotalRound);
            customer.setTotalSlim(currentTotalSlim);

            if (orderProductEntity.getOrderInterval() > 0) {
                customer.setOrderInterval(orderProductEntity.getOrderInterval());
            }

            this.customerService.update(customer);

            /*if (orderProductEntity.getSaveReturned().equalsIgnoreCase("Yes")) {
				saveReturnedContainers(Integer.parseInt(order.getSlimReturned()), Integer.parseInt(order.getRoundReturned()),
						order.getCustomerId(), order.getOrderId());
			}*/

            List<OrderProduct> orderProductEntityList = orderProductEntity.getOpList();

            if (orderProductEntityList != null && orderProductEntityList.size() != 0) {
                for (OrderProduct op : orderProductEntityList) {
                    if (op.getQuantity().equalsIgnoreCase("") || op.getQuantity().equalsIgnoreCase("0")) {
                        // Do not save
                    } else {
                        op.setOrderId(insertedOrder.getOrderId());
                        this.orderProductService.insert(op);
                    }
                }
            }

            redirectAttributes.addFlashAttribute("msg", "Order created successfully!");

            // POST/REDIRECT/GET
            return "redirect:/orders/list";
            //return "redirect:/customers/" + customer.getCustomerId();
        }
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public String updateOrderProductEntity(@ModelAttribute("opeForm") @Validated OrderProductEntity orderProductEntity,
            BindingResult result, Model model,
            final RedirectAttributes redirectAttributes) {
        logger.debug("Update order.");

        Order order = orderProductEntity.getOrder();
        long oldOrderId = order.getOrderId();

        Order oldOrder = this.orderService.findOrderByOrderId(oldOrderId);
        int roundBorrowedCountInitial = oldOrder.getRoundRefillOnlyCount();
        int slimBorrowedCountInitial = oldOrder.getSlimRefillOnlyCount();
        int roundReturnedInitial = Integer.parseInt(oldOrder.getRoundReturned());
        int slimReturnedInitial = Integer.parseInt(oldOrder.getSlimReturned());

        if (order.getRoundReturned().equalsIgnoreCase("")) {
            order.setRoundReturned("0");
        }

        if (order.getSlimReturned().equalsIgnoreCase("")) {
            order.setSlimReturned("0");
        }

        if (result.hasErrors()) {
            //model.addAttribute("customerId", order.getCustomerId());
            //model.addAttribute("drNumber", orderService.getNewDrNumber());
            //Customer customer = customerService.view(order.getCustomerId());
            //model.addAttribute("customerName", customer.getName());
            return "ErrorPage";
        } else {
            // Add message to flash scope
            redirectAttributes.addFlashAttribute("css", "success");
            order.setDeliveryReceiptNum(order.getDeliveryReceiptNum());

            this.orderService.delete(order);
            // Re-insert as a new row with a new id (Hibernate 7 rejects saving a deleted id).
            order.setOrderId(0);
            Order updatedOrder = this.orderService.insert(order);
            orderProductEntity.setOrder(updatedOrder);
            //Order updatedOrder = orderService.update(order);

            for (OrderProduct op : orderProductEntity.getOpList()) {
                if (op.getQuantity().equalsIgnoreCase("")) {
                    // Do not save
                } else {
                    op.setOrderId(updatedOrder.getOrderId());
                    if (op.getOrderProductId() == 0) {
                        System.out.println("Saving new OP");
                        //op.setProductId(productId);
                        if (!op.getQuantity().equalsIgnoreCase("0")) {
                            this.orderProductService.insert(op);
                        }

                    } else {
                        System.out.println("Updating/Delete existing OP");

                        if (op.getQuantity().equalsIgnoreCase("0")) {
                            this.orderProductService.remove(op);
                        } else {
                            this.orderProductService.remove(op); //OR REMOVE AND INSERT ??
                            op.setOrderProductId(0);
                            this.orderProductService.insert(op);
                        }
                    }
                }
            }

            int slimOutGoingCount = order.getSlimRefillOnlyCount() + order.getSlimFreeCount();
            int roundOutGoingCount = order.getRoundRefillOnlyCount() + order.getRoundFreeCount();

            int slimBuyCount = order.getSlimContainerOnlyCount() + order.getSlimRefillWithContainerCount();
            int roundBuyCount = order.getRoundContainerOnlyCount() + order.getRoundRefillWithContainerCount();

            Timestamp orderCreationDate = order.getCreatedAt();

            this.updateContainerActivity(slimOutGoingCount, roundOutGoingCount, slimBuyCount, roundBuyCount,
                    Integer.parseInt(updatedOrder.getSlimReturned()), Integer.parseInt(updatedOrder.getRoundReturned()),
                    updatedOrder.getCustomerId(), oldOrderId, updatedOrder.getOrderId(), orderCreationDate);
            Date orderCreationDateConverted = new Date(orderCreationDate.getTime());

            this.saveDebt(order.getAmountPaid(), order.getTotalAmount(), order.getCustomerId(), oldOrderId, orderCreationDateConverted, updatedOrder.getOrderId());
            System.out.println("Deleting debt with order id: " + oldOrderId);

            // Update some customer details including container balances
            Customer customer = this.customerService.view(order.getCustomerId());
            int currentTotalRound = customer.getTotalRound();
            int currentTotalSlim = customer.getTotalSlim();

            currentTotalRound = currentTotalRound - roundBorrowedCountInitial + order.getRoundRefillOnlyCount() + roundReturnedInitial - Integer.parseInt(order.getRoundReturned());
            currentTotalSlim = currentTotalSlim - slimBorrowedCountInitial + order.getSlimRefillOnlyCount() + slimReturnedInitial - Integer.parseInt(order.getSlimReturned());

            customer.setTotalRound(currentTotalRound);
            customer.setTotalSlim(currentTotalSlim);

            this.customerService.update(customer);

            redirectAttributes.addFlashAttribute("msg", "Order edited successfully!");

            // POST/REDIRECT/GET
            return "redirect:/orders/list";
            //return "redirect:/customers/" + customer.getCustomerId();
        }
    }

    /**
     * Saves the debt of the customer if amount paid is less than total amount for a given order.
     * 
     * @param amountPaid
     * @param totalAmount
     * @param customerId
     */
    public void saveDebt(double amountPaid, double totalAmount, long customerId, long prevOrderId, Date debtDate,
            long newOrderId) {

        double deficit = totalAmount - amountPaid;

        if (deficit != 0.0) {
            Debt debtEntry = new Debt();
            debtEntry.setCustomerId(customerId);
            debtEntry.setAmount(deficit);

            if (amountPaid == 0) {
                debtEntry.setRemarks("Total amount is: Php" + totalAmount
                        + " but the customer hasn't paid yet. PreviousOrderId: " + prevOrderId);
            } else {
                debtEntry.setRemarks("Total amount is: Php" + totalAmount
                        + " but the amount paid is only Php" + amountPaid + ". PreviousOrderId: " + prevOrderId);
            }

            debtEntry.setCreatedAt(Date.valueOf(LocalDate.now()));
            //debtEntry.setOrderId(prevOrderId);
            //this.debtService.delete(debtEntry);
            debtEntry.setOrderId(newOrderId);
            this.debtService.insert(debtEntry, prevOrderId);
        } else {
            Debt debtEntry = new Debt();
            debtEntry.setCustomerId(customerId);
            debtEntry.setOrderId(newOrderId);
            this.debtService.insert(debtEntry, prevOrderId);
        }
    }

    /**
     * Saves the returned and borrowed containers into the Container table.
     * 
     * @param slimCount
     * @param roundCount
     * @param slimReturned
     * @param roundReturned
     * @param customerId
     */
    public void saveContainerActivity(int slimBorrow, int roundBorrow, int slimBuy, int roundBuy, int slimReturned, int roundReturned,
            long customerId, long orderId, boolean saveReturnedContainers) {

        Container containerActivity = new Container();
        containerActivity.setCustomerId(customerId);
        containerActivity.setOrderId(orderId);
        containerActivity.setRoundCount(roundBorrow);
        containerActivity.setSlimCount(slimBorrow);
        containerActivity.setStatus("B"); // Borrow
        containerActivity.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        this.containerService.insert(containerActivity);

        // Buy Container - code is BC
        containerActivity = new Container();
        containerActivity.setCustomerId(customerId);
        containerActivity.setOrderId(orderId);
        containerActivity.setRoundCount(roundBuy);
        containerActivity.setSlimCount(slimBuy);
        containerActivity.setStatus("BC"); // Buy Container
        containerActivity.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        this.containerService.insert(containerActivity);

        if (saveReturnedContainers && (slimReturned != 0 || roundReturned != 0)) {
            containerActivity = new Container();
            containerActivity.setCustomerId(customerId);
            containerActivity.setOrderId(orderId);
            containerActivity.setRoundCount(roundReturned);
            containerActivity.setSlimCount(slimReturned);
            containerActivity.setStatus("RO"); // Return with Order
            containerActivity.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            this.containerService.insert(containerActivity);
        }
    }

    public void updateContainerActivity(int slimBorrow, int roundBorrow, int slimBuy, int roundBuy, int slimReturned, int roundReturned,
            long customerId, long orderId, long newOrderId, Timestamp containerActivityDate) {

        // The edited order is saved under a new id: move its activity there instead of copying it.
        this.deleteOrderContainerActivity(customerId, orderId);

        Container containerActivity = new Container();
        containerActivity.setCustomerId(customerId);
        containerActivity.setOrderId(newOrderId);
        containerActivity.setRoundCount(roundBorrow);
        containerActivity.setSlimCount(slimBorrow);
        containerActivity.setStatus("B");
        containerActivity.setCreatedAt(containerActivityDate);
        this.containerService.insert(containerActivity);

        // Buy Container - code is BC
        containerActivity = new Container();
        containerActivity.setCustomerId(customerId);
        containerActivity.setOrderId(newOrderId);
        containerActivity.setRoundCount(roundBuy);
        containerActivity.setSlimCount(slimBuy);
        containerActivity.setStatus("BC"); // Buy Container
        containerActivity.setCreatedAt(containerActivityDate);
        this.containerService.insert(containerActivity);

        if ((slimReturned != 0 || roundReturned != 0)) {
            containerActivity = new Container();
            containerActivity.setCustomerId(customerId);
            containerActivity.setOrderId(newOrderId);
            containerActivity.setRoundCount(roundReturned);
            containerActivity.setSlimCount(slimReturned);
            containerActivity.setStatus("RO"); // Return with Order
            containerActivity.setCreatedAt(containerActivityDate);
            this.containerService.insert(containerActivity);
        }
    }

    /**
     * The product as this customer pays for it, for the order form. A copy: changing the price on the
     * product itself would be saved by Hibernate and change the list price for every customer.
     */
    private static Product withCustomerPrice(Product product, CustomerPrice customerPrice) {
        Product shown = new Product();
        BeanUtils.copyProperties(product, shown);
        shown.setSellingPrice(customerPrice.getSellingPrice());
        return shown;
    }

    /**
     * Deletes the borrow (B), buy (BC) and return-with-order (RO) rows recorded for an order.
     * Containers returned on their own (R) have no order and are never touched.
     */
    private void deleteOrderContainerActivity(long customerId, long orderId) {
        for (Container activity : this.containerService.findAllByCustomerIdAndOrderId(customerId, orderId)) {
            if (ORDER_CONTAINER_ACTIVITY.contains(activity.getStatus())) {
                this.containerService.delete(activity);
            }
        }
    }

    /**
     * 
     * @param slimReturned
     * @param roundReturned
     * @param customerId
     * @param orderId
     */
    public void saveReturnedContainers(int slimReturned, int roundReturned, long customerId, long orderId) {

        if (slimReturned != 0 || roundReturned != 0) {
            Container containerActivity = new Container();
            containerActivity.setCustomerId(customerId);
            containerActivity.setOrderId(orderId);
            containerActivity.setRoundCount(roundReturned);
            containerActivity.setSlimCount(slimReturned);
            containerActivity.setStatus("RO"); // Return with Order
            containerActivity.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            this.containerService.insert(containerActivity);
        }
    }

}
