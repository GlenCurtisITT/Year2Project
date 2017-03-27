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
    private int currentCapacity;

    @OneToMany(mappedBy = "ward")
    private List<Patient> patients = new ArrayList<>();

    public Ward(String wardId, String name, int capacity) {
        this.wardId = wardId;
        this.name = name;
        this.MAX_CAPACITY = capacity;
        this.currentCapacity = 0;
    }
    public static Finder<String, Ward> find = new Finder<String, Ward>(Ward.class);

    public static List<Ward> findAll(){
        return Ward.find.all();
    }


    public void admitPatient(Patient p){
        patients.add(p);
        p.setWard(this);
        currentCapacity++;
        p.save();
        this.save();
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

    public int getCurrentCapacity() {
        return currentCapacity;
    }

}
