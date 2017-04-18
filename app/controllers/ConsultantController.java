package controllers;

import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import views.html.chiefAdminPages.chiefAdminHomePage;
import views.html.mainTemplate.*;
import views.html.consultantPages.*;

import java.io.IOException;
import java.util.*;

import java.util.stream.Collectors;

import models.users.*;
import models.*;

import javax.inject.Inject;

@Security.Authenticated(Secured.class)
@With(AuthAdminOrConsultant.class)
public class ConsultantController extends Controller {

    private FormFactory formFactory;

    @Inject
    public ConsultantController(FormFactory f){
        this.formFactory = f;
    }


    public Result consultantHomePage(){
        User u = HomeController.getUserFromSession();
        Consultant c = (Consultant) u;
        List<Appointment> appointments = new ArrayList<>();
        if(c.getSpecialization() == null){
            flash("error", "You have not declared your specialization");
        }
        if(c.getAppointments().size() != 0) {
            appointments = c.getAppointments().stream().filter(a ->!a.isComplete()).collect(Collectors.toList());
            Collections.sort(appointments, new DateComparator());
            HomeController.endPatientSession();
            return ok(consultantHomePage.render(c, appointments));
        }
        else
            return ok(consultantHomePage.render(c, appointments));
    }

    public Result viewAppointments(){
        Consultant c = (Consultant)HomeController.getUserFromSession();
        List<Appointment> appointmentList = c.getAppointments().stream().filter(a ->!a.isComplete()).collect(Collectors.toList());
        return ok(viewAppointments.render(c, appointmentList));
    }

    public Result dischargePatient() {
        Patient p = HomeController.getPatientFromSession();
        Chart c = p.getCurrentChart();
        Consultant consultant = (Consultant)HomeController.getUserFromSession();
        Ward w = p.getWard();
        c.setDischargeDate(new Date());
        w.dischargePatient(p);
        Bill b = p.getB();
        Chart newChart = new Chart(p);
        newChart.save();
        p.setChart(newChart);
        p.update();
        c.update();
        w.update();
        b.calcBill();
        b.update();
        if(w.getSl().getPatients().size() != 0){
            Patient nextP = w.getSl().getPatients().get(0);
            String logFileString = "Patient " + nextP.getfName() + " " + nextP.getlName() + "(" + nextP.getMrn() + ")" + " is next on the stanby list for " + w.getName();
            LogFile.writeToLog(logFileString);
            String logFileString2 = "Patient " + p.getfName() + " " + p.getlName() + "(" + p.getMrn() + ")" + " was discharged from " + w.getName() + " by Dr." + consultant.getLname() + "ID (" + consultant.getIdNum() + ")";
            LogFile.writeToLog(logFileString2);
            flash("success", "Patient has been discharged. Bill has been generated. Patient " + nextP.getMrn() + " is next on the standby list (See Logs)");
            return redirect(routes.HomeController.viewPatientByID(p.getMrn()));
        }
        String logFileString2 = "Patient " + p.getfName() + " " + p.getlName() + "(" + p.getMrn() + ")" + " was discharged from " + w.getName() + " by Dr." + consultant.getLname() + "ID (" + consultant.getIdNum() + ")";
        LogFile.writeToLog(logFileString2);
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

    public Result completeAppointment(String id){
        Consultant c = (Consultant) HomeController.getUserFromSession();
        List<Appointment> appointments = c.getAppointments();
        Appointment a = Appointment.find.byId(id);
        Patient p = a.getP();

        a.complete();
        String log ="Appointment for Patient " + p.getfName() + p.getlName() + "(" + p.getMrn() + ") was completed by Dr." + c.getLname();
        LogFile.writeToLog(log);
        flash("success", log);
        return ok(viewAppointments.render(c, appointments));

    }

    public Result declareSpecialisation(){
        Consultant c = (Consultant) HomeController.getUserFromSession();

        return ok(declareSpecialisation.render(c));
    }

    public Result declareSecialisationSubmit(){
        DynamicForm df = formFactory.form().bindFromRequest();
        Consultant c = Consultant.find.byId(df.get("id"));
        c.setSpecialization(df.get("specialisation"));
        c.update();
        flash("success", "Specialisation Added.");
        return redirect(routes.ConsultantController.consultantHomePage());
    }

}
