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
}
