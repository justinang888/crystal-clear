package com.spring.aurora.controller;

import static java.util.stream.Collectors.toList;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.spring.aurora.entity.ProductPriceEntity;
import com.spring.aurora.model.Customer;
import com.spring.aurora.model.CustomerPrice;
import com.spring.aurora.model.Product;
import com.spring.aurora.service.CustomerPriceService;
import com.spring.aurora.service.CustomerService;
import com.spring.aurora.service.ProductService;
import com.spring.aurora.util.CustomerPriceFormValidator;

@Controller
@RequestMapping(value = "/prices")
public class CustomerPriceController {
    @Autowired
    private CustomerPriceService customerPriceService;
    @Autowired
    private ProductService productService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    CustomerPriceFormValidator customerPriceFormValidator;

    @RequestMapping(value = "/list/{customerId}", method = RequestMethod.GET)
    public String newCustomerPrice(@PathVariable long customerId, Model model) {
        CustomerPrice price = new CustomerPrice();
        price.setCustomerId(customerId);
        Customer customer = this.customerService.view(customerId);
        List<Product> products = this.productService.findAll();
        List<CustomerPrice> prices = this.customerPriceService.findAllByCustomerId(customerId);
        List<ProductPriceEntity> productPrices = prices.stream().map((CustomerPrice p) -> new ProductPriceEntity(
                p.getPriceId(),
                this.customerService.view(p.getCustomerId()),
                this.productService.findByProductId(p.getProductId()),
                p.getSellingPrice()
        )).collect(toList());
        model.addAttribute("price", price);
        model.addAttribute("customer", customer);
        model.addAttribute("products", products);
        model.addAttribute("prices", productPrices);
        return "new-price";
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveCustomerPrice(@ModelAttribute("price") CustomerPrice price,
            BindingResult result, Model model,
            final RedirectAttributes redirect){
        this.customerPriceFormValidator.validate(price, result);
        if (result.hasErrors()) {
            Customer customer = this.customerService.view(price.getCustomerId());
            List<Product> products = this.productService.findAll();
            List<CustomerPrice> prices = this.customerPriceService.findAllByCustomerId(price.getCustomerId());
            List<ProductPriceEntity> productPrices = prices.stream().map((CustomerPrice p) -> new ProductPriceEntity(
                    p.getPriceId(),
                    this.customerService.view(p.getCustomerId()),
                    this.productService.findByProductId(p.getProductId()),
                    p.getSellingPrice()
                    )).collect(Collectors.toList());
            model.addAttribute("customer", customer);
            model.addAttribute("products", products);
            model.addAttribute("prices", productPrices);
            return "new-price";
        } else {
            redirect.addFlashAttribute("msg", "Product added successfully!");
        }

        this.customerPriceService.deleteCustomerPrice(price.getCustomerId(), price.getProductId());

        price.setCreatedAt(Date.valueOf(LocalDate.now()));
        price.setUpdatedAt(Date.valueOf(LocalDate.now()));
        this.customerPriceService.saveOrUpdate(price);

        return "redirect:/prices/list/" + price.getCustomerId();
    }
}
