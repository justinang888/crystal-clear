package com.spring.aurora.service;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.aurora.dao.ProductDao;
import com.spring.aurora.model.Product;

@Service("productService")
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDao productDao;

    @Override
    public Product save(Product product) {
        return this.productDao.save(product);
    }

    @Override
    public List<Product> findAll() {
        List<Product> result = new ArrayList<>();
        this.productDao.findAll().forEach(result::add);
        return result;
    }

    @Override
    public Product findByProductId(long productId) {
        return this.productDao.findById(productId).orElseGet(Product::new);
    }
}
