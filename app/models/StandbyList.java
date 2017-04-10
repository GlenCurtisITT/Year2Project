package models;

import com.avaje.ebean.Model;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by conno on 25/03/2017.
 */
@Entity
@SequenceGenerator(name = "standby_gen", allocationSize=1, initialValue=1)
public class StandbyList extends Model{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "standby_gen")
    private String standbyId;

    private int currentOccupancy;

    @OneToOne
    @JoinColumn(name = "wardId")
    private Ward w;

    @OneToMany(mappedBy = "sl")
    private List<Patient> patients = new ArrayList<>();

    public StandbyList(Ward w) {
        this.w = w;
        currentOccupancy = 0;
    }

    public Ward getW() {
        return w;
    }

    public void setW(Ward w) {
        this.w = w;
    }

    public List<Patient> getPatients() {
        return patients;
    }

    public void addPatient(Patient p) {
        patients.add(p);
        p.setSl(this);
        currentOccupancy++;
        p.save();
        this.update();
    }

    public int getCurrentOccupancy() {
        return currentOccupancy;
    }

    public void decrementOccupancy() {
        this.currentOccupancy--;
    }
}
