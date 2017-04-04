package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import scala.App;
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
        Consultant c = (Consultant) u;
        List<Appointment> appointments = new ArrayList<>();
        if(c.getSpecialization() == null){
            flash("error", "You have not declared your specialization");
        }
        if(c.getAppointments().size() != 0) {
            appointments = c.getAppointments();
            Collections.sort(appointments, new DateComparator());
            HomeController.endPatientSession();
            return ok(consultantHomePage.render(c, appointments));
        }
        else
            return ok(consultantHomePage.render(c, appointments));
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
        c.update();
        w.update();
        Bill b = new Bill();
        if(c.getB() != null){
            b = c.getB();
        }
        if(p.getAppointments().size() == 0 && b.isPaid()){
            if(c.getB() != null){
                if(!c.getB().isPaid()){
                    flash("success", "Patient has been discharged. Bill has been generated");
                    return redirect(routes.HomeController.viewPatientByID(p.getMrn()));
                }
            }
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

        flash("success", "Patient has been discharged.");
        return redirect(routes.HomeController.viewPatientByID(p.getMrn()));
    }

    public Result removePrescription(String prescription_id){
        Patient p = HomeController.getPatientFromSession();
        Consultant c = (Consultant)HomeController.getUserFromSession();
        Prescription pres = Prescription.find.byId(prescription_id);
        pres.delete();
        String logFileString = "Prescription " + prescription_id + " removed for Patient(" + p.getMrn() + ") by Dr." + c.getLname() + "(" + c.getIdNum() + ")";
        LogFile.writeToLog(logFileString);
        flash("success", "Prescription has been removed.");
        return redirect(routes.HomeController.viewPatientByID(p.getMrn()));
    }

    public Result addConsultant(){
        Consultant c = (Consultant) HomeController.getUserFromSession();
        Patient p = HomeController.getPatientFromSession();
        p.assignConsultant(c);
        String logFileString = "Dr. " + c.getLname() + "(" + c.getIdNum() + ") assigned to patient(" + p.getMrn() + ")";
        LogFile.writeToLog(logFileString);
        flash("success", "Patient assigned to Dr. " + c.getLname());
        return redirect(routes.HomeController.viewPatientByID(p.getMrn()));
    }
}
