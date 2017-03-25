package models;

import com.avaje.ebean.Model;
import java.util.List;

import javax.persistence.*;
import java.util.ArrayList;
/**
 * Created by Glen on 2/20/2017.
 */
@Entity
public class Ward extends Model {
    @Id
    private String wardId;
    private String name;
    private final int MAX_CAPACITY;
    private int currentOccupancy;
    private boolean status;

    @OneToOne(mappedBy = "w")
    private StandbyList sl;

    @OneToMany(mappedBy = "ward")
    private List<Patient> patients = new ArrayList<>();

    public Ward(String wardId, String name, int capacity) {
        this.wardId = wardId;
        this.name = name;
        this.MAX_CAPACITY = capacity;
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
        if(currentOccupancy == MAX_CAPACITY){
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
        if(sl.getPatients().size() != 0) {
            patients.add(getSl().getPatients().get(0));
            currentOccupancy++;
            getSl().decrementOccupancy();
            if(currentOccupancy == MAX_CAPACITY){
                status = true;
            }
            getSl().getPatients().remove(0);
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

    public int getMaxCapacity() {
        return MAX_CAPACITY;
    }

    public int getCurrentOccupancy() {
        return currentOccupancy;
    }

}
