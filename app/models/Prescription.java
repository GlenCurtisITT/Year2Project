package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Entity
@SequenceGenerator(name = "pre_gen", allocationSize=1, initialValue=1)
public class Prescription extends Model implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pre_gen")
    private String prescription_Id;
    private String frequency;
    private int dosage;
    private boolean paid;

    @ManyToOne
    @JoinColumn(name = "medicineId")
    private Medicine medicine;

    @ManyToOne
    @JoinColumn(name = "mrn")
    private Patient patient;

    public Prescription(String frequency, int dosage, Medicine medicine) {
        this.frequency = frequency;
        this.dosage = dosage;
        this.medicine = medicine;
        paid = false;
    }

    public void setPatient(Patient p) {
        this.patient = p;
        p.setPrescription(this);
    }

    public void setPatientOnly(Patient p){
        this.patient = p;
    }

    public Patient getPatient(){
        return this.patient;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
        this.update();
    }

    public static Finder<String, Prescription> find = new Finder<String, Prescription>(Prescription.class);

    public String getPrescriptionId() {
        return prescription_Id;
    }

    public void setPrescriptionId(String prescriptionId) {
        this.prescription_Id = prescriptionId;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public int getDosage() {
        return dosage;
    }

    public void setDosage(int dosage) {
        this.dosage = dosage;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
        medicine.setP(this);
    }


}
