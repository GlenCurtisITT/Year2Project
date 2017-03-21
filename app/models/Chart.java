package models;

import com.avaje.ebean.Model;
import play.data.format.Formats;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by conno on 20/03/2017.
 */
@Entity
@SequenceGenerator(name = "chart_gen", allocationSize=1, initialValue=1)
public class Chart extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chart_gen")
    private int chartId;
    private String currentWard;
    @Formats.DateTime(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date dateOfAdmittance;
    @Formats.DateTime(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date dischargeDate;
    private String mealPlan;

    @OneToOne
    @JoinColumn(name = "mrn")
    private Patient p;

    public Chart(String currentWard, Date dateOfAdmittance, Date dischargeDate, String mealPlan, Patient p) {
        this.currentWard = currentWard;
        this.dateOfAdmittance = dateOfAdmittance;
        this.dischargeDate = dischargeDate;
        this.mealPlan = mealPlan;
        this.p = p;
    }

    public static Finder<String, Ward> find = new Finder<String, Ward>(Ward.class);

    public static List<Ward> findAll(){
        return Ward.find.all();
    }

    public void setCurrentWard(String currentWard) {
        this.currentWard = currentWard;
    }

    public int getChartId() {
        return chartId;
    }

    public String getCurrentWard() {
        return currentWard;
    }

    public Date getDateOfAdmittance() {
        return dateOfAdmittance;
    }

    public Date getDischargeDate() {
        return dischargeDate;
    }

    public String getMealPlan() {
        return mealPlan;
    }
}
