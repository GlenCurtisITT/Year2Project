package controllers;

import models.Patient;
import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import sun.rmi.runtime.Log;
import views.html.*;
import views.html.loginPage.*;
import views.html.mainTemplate.*;
import views.html.adminPages.*;
import views.html.consultantPages.*;
import play.data.*;
import java.util.*;

import javax.inject.Inject;
import models.users.*;
/**
 * Created by Glen on 01/03/2017.
 */
public class AdminController extends Controller{

    private FormFactory formFactory;

    @Inject
    public AdminController(FormFactory f){
        this.formFactory = f;
    }

    public Result adminHomePage(){
        User u = HomeController.getUserFromSession();
        HomeController.endPatientSession();
        return ok(adminHomePage.render(u));
    }

    public Result deletePatient(String mrn){
        Patient.find.ref(mrn).delete(); //serialize before delete
        flash("success", "Patient has been deleted.");

        return redirect(routes.HomeController.searchPatient());
    }

    @Transactional
    public Result updatePatient(String mrn){
        Patient p;
        Form<Patient> patientForm;
        try{
            p = Patient.find.byId(mrn);
            patientForm = formFactory.form(Patient.class).fill(p);
        }catch(Exception e){
            return badRequest();
        }

        return ok(addPatient.render(patientForm,null, HomeController.getUserFromSession()));
    }

}
