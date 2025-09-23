package com.sparksupport.product.product_sales_application.serviceImpl;

import com.sparksupport.product.product_sales_application.model.Product;
import com.sparksupport.product.product_sales_application.model.Sale;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProductPdfService {

    private static final float MARGIN = 50;
    private static final float ROW_HEIGHT = 25;
    private static final float HEADER_HEIGHT = 30;
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public byte[] generateProductTablePdf(List<Product> products) throws IOException {
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
}
