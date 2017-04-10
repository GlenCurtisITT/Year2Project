package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
    }

    public void setPatient(Patient p) {
        this.patient = p;
        p.setPrescription(this);
    }

    public Patient getPatient(){
        return this.patient;
    }


    public void serialize() throws IOException {
        final String CHARTFILE = "public/Files/charts.gz";
        try(FileOutputStream fo = new FileOutputStream(CHARTFILE);
            GZIPOutputStream gzipOut = new GZIPOutputStream(new BufferedOutputStream(fo));
            ObjectOutputStream oo = new ObjectOutputStream(gzipOut);){
            oo.writeObject(this);
        }
    }

    public static List<Prescription> readArchive(String mrn){
        final String PRESCRIPTIONFILE = "public/Files/prescriptions.gz";
        List<Prescription> prescriptionResult = new ArrayList<>();
        Prescription pres = null;
        try (FileInputStream fin = new FileInputStream(PRESCRIPTIONFILE);
             GZIPInputStream gis = new GZIPInputStream(fin);
             ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(gis))){
            while (true) {
                pres = (Prescription) ois.readObject();
                if(pres.getPatient().getMrn().equals(mrn)){
                    prescriptionResult.add(pres);
                    pres.insert();
                }
            }
        }catch (ClassNotFoundException e) {
            prescriptionResult = null;
        }catch (EOFException e) {
            return prescriptionResult;
        }catch (IOException e) {
            prescriptionResult = null;
        }
        return prescriptionResult;
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
