package com.sparksupport.product.product_sales_application.serviceImpl;

import com.sparksupport.product.product_sales_application.exception.ProductNotFoundException;
import com.sparksupport.product.product_sales_application.model.Product;
import com.sparksupport.product.product_sales_application.repository.ProductRepository;
import com.sparksupport.product.product_sales_application.repository.SaleRepository;
import com.sparksupport.product.product_sales_application.service.ProductService;
import com.sparksupport.product.product_sales_application.dto.ProductDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, SaleRepository saleRepository) {
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
    }

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Product getProductById(Integer id) {
        return productRepository.findById(id).orElseThrow(
                ()-> new ProductNotFoundException(id)
        );
    }

    @Override
    public Product addProduct(ProductDto product) {
         Product productEntity = Product.builder()
                .description(product.getDescription())
                .name(product.getName())
                .price(product.getPrice())
                .isDeleted(false)
                .build();

        if (productRepository.existsByNameAndIsDeletedFalse(productEntity.getName())) {
            throw new IllegalArgumentException("Product with name '" + product.getName() + "' already exists.");
        }
        return productRepository.save(productEntity);
    }
    @Override
    public Product updateProduct(Integer id, Product product) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (product.getName() != null && !product.getName().isBlank()) {
            existingProduct.setName(product.getName().trim());
        }
        if (product.getPrice() != null) {
            existingProduct.setPrice(product.getPrice());
        }
        if (product.getDescription() != null && !product.getDescription().isBlank()) {
            existingProduct.setDescription(product.getDescription().trim());
        }

        return productRepository.save(existingProduct);
    }

    @Override
    public void deleteProduct(Integer id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
        } else {
            throw new ProductNotFoundException(id);
        }
    }

    @Override
    public Double getTotalRevenue() {
        /*return productRepository.findAll().stream()
                .mapToDouble(product -> {
                    double price = product.getPrice() != null ? product.getPrice() : 0.0;
                    int totalQuantity = product.getSaleList() != null
                            ? product.getSaleList().stream().mapToInt(Sale::getQuantity).sum()
                            : 0;
                    return price * totalQuantity;
                })
                .sum();*/

        /*List<Sale> saleList = saleRepository.findAll();
        for(Sale sale : saleList){
            if(sale.getSalePrice() != null && sale.getQuantity() != null){
                BigDecimal price = sale.getSalePrice() * sale.getQuantity();
            }
        }*/  // corner cases

        return saleRepository.findAll().stream().mapToDouble(
                saleObj -> {
                    BigDecimal price = saleObj.getSalePrice() != null ? saleObj.getSalePrice() : BigDecimal.ZERO;
                    return price.multiply(BigDecimal.valueOf(saleObj.getQuantity())).doubleValue();
                }
        ).sum();
    }

    @Override
    public Double getRevenueByProduct(Integer productId) { //corner cases
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return existingProduct.getSaleList().stream().mapToDouble(
                saleObj -> {
                    BigDecimal price = saleObj.getSalePrice() != null ? saleObj.getSalePrice() : BigDecimal.ZERO;
                    return price.multiply(BigDecimal.valueOf(saleObj.getQuantity())).doubleValue();
                }
        ).sum();
    }
}
