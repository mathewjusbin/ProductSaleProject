package com.sparksupport.product.application.serviceImpl;

import com.sparksupport.product.application.dto.UpdateSaleDto;
import com.sparksupport.product.application.exception.InsufficientStockException;
import com.sparksupport.product.application.exception.InventoryUpdateException;
import com.sparksupport.product.application.exception.ProductNotFoundException;
import com.sparksupport.product.application.exception.SaleNotFoundException;
import com.sparksupport.product.application.model.Product;
import com.sparksupport.product.application.model.Sale;
import com.sparksupport.product.application.repository.ProductRepository;
import com.sparksupport.product.application.repository.SaleRepository;
import com.sparksupport.product.application.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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

        // Use findByIdAndIsDeletedFalse to ensure we only allow sales for active (non-deleted) products
        Product existingProduct = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Check if we have enough quantity in stock
        if (existingProduct.getQuantity() < sale.getQuantity()) {
            throw new InsufficientStockException(productId, existingProduct.getQuantity(), sale.getQuantity());
        }

        // Set the sale price from the product's current price (fetched from database)
        sale.setSalePrice(BigDecimal.valueOf(existingProduct.getPrice()));

        // Reduce the quantity from product inventory
        if (!existingProduct.reduceQuantity(sale.getQuantity())) {
            throw new InventoryUpdateException(productId, sale.getQuantity());
        }

        // Save the updated product with reduced quantity
        productRepository.save(existingProduct);

        sale.setProductId(productId);
        return saleRepository.save(sale);
    }

    @Override
    @Transactional
    public Sale updateSales(Integer saleId, UpdateSaleDto updateSaleDto) {
        // Find the existing sale (only non-deleted sales)
        Sale existingSale = saleRepository.findByIdAndIsDeletedFalse(saleId)
                .orElseThrow(() -> new SaleNotFoundException(saleId));

        // Check if sale is already deleted
        if (existingSale.getIsDeleted()) {
            throw new SaleNotFoundException(saleId);
        }

        // Partial update - only update fields that are provided
        if (updateSaleDto.getQuantity() != null) {
            // If quantity is being updated, we need to handle inventory adjustment
            Product product = productRepository.findByIdAndIsDeletedFalse(existingSale.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(existingSale.getProductId()));

            // Calculate the difference in quantity
            Integer oldQuantity = existingSale.getQuantity();
            Integer newQuantity = updateSaleDto.getQuantity();
            Integer quantityDifference = newQuantity - oldQuantity;

            // Adjust product inventory accordingly
            if (quantityDifference > 0) {
                // Selling more - check if we have enough stock
                if (product.getQuantity() < quantityDifference) {
                    throw new InsufficientStockException(product.getId(), product.getQuantity(), quantityDifference);
                }
                product.setQuantity(product.getQuantity() - quantityDifference);
            } else if (quantityDifference < 0) {
                // Selling less - add back to inventory
                product.setQuantity(product.getQuantity() + Math.abs(quantityDifference));
            }

            // Save updated product inventory
            productRepository.save(product);

            // Update sale quantity
            existingSale.setQuantity(newQuantity);
        }

        if (updateSaleDto.getSaleDate() != null) {
            existingSale.setSaleDate(updateSaleDto.getSaleDate());
        }

        // Sale price remains unchanged as it's managed internally

        return saleRepository.save(existingSale);
    }

    @Override
    @Transactional
    public Boolean deleteSales(Integer saleId) {
        Sale sale = saleRepository.findByIdAndIsDeletedFalse(saleId)
                .orElseThrow(() -> new SaleNotFoundException(saleId));

        // Check if sale is already deleted
        if (sale.getIsDeleted()) {
            throw new IllegalStateException("Sale with ID " + saleId + " is already deleted");
        }

        // Option 1: Restore inventory (current implementation)
        // Use this if "delete sale" means "sale never happened"
        Product product = productRepository.findByIdAndIsDeletedFalse(sale.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(sale.getProductId()));

        // Add the sale quantity back to product inventory
        product.setQuantity(product.getQuantity() + sale.getQuantity());
        productRepository.save(product);

        // Option 2: Don't restore inventory (alternative)
        // Use this if "delete sale" means "remove from records but sale actually happened"
        // Comment out the above 4 lines and use this instead:
        /*
        // Just mark as deleted without affecting inventory
        // The products were actually sold, so inventory should remain as is
        */

        // Soft delete: mark as deleted instead of removing from database
        sale.setIsDeleted(true);
        saleRepository.save(sale);

        return Boolean.TRUE;
    }

    @Override
    public Page<Sale> getSalesByProductId(Integer productId, Pageable pageable) {
        // First verify that the product exists and is not deleted
        productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Return paginated sales for the product (only non-deleted sales)
        return saleRepository.findByProductIdAndIsDeletedFalse(productId, pageable);
    }

    @Override
    public Page<Sale> getAllSales(Pageable pageable) {
        // Return only non-deleted sales
        return saleRepository.findByIsDeletedFalse(pageable);
    }
}
