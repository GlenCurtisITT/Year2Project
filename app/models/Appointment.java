package models;
import com.avaje.ebean.Model;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import models.users.*;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Created by conno on 11/03/2017.
 */
public class Appointment extends Model {
    Date date = new Date();
    @ManyToOne()
    @JoinColumn(name = "mrn")
    Patient p = new Patient();
    @ManyToOne()
    @JoinColumn(name = "idNum")
    Consultant c = new Consultant();


}
