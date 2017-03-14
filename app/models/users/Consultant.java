package models.users;
import com.avaje.ebean.Model;
import org.mindrot.jbcrypt.BCrypt;
import models.*;
import java.util.*;
import javax.persistence.*;
import java.util.Date;

/**
 * Created by conno on 11/03/2017.
 */
@Entity
@DiscriminatorValue("Consultant")
@PrimaryKeyJoinColumn(referencedColumnName = "idNum")
public class Consultant extends User{
    private String specialization = null;

    @OneToMany(mappedBy = "c")
    private ArrayList<Patient> patients = new ArrayList<>();

    @OneToMany(mappedBy = "c")
    private ArrayList<Appointment> appointments;


    public Consultant(String fname, String lname, String phoneNumber, String address, String ppsNumber, Date dateOfBirth, String email, String password) {
        super(fname, lname, phoneNumber, address, ppsNumber, dateOfBirth, email, password);
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

    public List<Date> checkAppointments(){
        ArrayList<Date> appointmentDates = new ArrayList<>();
        for(Appointment a : appointments){
            appointmentDates.add(a.getAppDate());
        }
        if(appointmentDates.size() == 0){ //no existing appointments for this consultant
            return null;
        }
        return appointmentDates;
    }

    public void addAppointment(Appointment a){
        this.appointments.add(a);
    }
    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }


}
