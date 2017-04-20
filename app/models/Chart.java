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
public class Chart extends Model implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chart_gen")
    private int chartId;
    private String currentWard;
    @Formats.DateTime(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date dateOfAdmittance;
    @Formats.DateTime(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date dischargeDate;
    private String mealPlan;

    @ManyToOne
    @JoinColumn(name = "mrn")
    private Patient p;

    @ManyToOne()
    @JoinColumn(name = "recordId")
    private PatientRecord patientRecord;

    public Chart() {
    }

    public Chart(Patient p) {
        this.p = p;
        this.dischargeDate = null;
    }

    public Chart(String currentWard, Date dateOfAdmittance, String mealPlan, Patient p) {
        this.currentWard = currentWard;
        this.dateOfAdmittance = dateOfAdmittance;
        this.dischargeDate = null;
        this.mealPlan = mealPlan;
        this.p = p;
    }

    public static Finder<String, Chart> find = new Finder<String, Chart>(Chart.class);
    public static List<Chart> findAll(){
        return Chart.find.all();
    }

    public PatientRecord getPatientRecord() {
        return patientRecord;
    }

    public void setPatientRecord(PatientRecord patientRecord) {
        this.patientRecord = patientRecord;
    }

    public void setDateOfAdmittance(Date dateOfAdmittance) {
        this.dateOfAdmittance = dateOfAdmittance;
    }

    public void setMealPlan(String mealPlan) {
        this.mealPlan = mealPlan;
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
