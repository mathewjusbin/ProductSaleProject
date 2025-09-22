package com.Sparksupport.Product.product_sales_application.ServiceImpl;

import com.Sparksupport.Product.product_sales_application.Dto.Product;
import com.Sparksupport.Product.product_sales_application.Dto.Sale;
import com.Sparksupport.Product.product_sales_application.Exception.ProductNotFoundException;
import com.Sparksupport.Product.product_sales_application.Repository.ProductRepository;
import com.Sparksupport.Product.product_sales_application.Repository.SaleRepository;
import com.Sparksupport.Product.product_sales_application.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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
    public Product addProduct(Product product) {
        //Validation
        if (productRepository.existsByName(product.getName())) {
            throw new IllegalArgumentException("Product with name '" + product.getName() + "' already exists.");
        }
        return productRepository.save(product);
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
