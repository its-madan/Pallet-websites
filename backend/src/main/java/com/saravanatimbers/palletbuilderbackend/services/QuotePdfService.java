package com.saravanatimbers.palletbuilderbackend.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import com.saravanatimbers.palletbuilderbackend.models.Quote;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.Comparator;

@Service
public class QuotePdfService {
    public byte[] generateQuotePdf(Quote quote) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

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

            // Add first uploaded image (if any)
            try {
                String uploadDir = "backend/uploads/quotes/" + quote.getQuoteId();
                File dir = new File(uploadDir);
                if (dir.exists() && dir.isDirectory()) {
                    File[] images = dir.listFiles((d, name) -> name.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|webp)"));
                    if (images != null && images.length > 0) {
                        // Sort by name for determinism
                        File imageFile = java.util.Arrays.stream(images).sorted(Comparator.comparing(File::getName)).findFirst().get();
                        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
                        Image img = Image.getInstance(imageBytes);
                        img.scaleToFit(300, 300);
                        img.setAlignment(Element.ALIGN_CENTER);
                        document.add(img);
                        document.add(Chunk.NEWLINE);
                    }
                }
            } catch (Exception e) {
                // Ignore image errors
            }

            // Quote Details Table
            Map<String, Object> details = quote.getDetails();
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setSpacingBefore(10f);
            detailsTable.setSpacingAfter(10f);
            detailsTable.addCell(new PdfPCell(new Phrase("Description", boldFont)));
            detailsTable.addCell(new PdfPCell(new Phrase("Details", boldFont)));
            if (details != null) {
                for (Map.Entry<String, Object> entry : details.entrySet()) {
                    detailsTable.addCell(new PdfPCell(new Phrase(entry.getKey(), normalFont)));
                    detailsTable.addCell(new PdfPCell(new Phrase(entry.getValue() != null ? entry.getValue().toString() : "-", normalFont)));
                }
            }
            document.add(detailsTable);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
} 