package models.users;
import com.avaje.ebean.Model;
import org.mindrot.jbcrypt.BCrypt;
import models.*;
import java.util.*;
import javax.persistence.*;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Created by conno on 11/03/2017.
 */
@Entity
@DiscriminatorValue("Consultant")
@PrimaryKeyJoinColumn(referencedColumnName = "idNum")
public class Consultant extends User{
    private String specialization;

    @OneToMany(mappedBy = "c")
    private List<Patient> patients = new ArrayList<>();

    @OneToMany(mappedBy = "c")
    private List<Appointment> appointments = new ArrayList<>();


    public Consultant(String fname, String lname, String phoneNumber, String address, String ppsNumber, Date dateOfBirth, String email, String password) {
        super(fname, lname, phoneNumber, address, ppsNumber, dateOfBirth, email, password);
        specialization = null;
    }

    public static Finder<String, Consultant> find = new Finder<String, Consultant>(Consultant.class);

    public static List<Consultant> findAllConsultants(){
        return Consultant.find.all();
    }



    public static Consultant create(Consultant c){
        c.save();
        return c;
    }

    public static Consultant getConsultantById(String id){
        if(id == null)
            return null;
        else
            return find.byId(id);
    }

    public void addPatient(Patient p){
        patients.add(p);
    }

    public List<Date> checkAppointments(){
        ArrayList<Date> appointmentDates = new ArrayList<>();
        if(appointments.size() != 0){ //no existing appointments for this consultant
            for(Appointment a : appointments.stream().filter(a ->!a.isComplete()).collect(Collectors.toList())){
                appointmentDates.add(a.getAppDate());
            }
        }else{
            List<Date> dates = appointmentDates;
            return dates;
        }
        List<Date> dates = appointmentDates;
        return dates;
    }

    public void popAppointments(){
        appointments.clear();
        List<Appointment> appoints = Appointment.findAll();
        for(Appointment a: appoints){
            if(a.getC().getIdNum().equals(this.getIdNum()) && !appointments.contains(a)){
                appointments.add(a);
            }
        }
    }
    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public List<Appointment> getAppointments() {
        return appointments.stream().filter(a ->!a.isComplete()).collect(Collectors.toList());
    }
}
