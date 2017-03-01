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
/**
 * Created by Glen on 01/03/2017.
 */
public class AdminController extends Controller{
    public Result adminHomePage(){
        User u = HomeController.getUserFromSession();
        return ok(adminHomePage.render(u));
    }

}
