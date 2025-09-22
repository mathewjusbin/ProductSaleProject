package com.sparksupport.product.product_sales_application.pdf.service;

import com.sparksupport.product.product_sales_application.model.Product;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ProductPdfService {
    public byte[] generateProductTablePdf(List<Product> products) throws IOException {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            float y = 750;
            float margin = 50;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
            float rowHeight = 20;
            String[] headers = {"ID", "Name", "Description", "Price", "Quantity", "Revenue"};
            float[] colWidths = {40, 100, 180, 60, 60, 60};
            float x = margin;
            // Draw headers
            for (int i = 0; i < headers.length; i++) {
                contentStream.beginText();
                contentStream.newLineAtOffset(x, y);
                contentStream.showText(headers[i]);
                contentStream.endText();
                x += colWidths[i];
            }
            y -= rowHeight;
            // Draw product rows
            contentStream.setFont(PDType1Font.HELVETICA, 11);
            for (Product p : products) {
                x = margin;
                String[] vals = {
                    String.valueOf(p.getId()),
                    p.getName(),
                    p.getDescription(),
                    String.valueOf(p.getPrice()),
                    String.valueOf(p.getQuantity()),
                    String.valueOf(p.getPrice() * p.getQuantity())
                };
                for (int i = 0; i < vals.length; i++) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(x, y);
                    contentStream.showText(vals[i]);
                    contentStream.endText();
                    x += colWidths[i];
                }
                y -= rowHeight;
                if (y < 50) break; // Avoid overflow
            }
            contentStream.close();
            document.save(out);
            return out.toByteArray();
        }
    }
}

