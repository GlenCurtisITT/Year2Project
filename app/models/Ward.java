package models;

import com.avaje.ebean.Model;
import java.util.List;

import javax.persistence.*;
import java.util.ArrayList;
/**
 * Created by Glen on 2/20/2017.
 */
@Entity
@SequenceGenerator(name = "ward_gen", allocationSize=1, initialValue=1)
public class Ward extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ward_gen")
    private String wardId;
    private String name;
    private int maxCapacity;
    private int currentOccupancy;
    private boolean status;

    @OneToOne(mappedBy = "w")
    private StandbyList sl;

    @OneToMany(mappedBy = "ward")
    private List<Patient> patients = new ArrayList<>();

    public Ward(String name, int capacity) {
        this.setName(name);
        this.setMaxCapacity(capacity);
        this.currentOccupancy = 0;
        this.status = false;
    }
    public static Finder<String, Ward> find = new Finder<String, Ward>(Ward.class);

    public static List<Ward> findAll(){
        return Ward.find.all();
    }


    public void admitPatient(Patient p){
        patients.add(p);
        p.setWard(this);
        currentOccupancy++;
        if(currentOccupancy == maxCapacity){
            status = true;
        }
        p.save();
        this.save();
    }

    public void dischargePatient(Patient p){
        patients.remove(p);
        p.removeWard();
        currentOccupancy--;
        if(status == true){
            status = false;
        }
        p.save();
        this.save();
    }

    public boolean capacityStatus(){
        return status;
    }

    public StandbyList getSl() {
        return sl;
    }

    public void setSl(StandbyList sl) {
        this.sl = sl;
    }

    public String getWardId() {
        return wardId;
    }

    public String getName() {
        return name;
    }

    public int getCurrentOccupancy() {
        return currentOccupancy;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
}
