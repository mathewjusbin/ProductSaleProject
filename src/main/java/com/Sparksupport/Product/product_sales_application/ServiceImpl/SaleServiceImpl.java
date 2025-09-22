package com.Sparksupport.Product.product_sales_application.ServiceImpl;

import com.Sparksupport.Product.product_sales_application.Dto.Product;
import com.Sparksupport.Product.product_sales_application.Dto.Sale;
import com.Sparksupport.Product.product_sales_application.Exception.ProductNotFoundException;
import com.Sparksupport.Product.product_sales_application.Repository.ProductRepository;
import com.Sparksupport.Product.product_sales_application.Repository.SaleRepository;
import com.Sparksupport.Product.product_sales_application.Service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

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
    public Sale addSales(Integer productId, Sale sale) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
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
