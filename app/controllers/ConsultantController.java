package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import sun.rmi.runtime.Log;
import views.html.*;
import views.html.loginPage.*;
import views.html.mainTemplate.*;
import views.html.adminPages.*;
import views.html.consultantPages.*;
import play.data.*;

import java.io.IOException;
import java.util.*;

import javax.inject.Inject;
import models.users.*;
import models.*;
/**
 * Created by Glen on 01/03/2017.
 */
public class ConsultantController extends Controller {
    public Result consultantHomePage(){
        User u = HomeController.getUserFromSession();
        HomeController.endPatientSession();
        return ok(consultantHomePage.render(u));
    }

    public Result viewAppointments(){
        Consultant c = (Consultant)HomeController.getUserFromSession();
        List<Appointment> appointmentList = c.getAppointments();
        return ok(viewAppointments.render(c, appointmentList));
    }

    public Result dischargePatient() {
        Patient p = HomeController.getPatientFromSession();
        Chart c = p.getChart();
        Ward w = p.getWard();
        w.dischargePatient(p);
        c.setDischargeDate(new Date());
        if(p.getAppointments().size() == 0){
            try {
                p.serialize();
                c.serialize();
                c.delete();
                p.delete();
                flash("success", "Patient has been discharged.");
                return redirect(routes.HomeController.searchPatient());
            } catch (IOException e) {
                flash("error", "Could not archive patient or chart");
                return redirect(routes.HomeController.discharge());
            }
        }
        try {
            c.serialize();
            c.delete();
        } catch (IOException e) {
            flash("error", "Could not archive chart");
            return redirect(routes.HomeController.discharge());
        }
        flash("success", "Patient has been discharged.");
        return redirect(routes.HomeController.viewPatientByID(p.getMrn()));
    }
}
