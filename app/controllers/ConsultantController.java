package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.mainTemplate.*;
import views.html.consultantPages.*;

import java.io.IOException;
import java.util.*;

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
        Chart c = p.getCurrentChart();
        Ward w = p.getWard();
        w.dischargePatient(p);
        Bill b = new Bill();
        c.setDischargeDate(new Date());
        if(p.getB() != null) {
            b = new Bill(p);
            p.setB(b);
            b.save();
        }else{
            b = p.getB();
        }
        Chart newChart = new Chart(p);
        newChart.save();
        p.setChart(newChart);
        p.update();
        c.update();
        w.update();
        b.calcBill();
        b.update();
        flash("success", "Patient has been discharged. Bill has been generated");
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
