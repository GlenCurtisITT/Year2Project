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
    }

    public static PatientRecord record(Patient p){
        PatientRecord pr = new PatientRecord(p);
        p.setPatientRecord(pr);
        pr.save();
        if(p.getCompletedAppointments().size() != 0){
            List<Appointment> tempApps = p.getCompletedAppointments();
            for(Appointment a : tempApps){
                a.setP(null);
                a.setPatientRecord(pr);
                pr.getAppointments().add(a);
                p.getAllAppointments().remove(a);
                a.update();
                pr.update();
            }
        }
        if(p.getAllBillingCharts().size() != 0){
            List<Chart> tempCharts = p.getAllBillingCharts();
            for(Chart c : tempCharts){
                if (c.getDischargeDate() != null) {
                    c.setPatientRecord(pr);
                    pr.getCharts().add(c);
                    p.getCharts().remove(c);
                    c.setP(null);
                    c.update();
                    pr.update();
                }
            }
        }
        pr.update();
        p.update();
        return pr;
    }

    public void addToRecord(){
        if(p.getAllBillingCharts().size() != 0){
            p.getAllBillingCharts().stream().forEach(c -> {
                charts.add(c);
                c.setP(null);
                c.setPatientRecord(this);
                p.getCharts().remove(c);
                c.update();
            });  //charts to be paid and added to record
        }
        if(p.getCompletedAppointments().size() != 0){
            p.getCompletedAppointments().stream().forEach(a -> {
                appointments.add(a);
                a.setP(null);
                a.setPatientRecord(this);
                p.getAllAppointments().remove(a);
                a.update();
            }); //appointments which are completed are  paid and added to record
        }
        p.update();
        this.update();
    }

    public void setP(Patient p) {
        this.p = p;
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
