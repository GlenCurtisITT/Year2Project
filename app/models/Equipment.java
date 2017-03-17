package models;
import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by conno on 16/03/2017.
 */
@Entity
public class Equipment extends Model {
    @Id
    private String equipId;
    private String type;
    private boolean status;

    @OneToMany(mappedBy = "e")
    private List<Appointment> appointments = new ArrayList<>();

    public Equipment(String equipId, String type, boolean status) {
        this.equipId = equipId;
        this.type = type;
        this.status = status;
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

    public boolean isStatus() {
        return status;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void addAppointment(Appointment a){
        appointments.add(a);
    }

}
