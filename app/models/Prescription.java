package models;

import com.avaje.ebean.Model;
import play.db.ebean.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by conno on 27/03/2017.
 */
@Entity
@SequenceGenerator(name = "pre_gen", allocationSize=1, initialValue=1)
public class Prescription extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pre_gen")
    private String prescription_Id;
    private String frequency;
    private int dosage;

    @OneToOne
    @JoinColumn(name = "medicineId")
    private Medicine medicine;

    @ManyToMany(mappedBy = "prescriptionList")
    private List<Chart> charts = new ArrayList<>();

    public Prescription(String frequency, int dosage, Medicine medicine) {
        this.frequency = frequency;
        this.dosage = dosage;
        this.medicine = medicine;
    }

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

    public void setChart(Chart c) {
        this.charts.add(c);
    }
}
