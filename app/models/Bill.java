package models;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

/**
 * Created by conno on 27/03/2017.
 */
@Entity
public class Bill extends Model implements MedBilling{
    @Id
    private String billId;
    private double amount;
    private boolean isPaid;

    @OneToOne(mappedBy = "b")
    private Chart c;

    public Bill(Chart c) {
        this.c = c;
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

    public Chart getC() {
        return c;
    }

    public void setC(Chart c) {
        this.c = c;
    }

    public int calcNumberOfDays(){
        SimpleDateFormat myFormat = new SimpleDateFormat("dd MM yyyy");
        String inputString1 = c.getDateOfAdmittance().toString();
        String inputString2 = c.getDischargeDate().toString();
        long days;
        try {
            java.util.Date date1 = myFormat.parse(inputString1);
            java.util.Date date2 = myFormat.parse(inputString2);
            long diff = date2.getTime() - date1.getTime();
            days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
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
        int days = calcNumberOfDays();
        double appointments = 0;
        double prescriptions = 0;
        double stay = 0;
        stay = days * COST_PER_DAY;
        if(c.getPrescriptionList().size() != 0) {
            for(Prescription p : c.getPrescriptionList()){
                prescriptions += p.getDosage() * p.getMedicine().getPricePerUnit();
            }
        }
        if(c.getP().getAppointments().size() != 0){
            appointments += c.getP().getAppointments().size() * APPOINTMENT_COST;
        }
        if(c.getP().getMedicalCard() == true){
            amount = stay;
        }
        else{
            amount = stay + prescriptions + appointments;
        }
/*
        PDF pdf = new PDF(amount, c.getP());
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(PDF.FILE));
            document.open();
            PDF.addMetaData(document);
            PDF.addTitlePage(document);
            PDF.addContent(document);
            document.close();
        } catch (Exception e) {

        }
*/
    }



}
