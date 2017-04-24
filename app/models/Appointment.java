package models;
import com.avaje.ebean.Model;

import java.io.Serializable;
import java.util.*;
import java.text.SimpleDateFormat;

import models.users.*;
import play.data.format.Formats;

import javax.persistence.*;

@Entity
@SequenceGenerator(name = "app_gen", allocationSize=1, initialValue=1)
public class Appointment extends Model implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_gen")
    private String id;
    @Formats.DateTime(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date appDate = new Date();

    private boolean complete;

    @ManyToOne()
    @JoinColumn(name = "recordId")
    private PatientRecord patientRecord;

    @ManyToOne()
    @JoinColumn(name = "mrn")
    private Patient p;

    @ManyToOne()
    @JoinColumn(name = "idNum")
    private Consultant c;
    @ManyToOne()
    @JoinColumn(name = "equipId")
    private Equipment e = null;

    public Appointment(Date appDate, Consultant c, Patient p){
        this.setAppDate(appDate);
        this.c = c;
        this.p = p;
        complete = false;
    }

    public Appointment(Consultant c, Patient p){
        setAppDate(new Date());
        this.c = c;
        this.p = p;
        complete = false;
    }

    public static Appointment create(Appointment a){
        if(a.getC() != null && a.getP() != null) {
            a.save();
            return a;
        }
        else
            return null;
    }


    public static Finder<String, Appointment> find = new Finder<String, Appointment>(Appointment.class);

    public static List<Appointment> findAll(){
        return Appointment.find.all();
    }

    //Formatting for calendar, take in an ArrayList of appointments. Passes back out Full name of patient, Formatted date and Appointment ID.
    public static List<DateForCalendar> formatedDateList(List<Appointment> appointmentsIn) {
        ArrayList<DateForCalendar> formattedDates = new ArrayList<>();
        List<Appointment> appointments = appointmentsIn;
        for (Appointment a : appointments) {
            Patient p = Patient.find.byId(a.getP().getMrn());
            String fullname = p.getfName() + " " + p.getlName();
            DateForCalendar date = new DateForCalendar(fullname, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(a.appDate), a.getId());
            formattedDates.add(date);
        }
        return formattedDates;
    }

    public boolean isComplete() {
        return complete;
    }

    public void complete() {
        this.complete = true;
        this.update();
        p.update();
    }

    public PatientRecord getPatientRecord() {
        return patientRecord;
    }

    public void setPatientRecord(PatientRecord patientRecord) {
        this.patientRecord = patientRecord;
    }

    public String getId() {
        return id;
    }

    public Date getAppDate() {
        return appDate;
    }

    public String getFormattedAppDate(Date a){
        return new SimpleDateFormat("dd MMM yyyy").format(a);
    }

    public String getFormattedAppTime(Date a){
        return new SimpleDateFormat("hh:mm a").format(a);
    }

    public void setAppDate(Date appDate) {
        this.appDate = appDate;
    }

    public Patient getP() {
        return p;
    }

    public void setP(Patient p) {
        this.p = p;
    }

    public Consultant getC() {
        return c;
    }

    public void setC(Consultant c) {
        this.c = c;
    }

    public Equipment getE() {
        return e;
    }

    public void setEquipment(Equipment e) {
        this.e = e;
        e.addAppointment(this);
    }
}
