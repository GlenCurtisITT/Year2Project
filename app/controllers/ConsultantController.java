package controllers;

import play.data.DynamicForm;
import play.data.Form;
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

import static controllers.HomeController.getPatientFromSession;
import static controllers.HomeController.getUserFromSession;

@Security.Authenticated(Secured.class)
@With(AuthConsultant.class)
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

    public Result makePrescription(){
        Form<Prescription> addPrescriptionForm = formFactory.form(Prescription.class);
        List<Medicine> medicine = Medicine.findAll();
        Patient p = getPatientFromSession();
        User u = getUserFromSession();
        return ok(makePrescription.render(addPrescriptionForm, medicine, p, u, null));
    }

    public Result viewMedicine(){
        User u = getUserFromSession();
        List<Medicine> medicine = Medicine.findAll();
        return ok(viewMedicine.render(u, medicine));
    }

    public Result makePrescriptionSubmit(){
        DynamicForm newPrescriptionForm = formFactory.form().bindFromRequest();
        Form errorForm = formFactory.form().bindFromRequest();
        List<Medicine> medicine = Medicine.findAll();
        Patient p = getPatientFromSession();
        User u = getUserFromSession();
        //Checking if Form has errors.
        if(newPrescriptionForm.hasErrors()){
            return badRequest(makePrescription.render(errorForm, medicine, p, u, "Error in form."));
        }
        if(newPrescriptionForm.get("frequency").equals("") || newPrescriptionForm.get("dosage").equals("")){
            return badRequest(makePrescription.render(errorForm, medicine, p, u, "Must enter the dosage and how often patient is to take medicine"));
        }
        try {
            Integer.parseInt(newPrescriptionForm.get("dosage"));
        }catch (NumberFormatException e){
            return badRequest(makePrescription.render(errorForm, medicine, p, u, "Dosage must be represented by numbers"));
        }
        if(Medicine.find.byId(newPrescriptionForm.get("medicineId")) == null){
            return badRequest(makePrescription.render(errorForm, medicine, p, u, "Must choose Medicine"));
        }
        //can enter checking against other medicine to prevent bad interactions later
        try{
            Integer.parseInt(newPrescriptionForm.get("dosage"));
        }catch(NumberFormatException e){
            return badRequest(makePrescription.render(errorForm, medicine, p, u, "Dosage must only contain numbers"));
        }
        Medicine m = Medicine.find.byId(newPrescriptionForm.get("medicineId"));
        Prescription pres = new Prescription(newPrescriptionForm.get("frequency"), Integer.parseInt(newPrescriptionForm.get("dosage")), m);
        pres.setMedicine(m);
        pres.setPatient(p);
        pres.save();
        p.update();
        p.getB().noticeItem();
        String s = "Prescription for " + pres.getDosage() + pres.getMedicine().getUnitOfMeasurement() + " of " + pres.getMedicine().getName() + " written for " + getPatientFromSession().getfName() + " " + getPatientFromSession().getlName();
        flash("success", s);
        return redirect(controllers.routes.HomeController.viewPatient());
    }

    public Result admitPatient(){
        Patient p = getPatientFromSession();
        Form<Chart> addChartForm = formFactory.form(Chart.class);
        User u = getUserFromSession();
        List<Ward> wardList = Ward.findAll();
        return ok(admitPatient.render(addChartForm, wardList, p, u, null));
    }


    public Result admitPatientSubmit(){
        DynamicForm newChartForm = formFactory.form().bindFromRequest();
        Form errorForm = formFactory.form().bindFromRequest();
        Patient p = getPatientFromSession();
        User u = getUserFromSession();
        List<Ward> wards = Ward.findAll();
        Form<Chart> addChartForm = formFactory.form(Chart.class);
        Ward w = Ward.find.byId(newChartForm.get("wardId"));
        if(w == null){
            return badRequest(admitPatient.render(errorForm, wards, p, u, "Please select a ward."));
        }
        if(newChartForm.get("mealPlan").equals("")){
            return badRequest(admitPatient.render(errorForm, wards, p, u, "Please enter a meal plan."));
        }
        //Checking if Form has errors.
        if(newChartForm.hasErrors()){
            return badRequest(admitPatient.render(errorForm, wards, p, u, "Error in form."));
        }

        //Checking if ward is full
        if(!w.capacityStatus()){
            w.admitPatient(p);
        } else{
            if(p.getSl() != null){
                if(p.getSl().getW().getWardId().equals(w.getWardId())){
                    return ok(admitPatient.render(addChartForm, wards, p, u, "Patient is already on the standby list for this ward."));
                }
            }
            w.getSl().addPatient(p);
            //Writing to log file
            String logFileString = p.getfName() + " "
                    + p.getlName() + " was put on standby-list for ward " + w.getName();
            LogFile.writeToLog(logFileString);
            flash("success", "Ward is full. Patient added to Standby List");
            return redirect(controllers.routes.HomeController.viewPatientByID(p.getMrn()));
        }

        //Adding Appointment to database
        Chart c = p.getCurrentChart();
        c.setCurrentWard(w.getName());
        c.setMealPlan(newChartForm.get("mealPlan"));
        c.setDateOfAdmittance(new Date());
        c.update();
        //Flashing String s to memory to be used in view patient screen.
        String s = "";
        if(p.getSl() != null){
            s = p.getfName() + " " + p.getlName() + " was removed from the " + p.getSl().getW().getName() + " waiting list.\n";
            p.getSl().removePatient(p);
        }
        s += p.getfName() + " " + p.getlName() + " admitted to " + w.getName();
        flash("success", s);
        //Writing to log file.
        String logFileString = getUserFromSession().checkRole() + " "
                + getUserFromSession().getFname() + " "
                + getUserFromSession().getLname() + " admitted patient "
                + p.getfName() + " "
                + p.getlName() + " to ward " + w.getName();
        LogFile.writeToLog(logFileString);
        return redirect(controllers.routes.HomeController.viewPatientByID(p.getMrn()));
    }

    public Result discharge() {
        Patient p = getPatientFromSession();
        Consultant c = (Consultant)getUserFromSession();
        return ok(discharge.render(c, p));
    }

    public Result viewAppointments(){
        Consultant c = (Consultant)HomeController.getUserFromSession();
        List<Appointment> appointmentList = c.getAppointments().stream().filter(a ->!a.isComplete()).collect(Collectors.toList());
        return ok(viewAppointments.render(c, appointmentList));
    }

    public Result dischargePatient() {
        Patient p = getPatientFromSession();
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
        Patient p = getPatientFromSession();
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
        Patient p = getPatientFromSession();
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
        p.getB().resetPaidStatus();
        a.complete();
        p.getB().calcBill();
        String log ="Appointment for Patient " + p.getfName() + " " + p.getlName() + "(" + p.getMrn() + ") was completed by Dr." + c.getLname();
        LogFile.writeToLog(log);
        flash("success", log);
        return redirect(routes.ConsultantController.viewAppointments());

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
