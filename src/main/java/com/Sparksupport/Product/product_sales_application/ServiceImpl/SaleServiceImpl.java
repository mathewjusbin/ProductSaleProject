package com.sparksupport.product.product_sales_application.serviceImpl;

import com.sparksupport.product.product_sales_application.exception.ProductNotFoundException;
import com.sparksupport.product.product_sales_application.model.Product;
import com.sparksupport.product.product_sales_application.model.Sale;
import com.sparksupport.product.product_sales_application.repository.ProductRepository;
import com.sparksupport.product.product_sales_application.repository.SaleRepository;
import com.sparksupport.product.product_sales_application.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaleServiceImpl implements SaleService {

    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;

    @Autowired
    public SaleServiceImpl(ProductRepository productRepository, SaleRepository saleRepository) {
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
    }

    @Override
    @Transactional
    public Sale addSales(Integer productId, Sale sale) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Check if we have enough quantity in stock
        if (existingProduct.getQuantity() < sale.getQuantity()) {
            throw new RuntimeException("Insufficient stock. Available: " + existingProduct.getQuantity()
                + ", Requested: " + sale.getQuantity());
        }

        // Reduce the quantity from product inventory
        if (!existingProduct.reduceQuantity(sale.getQuantity())) {
            throw new RuntimeException("Failed to reduce product quantity");
        }

        // Save the updated product with reduced quantity
        productRepository.save(existingProduct);

        sale.setProductId(productId);
        return saleRepository.save(sale);
    }

    @Override
    public Boolean updateSales(Integer saleId, Sale sale) {
        return Boolean.TRUE;
    }

    @Override
    public Boolean deleteSales(Integer saleId) {
        return Boolean.TRUE;
    }
}
