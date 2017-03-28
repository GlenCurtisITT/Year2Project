package models;

import com.avaje.ebean.Model;
import play.data.format.Formats;

import javax.persistence.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by conno on 20/03/2017.
 */
@Entity
@SequenceGenerator(name = "chart_gen", allocationSize=1, initialValue=1)
public class Chart extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chart_gen")
    private int chartId;
    private String currentWard;
    @Formats.DateTime(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date dateOfAdmittance;
    @Formats.DateTime(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date dischargeDate;
    private String mealPlan;

    @OneToOne
    @JoinColumn(name = "mrn")
    private Patient p;

    @OneToOne
    @JoinColumn(name = "billId")
    private Bill b;

    @ManyToMany
    @JoinTable(name = "CHARTPRESCRIPTION")
    private List<Prescription> prescriptionList = new ArrayList<>();

    public Chart() {
    }

    public Chart(String currentWard, Date dateOfAdmittance, String mealPlan, Patient p) {
        this.currentWard = currentWard;
        this.dateOfAdmittance = dateOfAdmittance;
        this.dischargeDate = null;
        this.mealPlan = mealPlan;
        this.p = p;
    }

    public static Finder<String, Ward> find = new Finder<String, Ward>(Ward.class);

    public static List<Ward> findAll(){
        return Ward.find.all();
    }

    public void serialize() throws IOException{
        final String CHARTFILE = "public/Files/charts.gz";
        try(FileOutputStream fo = new FileOutputStream(CHARTFILE);
            GZIPOutputStream gzipOut = new GZIPOutputStream(new BufferedOutputStream(fo));
            ObjectOutputStream oo = new ObjectOutputStream(gzipOut);){
            oo.writeObject(this);
        }
    }

    public static Chart readArchive(String mrn){
        final String CHARTFILE = "public/Files/charts.gz";
        Chart c = new Chart();
        Chart chartResult = null;
        try (FileInputStream fin = new FileInputStream(CHARTFILE);
             GZIPInputStream gis = new GZIPInputStream(fin);
             ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(gis))){
            while (true) {
                c = (Chart) ois.readObject();
                if(c.getP().getMrn().equals(mrn)){
                    chartResult = c;
                    chartResult.insert();
                    return chartResult;
                }
            }
        }catch (ClassNotFoundException e) {
            chartResult = null;
        }catch (IOException e) {
            chartResult = null;
        }
        return null;
    }

    public Bill getB() {
        return b;
    }

    public void setB(Bill b) {
        this.b = b;
    }

    public List<Prescription> getPrescriptionList() {
        return prescriptionList;
    }

    public void setPrescription(Prescription p) {
        this.prescriptionList.add(p);
    }

    public void setCurrentWard(String currentWard) {
        this.currentWard = currentWard;
    }

    public int getChartId() {
        return chartId;
    }

    public Patient getP() {
        return p;
    }

    public void setP(Patient p) {
        this.p = p;
    }

    public void setDischargeDate(Date dischargeDate) {
        this.dischargeDate = dischargeDate;
    }

    public String getCurrentWard() {
        return currentWard;
    }

    public Date getDateOfAdmittance() {
        return dateOfAdmittance;
    }

    public Date getDischargeDate() {
        return dischargeDate;
    }

    public String getMealPlan() {
        return mealPlan;
    }
}
