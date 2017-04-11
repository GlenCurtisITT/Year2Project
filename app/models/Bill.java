package models;

import com.avaje.ebean.Model;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import services.PDF;

import javax.persistence.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by conno on 27/03/2017.
 */
@Entity
@SequenceGenerator(name = "bill_gen", allocationSize=1, initialValue=1)
public class Bill extends Model implements MedBilling{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bill_gen")
    private String billId;
    private double amount;
    private boolean isPaid;

    @OneToOne(mappedBy = "b")
    private Patient p;

    public Bill() {
    }

    public Bill(Patient p) {
        this.p = p;
        isPaid = false;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Patient getP() {
        return p;
    }

    public void setP(Patient p) {
        this.p = p;
    }

    public int calcNumberOfDays(Chart c){
        SimpleDateFormat myFormat = new SimpleDateFormat("dd MM yyyy");
        long days;
        if(c.getDateOfAdmittance() != null && c.getDischargeDate() != null) {
            String inputString1 = c.getDateOfAdmittance().toString();
            String inputString2 = c.getDischargeDate().toString();
            try {
                java.util.Date date1 = myFormat.parse(inputString1);
                java.util.Date date2 = myFormat.parse(inputString2);
                long diff = date2.getTime() - date1.getTime();
                days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            } catch (ParseException e) {
                days = 0;
            }
        }
        else{
            days = 0;
        }
        return (int) days;
    }

    public void payBill(){
        isPaid = true;
    }

    public boolean isPaid(){
        return isPaid;
    }

    public void calcBill(){
        PDF pdf = new PDF(p);
        double appointments = 0;
        double prescriptions = 0;
        ArrayList<Chart> charts = new ArrayList<>();
        ArrayList<Integer> stay = new ArrayList<>();
        ArrayList<Double> costOfStay = new ArrayList<>();
        if (p.getPrescriptionList().size() != 0) {
            for (Prescription prescription : p.getPrescriptionList()) {
                prescriptions += prescription.getDosage() * prescription.getMedicine().getPricePerUnit();
            }
        }
        if (p.getAppointments().size() != 0) {
            appointments += p.getAppointments().size() * APPOINTMENT_COST;
        }
        amount = prescriptions + appointments;
        for(Chart c : p.getAllBillingCharts()) {
            int days = calcNumberOfDays(c);
            double stayCost = 0;
            stayCost = days * COST_PER_DAY;
            if (p.getMedicalCard() == true) {
                amount = stayCost;
            } else {
                amount += stayCost;
            }
            if (amount == 0) {
                isPaid = true;
            }
            stay.add(days);
            costOfStay.add(stayCost);
            charts.add(c);
        }
            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(pdf.FILE));
                    document.open();
                PDF.addMetaData(document, p);
                PDF.addContent(document, charts, p, stay , costOfStay, appointments, prescriptions);
                document.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (DocumentException e) {
                e.printStackTrace();
            }
    }



}
