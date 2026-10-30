package com.spring.aurora.controller;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.spring.aurora.model.Product;
import com.spring.aurora.service.ProductService;
import com.spring.aurora.util.ProductFormValidator;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    ProductFormValidator productFormValidator;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String listAll(Model model) {
        List<Product> products = this.productService.findAll();
        model.addAttribute("products", products);
        return "list-products";
    }

    @RequestMapping(value = "/list/json",
            method = RequestMethod.GET,
            produces = "application/json",
            consumes = "application/json")
    public @ResponseBody List<Product> listAllJSON(){
        return this.productService.findAll();
    }

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String addNewProductForm(Model model){
        Product product = new Product();
        model.addAttribute("product", product);
        return "new-product";
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveNewProductForm(@ModelAttribute("product") Product product,
            BindingResult result, Model model,
            final RedirectAttributes redirect){
        this.productFormValidator.validate(product, result);
        if (result.hasErrors()) {
            return "new-product";
        }
        product.setInitialPrice(product.getSellingPrice());
        product.setUpdatedAt(Date.valueOf(LocalDate.now()));
        if (product.getProductId() == 0) {
            product.setCreatedAt(Date.valueOf(LocalDate.now()));
            // product.setProductId(null);
            redirect.addFlashAttribute("msg", "Product added successfully!");
        } else {
            redirect.addFlashAttribute("msg", "Product updated successfully!");
        }
        this.productService.save(product);
        return "redirect:/products/list";
    }

    @RequestMapping(value = "/edit/{productId}", method = RequestMethod.GET)
    public String editProductForm(@PathVariable long productId, Model model) {
        Product product = this.productService.findByProductId(productId);
        model.addAttribute("product", product);
        return "edit-product";
    }
}
