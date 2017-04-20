package models;
import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by conno on 16/03/2017.
 */
@Entity
@SequenceGenerator(name = "equip_gen", allocationSize=1, initialValue=4)
public class Equipment extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "equip_gen")
    private String equipId;
    private String type;
    private boolean status;

    @OneToMany(mappedBy = "e")
    private List<Appointment> appointments = new ArrayList<>();

    public Equipment(String type, boolean status) {
        this.setType(type);
        this.setStatus(status);
    }

    public static Finder<String, Equipment> find = new Finder<String, Equipment>(Equipment.class);

    public static List<Equipment> findAll(){
        return Equipment.find.all();
    }

    public String getEquipId() {
        return equipId;
    }

    public String getType() {
        return type;
    }

    public boolean getStatus() {
        return status;
    }

    public List<Appointment> getAppointments() {
        return appointments.stream().filter(a -> !a.isComplete()).collect(Collectors.toList());
    }

    public void setStatus(boolean status) {
        this.status = status;
        this.update();
    }

    public void addAppointment(Appointment a){
        appointments.add(a);
    }

    public void setType(String type) {
        this.type = type;
    }
}
