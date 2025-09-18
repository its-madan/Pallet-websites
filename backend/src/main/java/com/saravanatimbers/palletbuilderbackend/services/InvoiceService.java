package com.saravanatimbers.palletbuilderbackend.services;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.saravanatimbers.palletbuilderbackend.models.Order;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Element;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class InvoiceService {
    /**
     * Generates a PDF invoice for the given order. This is a placeholder implementation.
     * Replace with your actual PDF generation logic as needed.
     */
    public byte[] generateInvoicePdf(Order order) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Logo
            try {
                // Load logo from classpath (resources/static/images)
                java.io.InputStream logoStream = getClass().getClassLoader().getResourceAsStream("static/images/company-logo.png");
                if (logoStream != null) {
                    byte[] logoBytes = logoStream.readAllBytes();
                    Image logo = Image.getInstance(logoBytes);
                    logo.scaleToFit(80, 80);
                    logo.setAlignment(Element.ALIGN_CENTER);
                    document.add(logo);
                }
            } catch (Exception e) {
                // Logo is optional
            }

            // Company Info
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            Paragraph company = new Paragraph("SARAVANA TIMBERS", titleFont);
            company.setAlignment(Element.ALIGN_CENTER);
            document.add(company);
            Paragraph gstPhone = new Paragraph("GSTIN: 33ASPPS0683QIZU    Phone: 9788885558", subFont);
            gstPhone.setAlignment(Element.ALIGN_CENTER);
            document.add(gstPhone);
            Paragraph address = new Paragraph("No 7,Thudiyalur road, Saravanampatti, Coimbatore - 641035", subFont);
            address.setAlignment(Element.ALIGN_CENTER);
            document.add(address);
            document.add(Chunk.NEWLINE);

            // Invoice Title
            Paragraph invoiceTitle = new Paragraph("TAX INVOICE", boldFont);
            invoiceTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(invoiceTitle);
            document.add(Chunk.NEWLINE);

            // Order and Date
            String orderId = order != null ? order.getOrderId() : "Unknown";
            String invoiceDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            PdfPTable orderTable = new PdfPTable(2);
            orderTable.setWidthPercentage(100);
            orderTable.setSpacingBefore(5f);
            orderTable.setSpacingAfter(5f);
            orderTable.addCell(new PdfPCell(new Phrase("Order ID: " + orderId, normalFont)) {{ setBorder(Rectangle.NO_BORDER); }});
            orderTable.addCell(new PdfPCell(new Phrase("Date: " + invoiceDate, normalFont)) {{ setBorder(Rectangle.NO_BORDER); setHorizontalAlignment(Element.ALIGN_RIGHT); }});
            document.add(orderTable);

            // Pallet Order Details Table
            Map<String, Object> details = order != null ? order.getQuoteDetails() : null;
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setSpacingBefore(10f);
            detailsTable.setSpacingAfter(10f);
            detailsTable.addCell(new PdfPCell(new Phrase("Description", boldFont)));
            detailsTable.addCell(new PdfPCell(new Phrase("Details", boldFont)));

            if (details != null) {
                addDetailCell(detailsTable, "Pallet Type", getDetail(details, order, "palletType"));
                addDetailCell(detailsTable, "Material", getDetail(details, order, "material"));
                addDetailCell(detailsTable, "Urgency", getDetail(details, order, "urgency"));
                addDetailCell(detailsTable, "Quantity", getDetail(details, order, "quantity"));
                addDetailCell(detailsTable, "Length (mm)", getDetail(details, order, "length"));
                addDetailCell(detailsTable, "Width (mm)", getDetail(details, order, "width"));
                addDetailCell(detailsTable, "Height (mm)", getDetail(details, order, "height"));
                addDetailCell(detailsTable, "Load Capacity (kg)", getDetail(details, order, "loadCapacity"));
                addDetailCell(detailsTable, "Quoted ID", getDetail(details, order, "quoteId"));
                addDetailCell(detailsTable, "Base Price (per unit)", getDetail(details, order, "basePrice"));
                addDetailCell(detailsTable, "Material Surcharge (per unit)", getDetail(details, order, "materialSurcharge"));
                addDetailCell(detailsTable, "Urgency Fee (per unit)", getDetail(details, order, "urgencyFee"));
                addDetailCell(detailsTable, "Shipping (total)", getDetail(details, order, "shipping"));
                // --- CGST and SGST as percent in label, amount in value ---
                double subtotal = 0.0;
                double cgstPercent = 0.0;
                double sgstPercent = 0.0;
                try { subtotal = Double.parseDouble(getDetail(details, order, "subtotal").toString()); } catch (Exception ignored) {}
                try { cgstPercent = Double.parseDouble(getDetail(details, order, "cgstPercent").toString()); } catch (Exception ignored) {}
                try { sgstPercent = Double.parseDouble(getDetail(details, order, "sgstPercent").toString()); } catch (Exception ignored) {}
                double cgstAmount = subtotal * cgstPercent / 100.0;
                double sgstAmount = subtotal * sgstPercent / 100.0;
                addDetailCell(detailsTable, String.format("CGST (%.0f%%)", cgstPercent), String.format("%.2f", cgstAmount));
                addDetailCell(detailsTable, String.format("SGST (%.0f%%)", sgstPercent), String.format("%.2f", sgstAmount));
                // --- End CGST/SGST ---
                addDetailCell(detailsTable, "Subtotal", getDetail(details, order, "subtotal"));
                addDetailCell(detailsTable, "Total Tax", getDetail(details, order, "totalTax"));
                addDetailCell(detailsTable, "Total Payable Amount", getDetail(details, order, "totalPrice"));
            }
            document.add(detailsTable);

            // Total Payable Amount
            if (details != null && details.get("totalPrice") != null) {
                Paragraph total = new Paragraph("TOTAL PAYABLE AMOUNT: ₹ " + details.get("totalPrice"), boldFont);
                total.setAlignment(Element.ALIGN_RIGHT);
                document.add(total);
            }
            document.add(Chunk.NEWLINE);

            // Payment Terms
            Paragraph paymentTerms = new Paragraph("PAYMENT TERMS:\n- Payment due within 15 days of invoice date\n- Bank: HDFC Bank\n- Account Name: Saravana Timbers\n- Account Number: 123456788012\n- IFSC Code: HDFC0001234", normalFont);
            document.add(paymentTerms);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private void addDetailCell(PdfPTable table, String label, Object value) {
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        table.addCell(new PdfPCell(new Phrase(label, normalFont)));
        table.addCell(new PdfPCell(new Phrase(value != null ? value.toString() : "-", normalFont)));
    }

    // Add this helper method for fallback logic
    private Object getDetail(Map<String, Object> details, Order order, String key) {
        Object value = details.get(key);
        if (value != null) return value;
        // Fallback: try to get from Order directly if available
        switch (key) {
            case "quantity":
                // If Order has a direct quantity field, return it (not present in current Order model)
                break;
            case "totalPrice":
                // If Order has a direct totalPrice field, return it (not present in current Order model)
                break;
            // Add more cases as needed if Order model is extended
        }
        return "-";
    }
}