package models;
import com.avaje.ebean.Model;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import models.users.*;
import play.data.format.Formats;

import javax.persistence.*;

/**
 * Created by conno on 11/03/2017.
 */
@Entity
@SequenceGenerator(name = "app_gen", allocationSize=1, initialValue=1)
public class Appointment extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_gen")
    private String id;
    @Formats.DateTime(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date appDate = new Date();
    @ManyToOne()
    @JoinColumn(name = "mrn")
    private Patient p;
    @ManyToOne()
    @JoinColumn(name = "idNum")
    private Consultant c;

    public Appointment(Date appDate, Consultant c, Patient p){
        this.appDate = appDate;
        this.c = c;
        this.p = p;
    }

    public Appointment(Consultant c, Patient p){
        appDate = new Date();
        this.c = c;
        this.p = p;
    }

    public static Appointment create(Date appDate, Consultant c, Patient p){
        Appointment app = new Appointment(appDate, c, p);
        if(c != null && p != null) {
            c.addAppointment(app);
            p.addAppointment(app);
            app.save();
            return app;
        }
        else
            return null;
    }

    public String getId() {
        return id;
    }

    public Date getAppDate() {
        return appDate;
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
}
