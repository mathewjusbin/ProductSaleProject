package com.sparksupport.product.product_sales_application.dto;
import jakarta.validation.constraints.Min;

public class ProductPaginationRequest {

    @Min(value = 0, message = " Page number should be 0 or greater ")
    private int pageNumber;

    @Min(value = 1, message = " List size should be 1 or greater ")
    private int listSize;

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getListSize() {
        return listSize;
    }

    public void setListSize(int listSize) {
        this.listSize = listSize;
    }
}
