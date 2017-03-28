package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by conno on 27/03/2017.
 */
@Entity
@SequenceGenerator(name = "med_gen", allocationSize=1, initialValue=1)
public class Medicine extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "med_gen")
    private String medicineId;
    private String name;
    private String sideAffects;
    private String ingredients;
    private double pricePerUnit;
    private String unitOfMeasurement;

    @OneToOne(mappedBy = "medicine")
    private Prescription p;

    public Medicine(String name, String sideAffects, String ingredients, double pricePerUnit, String unitOfMeasurement) {
        this.name = name;
        this.sideAffects = sideAffects;
        this.ingredients = ingredients;
        this.pricePerUnit = pricePerUnit;
        this.unitOfMeasurement = unitOfMeasurement;
    }

    public static Finder<String, Medicine> find = new Finder<String, Medicine>(Medicine.class);

    public static List<Medicine> findAll(){
        return Medicine.find.all();
    }


    public String getUnitOfMeasurement() {
        return unitOfMeasurement;
    }

    public void setUnitOfMeasurement(String unitOfMeasurement) {
        this.unitOfMeasurement = unitOfMeasurement;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(String medicineId) {
        this.medicineId = medicineId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSideAffects() {
        return sideAffects;
    }

    public void setSideAffects(String sideAffects) {
        this.sideAffects = sideAffects;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public Prescription getP() {
        return p;
    }

    public void setP(Prescription p) {
        this.p = p;
    }
}
