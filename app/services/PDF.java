package services;

import models.*;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.List;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;


import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class PDF {
    public final String FILE;
    static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,
            Font.BOLD);
    static Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.NORMAL, BaseColor.RED);
    static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16,
            Font.BOLD);
    static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.BOLD);

    public PDF(Patient p) {
        FILE = "public/pdfFolder/" + p.getMrn() + ".pdf";
        File file = new File(FILE);
    }

    public static void addMetaData(Document document, Patient p) {
        document.addTitle(p.getfName() + p.getlName() + " | " + p.getMrn());
        document.addSubject("Using iText");
        document.addKeywords("Java, PDF, iText");
        document.addAuthor("Lars Vogel");
        document.addCreator("Lars Vogel");
    }

    public static void addContent(Document document, ArrayList<Chart> charts, Patient p, Bill b, ArrayList<Integer> stay, ArrayList<Double> costOfStay, double costOfAppointments, double prescriptionCost) throws DocumentException {
        Anchor anchor = new Anchor("Medical Bill", catFont);
        anchor.setName("Medical Bill");
        Paragraph preface = new Paragraph();
        // We add one empty line
        addEmptyLine(preface, 1);
        // Lets write a big header
        preface.add(new Paragraph("Please ensure your patient account number is correct", catFont));

        addEmptyLine(preface, 1);
        // Will create: Report generated by: _name, _date
        preface.add(new Paragraph(
                "Report generated by: **Enter Hospital Name**, " + new Date(), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                smallBold));
        addEmptyLine(preface, 1);
        preface.add(new Paragraph(
                "Patient Name: " + p.getfName() + " " + p.getlName(),
                smallBold));
        preface.add(new Paragraph(
                "Patient MRN: " + p.getMrn(),
                smallBold));
        addEmptyLine(preface, 3);
        preface.add(new Paragraph(
                "Breakdown of costs",
                smallBold));
        document.add(preface);

        Chapter catPart = new Chapter(new Paragraph(anchor), 1);
        Paragraph subPara = new Paragraph("Breakdown Of Costs", subFont);
        Section subCatPart = catPart.addSection(subPara);
        subPara = new Paragraph("Appointments and Admittance", subFont);

        subCatPart = catPart.addSection(subPara);
        // add a list
        Paragraph paragraph = new Paragraph();
        addEmptyLine(paragraph, 2);
        subCatPart.add(paragraph);

        // add a table
        createTable(subCatPart, p, charts, b, stay, costOfStay);

        addEmptyLine(paragraph, 2);
        subCatPart.add(paragraph);
        if(p.getPrescriptionList().size() != 0) {
            createPresList(subCatPart, p , prescriptionCost);
        }
        subCatPart.add(new Paragraph("Total cost of Prescriptions: €" + prescriptionCost));
        subCatPart.add(new Paragraph("Total cost of Appointments: €" + costOfAppointments));
        subCatPart.add(new Paragraph("Total cost of Stay: €" + costOfStay.stream().mapToInt(Double::intValue).sum()));
        subCatPart.add(new Paragraph("Gross Cost: €" + (costOfStay.stream().mapToInt(Double::intValue).sum() + prescriptionCost + costOfAppointments)));

        if(p.getMedicalCard() == true) {
            subCatPart.add(new Paragraph("Patient has a medical card. Amount covered:" + p.getB().getAmount(), subFont));
        }
        subCatPart.add(new Paragraph("Net Bill: €" + p.getB().getAmount(), subFont));

        // now add all this to the document
        document.add(subCatPart);

    }

    public static void createTable(Section subCatPart, Patient p, ArrayList<Chart> charts, Bill b, ArrayList<Integer> stay, ArrayList<Double> costOfStay)
            throws BadElementException {
        PdfPTable table = new PdfPTable(5);
        // t.setBorderColor(BaseColor.GRAY);
        // t.setPadding(4);
        // t.setSpacing(4);
        // t.setBorderWidth(1);

        PdfPCell c1;
        if(p.getCompletedAppointments().size() != 0) {
            c1 = new PdfPCell(new Phrase("Appointments"));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            for(int i = 1; i <= p.getCompletedAppointments().size() ; i++ ) {
                table.addCell(c1);
                table.addCell("€" + Double.toString(b.APPOINTMENT_COST));
                table.addCell("Appointment " + Integer.toString(i));
                table.addCell("Consultant: Dr." + p.getCompletedAppointments().get(i - 1).getC().getLname());
                table.addCell(p.getCompletedAppointments().get(i-1).getE().getType());
            }
        }

        if(p.getBillingChart().getDateOfAdmittance() != null) {
            for(int i = 0; i < charts.size(); i++) {
                c1 = new PdfPCell(new Phrase(charts.get(i).getCurrentWard()));
                c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(c1);
                table.addCell(Integer.toString(stay.get(i)) + " days");
                table.addCell("€" + Double.toString(costOfStay.get(i)));
            }
        }
            subCatPart.add(table);

    }

    public static void createPresList(Section subCatPart, Patient p, double prescriptionCost) {
        List list = new List(true, false, 10);
        for (Prescription pres : p.getPrescriptionList()) {
            list.add(new ListItem(pres.getMedicine().getName() + " " + pres.getDosage() + pres.getMedicine().getUnitOfMeasurement() + " €" + (pres.getMedicine().getPricePerUnit()*pres.getDosage())));
        }
        subCatPart.add(list);
    }

    public static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
}



