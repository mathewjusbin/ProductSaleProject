package com.sparksupport.product.product_sales_application.serviceImpl;

import com.sparksupport.product.product_sales_application.model.Product;
import com.sparksupport.product.product_sales_application.model.Sale;
import com.sparksupport.product.product_sales_application.config.PdfTaskManager;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ProductPdfService implements com.sparksupport.product.product_sales_application.service.ProductPdfService {

    private static final float MARGIN = 50;
    private static final float ROW_HEIGHT = 25;
    private static final float HEADER_HEIGHT = 30;
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // Async processing components
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final Path exportDir = Paths.get("./exports");
    private final PdfTaskManager taskManager;
    private final com.sparksupport.product.product_sales_application.service.ProductService productService;
    private final com.sparksupport.product.product_sales_application.repository.SaleRepository saleRepository;

    @Autowired
    public ProductPdfService(PdfTaskManager taskManager,
                           com.sparksupport.product.product_sales_application.service.ProductService productService,
                           com.sparksupport.product.product_sales_application.repository.SaleRepository saleRepository) throws IOException {
        this.taskManager = taskManager;
        this.productService = productService;
        this.saleRepository = saleRepository;
        // Ensure export directory exists
        if (!Files.exists(exportDir)) {
            Files.createDirectories(exportDir);
        }
    }

    @Override
    public String submitPdfGenerationJob(List<Product> products) {
        String jobId = UUID.randomUUID().toString();
        taskManager.setJobStatus(jobId, "IN_PROGRESS");

        executorService.submit(() -> {
            try {
                byte[] pdfBytes = generateProductTablePdf(products);
                String fileName = "products-report-" + jobId + ".pdf";
                Path filePath = exportDir.resolve(fileName);
                Files.write(filePath, pdfBytes);
                taskManager.setJobStatus(jobId, "COMPLETED");
                System.out.println("PDF generation completed successfully for jobId: " + jobId);
            } catch (Exception e) {
                taskManager.setJobStatus(jobId, "FAILED");
                System.err.println("PDF generation failed for jobId: " + jobId + ". Error: " + e.getMessage());
                e.printStackTrace();

                // Log the detailed error
                try {
                    System.err.println("Detailed error information:");
                    System.err.println("Exception type: " + e.getClass().getSimpleName());
                    System.err.println("Message: " + e.getMessage());
                    if (e.getCause() != null) {
                        System.err.println("Cause: " + e.getCause().getMessage());
                    }
                } catch (Exception logEx) {
                    System.err.println("Error while logging exception details: " + logEx.getMessage());
                }
            }
        });

        return jobId;
    }

    @Override
    public String checkJobStatus(String jobId) {
        return taskManager.getJobStatus(jobId);
    }

    @Override
    public byte[] getFileIfReady(String jobId) throws Exception {
        String status = checkJobStatus(jobId);

        if (!"COMPLETED".equals(status)) {
            // Return null if file is not ready - controller will handle status response
            return null;
        }

        String fileName = "products-report-" + jobId + ".pdf";
        Path filePath = exportDir.resolve(fileName);

        if (!Files.exists(filePath)) {
            throw new Exception("File not found for completed job: " + jobId);
        }

        return Files.readAllBytes(filePath);
    }

    @Override
    public byte[] generateProductTablePdf(List<Product> products) throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                drawProductTable(contentStream, products, page);
            }

            document.save(out);
            return out.toByteArray();
        }
    }

    private void drawProductTable(PDPageContentStream contentStream, List<Product> products, PDPage page) throws IOException {
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();

        // A4 page width is approximately 595px, with 50px margins on each side = 495px available
        // Column definitions - optimized to fit within page margins without clipping
        String[] headers = {"ID", "Product Name", "Description", "Price", "Qty", "Revenue"};
        float[] columnWidths = {30, 140, 180, 65, 40, 80}; // Increased Product Name to 140px, reduced Description to 180px

        float currentY = pageHeight - 100; // Start from top

        // Draw title
        drawTitle(contentStream, "Product Inventory Report", pageWidth, currentY);
        currentY -= 50;

        // Draw table header
        currentY = drawTableHeader(contentStream, headers, columnWidths, currentY);

        // Draw product rows
        drawProductRows(contentStream, products, columnWidths, currentY);

        // Draw footer
        drawFooter(contentStream, products.size(), pageWidth);
    }

    private void drawTitle(PDPageContentStream contentStream, String title, float pageWidth, float yPosition) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
        float titleWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(title) / 1000 * 18;
        float titleX = (pageWidth - titleWidth) / 2;

        contentStream.beginText();
        contentStream.newLineAtOffset(titleX, yPosition);
        contentStream.showText(title);
        contentStream.endText();
    }

    private float drawTableHeader(PDPageContentStream contentStream, String[] headers,
                                 float[] columnWidths, float startY) throws IOException {
        float currentY = startY;
        float currentX = MARGIN;

        // Set header background color (light gray)
        contentStream.setNonStrokingColor(Color.LIGHT_GRAY);
        contentStream.addRect(MARGIN, currentY - HEADER_HEIGHT, columnWidths[0] + columnWidths[1] + columnWidths[2] + columnWidths[3] + columnWidths[4] + columnWidths[5], HEADER_HEIGHT);
        contentStream.fill();

        // Reset color for text
        contentStream.setNonStrokingColor(Color.BLACK);

        // Draw header text
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);

        for (int i = 0; i < headers.length; i++) {
            // Draw cell border
            drawCellBorder(contentStream, currentX, currentY - HEADER_HEIGHT, columnWidths[i], HEADER_HEIGHT);

            // Draw text (centered in cell)
            String headerText = headers[i];
            float textWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(headerText) / 1000 * 11;
            float textX = currentX + (columnWidths[i] - textWidth) / 2;
            float textY = currentY - HEADER_HEIGHT/2 - 3; // Center vertically

            contentStream.beginText();
            contentStream.newLineAtOffset(textX, textY);
            contentStream.showText(headerText);
            contentStream.endText();

            currentX += columnWidths[i];
        }

        return currentY - HEADER_HEIGHT;
    }

    private void drawProductRows(PDPageContentStream contentStream, List<Product> products,
                                 float[] columnWidths, float startY) throws IOException {
        float currentY = startY;
        contentStream.setFont(PDType1Font.HELVETICA, 9); // Smaller font for better fit

        for (Product product : products) {
            // Skip deleted products
            if (product.getIsDeleted() != null && product.getIsDeleted()) {
                continue;
            }

            float currentX = MARGIN;

            // Calculate actual revenue from sales data
            BigDecimal actualRevenue = calculateActualRevenue(product);

            // Prepare row data with better text handling
            String[] rowData = {
                String.valueOf(product.getId()),
                truncateText(product.getName(), 30), // Increased from 20 to 30 characters
                product.getDescription() != null ? product.getDescription() : "", // Will be wrapped
                formatCurrency(product.getPrice()),
                String.valueOf(product.getQuantity()),
                formatCurrency(actualRevenue)
            };

            // Calculate row height needed for description wrapping
            String description = rowData[2];
            List<String> wrappedLines = wrapText(description, columnWidths[2] - 6, PDType1Font.HELVETICA, 9); // 6px padding
            float rowHeightNeeded = Math.max(ROW_HEIGHT, wrappedLines.size() * 12); // 12px per line

            // Draw each cell with different alignment for different columns
            for (int i = 0; i < rowData.length; i++) {
                // Draw cell border with dynamic height
                drawCellBorder(contentStream, currentX, currentY - rowHeightNeeded, columnWidths[i], rowHeightNeeded);

                // Handle description column with word wrapping
                if (i == 2) { // Description column - wrap text
                    float lineY = currentY - 10; // Start from top of cell
                    for (String line : wrappedLines) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(currentX + 3, lineY);
                        contentStream.showText(line);
                        contentStream.endText();
                        lineY -= 12; // Move to next line
                    }
                } else {
                    // Other columns - single line text
                    float textX;
                    if (i == 3 || i == 4 || i == 5) { // Price, Quantity, Revenue - center aligned
                        float textWidth = PDType1Font.HELVETICA.getStringWidth(rowData[i]) / 1000 * 9;
                        textX = currentX + (columnWidths[i] - textWidth) / 2;
                    } else { // ID and Name - left aligned
                        textX = currentX + 5;
                    }

                    float textY = currentY - rowHeightNeeded/2 - 2; // Center vertically

                    contentStream.beginText();
                    contentStream.newLineAtOffset(textX, textY);
                    contentStream.showText(rowData[i]);
                    contentStream.endText();
                }

                currentX += columnWidths[i];
            }

            currentY -= rowHeightNeeded;

            // Check if we need a new page
            if (currentY < 100) {
                // Add new page logic here if needed
                break; // For now, just stop adding rows
            }
        }
    }

    // New method to calculate actual revenue from sales data
    private BigDecimal calculateActualRevenue(Product product) {
        BigDecimal totalRevenue = BigDecimal.ZERO;

        if (product.getSaleList() != null && !product.getSaleList().isEmpty()) {
            for (Sale sale : product.getSaleList()) {
                // Skip deleted sales
                if (sale.getIsDeleted() != null && sale.getIsDeleted()) {
                    continue;
                }

                // Calculate revenue from actual sales: salePrice * quantity
                BigDecimal saleRevenue = sale.getSalePrice().multiply(BigDecimal.valueOf(sale.getQuantity()));
                totalRevenue = totalRevenue.add(saleRevenue);
            }
        }

        return totalRevenue;
    }

    private void drawCellBorder(PDPageContentStream contentStream, float x, float y, float width, float height) throws IOException {
        contentStream.setStrokingColor(Color.BLACK);
        contentStream.setLineWidth(0.5f);
        contentStream.addRect(x, y, width, height);
        contentStream.stroke();
    }

    private void drawFooter(PDPageContentStream contentStream, int totalProducts, float pageWidth) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA, 10);

        String footerText = String.format("Total Products: %d | Generated on: %s",
                                        totalProducts, LocalDateTime.now().format(DATE_FORMAT));

        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, 50);
        contentStream.showText(footerText);
        contentStream.endText();

        // Page number
        String pageInfo = "Page 1 of 1";
        float pageInfoWidth = PDType1Font.HELVETICA.getStringWidth(pageInfo) / 1000 * 10;

        contentStream.beginText();
        contentStream.newLineAtOffset(pageWidth - MARGIN - pageInfoWidth, 50);
        contentStream.showText(pageInfo);
        contentStream.endText();
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    // Add new method for text wrapping
    private List<String> wrapText(String text, float maxWidth, PDType1Font font, int fontSize) throws IOException {
        List<String> lines = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            lines.add("");
            return lines;
        }

        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            float textWidth = font.getStringWidth(testLine) / 1000 * fontSize;

            if (textWidth <= maxWidth) {
                currentLine = new StringBuilder(testLine);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    // Single word is too long, truncate it
                    lines.add(truncateText(word, (int)(maxWidth / fontSize * 1.5)));
                    currentLine = new StringBuilder();
                }
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    // Helper method to format currency with Rs. prefix
    private String formatCurrency(Number amount) {
        return "Rs. " + CURRENCY_FORMAT.format(amount);
    }

    @Override
    public String submitPdfGenerationJob() {
        String jobId = UUID.randomUUID().toString();
        taskManager.setJobStatus(jobId, "IN_PROGRESS");

        // Submit the PDF generation job without pre-loading all data
        executorService.submit(() -> {
            try {
                byte[] pdfBytes = generateProductTablePdfWithBatching();
                String fileName = "products-report-" + jobId + ".pdf";
                Path filePath = exportDir.resolve(fileName);
                Files.write(filePath, pdfBytes);
                taskManager.setJobStatus(jobId, "COMPLETED");
                System.out.println("PDF generation completed successfully for jobId: " + jobId);
            } catch (Exception e) {
                taskManager.setJobStatus(jobId, "FAILED");
                System.err.println("PDF generation failed for jobId: " + jobId + ". Error: " + e.getMessage());
                e.printStackTrace();

                // Log the detailed error
                try {
                    System.err.println("Detailed error information:");
                    System.err.println("Exception type: " + e.getClass().getSimpleName());
                    System.err.println("Message: " + e.getMessage());
                    if (e.getCause() != null) {
                        System.err.println("Cause: " + e.getCause().getMessage());
                    }
                } catch (Exception logEx) {
                    System.err.println("Error while logging exception details: " + logEx.getMessage());
                }
            }
        });

        return jobId;
    }

    // New method for batch processing
    @Transactional(readOnly = true)
    public byte[] generateProductTablePdfWithBatching() throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                drawProductTableWithBatching(contentStream, page);
            }

            document.save(out);
            return out.toByteArray();
        }
    }

    private void drawProductTableWithBatching(PDPageContentStream contentStream, PDPage page) throws IOException {
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();

        String[] headers = {"ID", "Product Name", "Description", "Price", "Qty", "Revenue"};
        float[] columnWidths = {30, 140, 180, 65, 40, 80};

        float currentY = pageHeight - 100;

        // Draw title and header
        drawTitle(contentStream, "Product Inventory Report", pageWidth, currentY);
        currentY -= 50;
        currentY = drawTableHeader(contentStream, headers, columnWidths, currentY);

        // Process products in batches
        int batchSize = 50; // Process 50 products at a time
        int pageNumber = 0;
        boolean hasMoreProducts = true;

        while (hasMoreProducts && currentY > 100) {
            // Fetch batch of products
            List<Product> productBatch = fetchProductBatch(pageNumber, batchSize);

            if (productBatch.isEmpty()) {
                hasMoreProducts = false;
                break;
            }

            // Process each product in the batch
            for (Product product : productBatch) {
                if (product.getIsDeleted() != null && product.getIsDeleted()) {
                    continue;
                }

                // Calculate revenue for this product (with sales batching)
                BigDecimal actualRevenue = calculateRevenueWithBatching(product.getId());

                // Draw product row
                currentY = drawSingleProductRow(contentStream, product, actualRevenue, columnWidths, currentY);

                // Check if we need a new page
                if (currentY < 100) {
                    break;
                }
            }

            pageNumber++;
        }

        // Draw footer
        int totalProducts = getTotalProductCount();
        drawFooter(contentStream, totalProducts, pageWidth);
    }

    @Transactional(readOnly = true)
    public List<Product> fetchProductBatch(int pageNumber, int batchSize) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageNumber, batchSize);
        return productService.getAllProducts(pageable).getContent();
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateRevenueWithBatching(Integer productId) {
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int saleBatchSize = 100; // Process 100 sales at a time
        int salePageNumber = 0;
        boolean hasMoreSales = true;

        while (hasMoreSales) {
            // Fetch sales in batches using a custom repository method
            List<Sale> salesBatch = fetchSalesBatchForProduct(productId, salePageNumber, saleBatchSize);

            if (salesBatch.isEmpty()) {
                hasMoreSales = false;
                break;
            }

            // Calculate revenue for this batch
            for (Sale sale : salesBatch) {
                if (sale.getIsDeleted() != null && sale.getIsDeleted()) {
                    continue;
                }
                BigDecimal saleRevenue = sale.getSalePrice().multiply(BigDecimal.valueOf(sale.getQuantity()));
                totalRevenue = totalRevenue.add(saleRevenue);
            }

            salePageNumber++;
        }

        return totalRevenue;
    }

    private List<Sale> fetchSalesBatchForProduct(Integer productId, int pageNumber, int batchSize) {
        try {
            Pageable pageable = org.springframework.data.domain.PageRequest.of(pageNumber, batchSize);
            return saleRepository.findByProductIdAndIsDeletedFalse(productId, pageable).getContent();
        } catch (Exception e) {
            System.err.println("Error fetching sales batch for product " + productId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private int getTotalProductCount() {
        // Get total count without loading all products
        return (int) productService.getAllProducts(org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
    }

    private float drawSingleProductRow(PDPageContentStream contentStream, Product product,
                                     BigDecimal actualRevenue, float[] columnWidths, float currentY) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA, 9);
        float currentX = MARGIN;

        String[] rowData = {
            String.valueOf(product.getId()),
            truncateText(product.getName(), 30),
            product.getDescription() != null ? product.getDescription() : "",
            formatCurrency(product.getPrice()),
            String.valueOf(product.getQuantity()),
            formatCurrency(actualRevenue)
        };

        String description = rowData[2];
        List<String> wrappedLines = wrapText(description, columnWidths[2] - 6, PDType1Font.HELVETICA, 9);
        float rowHeightNeeded = Math.max(ROW_HEIGHT, wrappedLines.size() * 12);

        // Draw cells
        for (int i = 0; i < rowData.length; i++) {
            drawCellBorder(contentStream, currentX, currentY - rowHeightNeeded, columnWidths[i], rowHeightNeeded);

            if (i == 2) { // Description column - wrap text
                float lineY = currentY - 10;
                for (String line : wrappedLines) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(currentX + 3, lineY);
                    contentStream.showText(line);
                    contentStream.endText();
                    lineY -= 12;
                }
            } else {
                float textX;
                if (i == 3 || i == 4 || i == 5) {
                    float textWidth = PDType1Font.HELVETICA.getStringWidth(rowData[i]) / 1000 * 9;
                    textX = currentX + (columnWidths[i] - textWidth) / 2;
                } else {
                    textX = currentX + 5;
                }

                float textY = currentY - rowHeightNeeded/2 - 2;

                contentStream.beginText();
                contentStream.newLineAtOffset(textX, textY);
                contentStream.showText(rowData[i]);
                contentStream.endText();
            }

            currentX += columnWidths[i];
        }

        return currentY - rowHeightNeeded;
    }

    // Remove the old method that fetches all data at once
    // @Transactional(readOnly = true)
    // public List<Product> fetchProductsWithSalesDataInTransaction() { ... }
}
