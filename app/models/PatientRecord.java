package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by conno on 18/04/2017.
 */
@Entity
@SequenceGenerator(name = "record_gen", allocationSize=1, initialValue=1)
public class PatientRecord extends Model implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "record_gen")
    private String recordId;

    @OneToOne
    @JoinColumn(name = "mrn")
    private Patient p;


    @OneToMany(mappedBy = "patientRecord")
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "patientRecord")
    private List<Chart> charts = new ArrayList<>();

    private PatientRecord(Patient p) {
        this.p = p;
        if(p.getAllBillingCharts().size() != 0){
            this.charts = p.getAllBillingCharts();  //charts to be paid and added to record
        }
        if(p.getCompletedAppointments().size() != 0){
            this.appointments = p.getCompletedAppointments(); //appointments which are completed are to be paid and added to record
        }
    }

    public static PatientRecord record(Patient p){
        PatientRecord pr = new PatientRecord(p);
        p.setPatientRecord(pr);
        p.getCompletedAppointments().stream().forEach(a -> {
            a.setP(null);
            a.setPatientRecord(pr);
            p.getAllAppointments().remove(a);
            a.update();
        });
        p.getCharts().stream().forEach(c -> {
            if(c.getDischargeDate() != null) {
                c.setP(null);
                c.setPatientRecord(pr);
                p.getCharts().remove(c);
                c.update();
            }
        });
        pr.save();
        p.update();
        return pr;
    }

    public void addToRecord(){
        if(p.getAllBillingCharts().size() != 0){
            p.getAllBillingCharts().stream().forEach(c -> {
                charts.add(c);
                c.setP(null);
                p.getCharts().remove(c);
                c.update();
            });  //charts to be paid and added to record
        }
        if(p.getCompletedAppointments().size() != 0){
            p.getCompletedAppointments().stream().forEach(a -> {
                appointments.add(a);
                a.setP(null);
                p.getAllAppointments().remove(a);
                a.update();
            }); //appointments which are completed are  paid and added to record
        }
        p.update();
        this.update();
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    public List<Chart> getCharts() {
        return charts;
    }

    public void setCharts(List<Chart> charts) {
        this.charts = charts;
    }

    public String getRecordId() {
        return recordId;
    }
}
