package controllers;

import controllers.*;
import play.api.Environment;
import play.mvc.*;
import play.data.*;
import play.db.ebean.Transactional;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import models.users.*;
import views.html.loginPage.*;

public class LoginController extends Controller {

    private FormFactory formFactory;

    private Environment env;

    @Inject
    public LoginController(Environment e, FormFactory f){
        this.env = e;
        this.formFactory = f;
    }

    public Result loginSubmit(){
        DynamicForm newLoginForm = formFactory.form().bindFromRequest();
        Form<Login> errorForm = formFactory.form(Login.class).bindFromRequest();
        User login = User.authenticate(newLoginForm.get("email"), newLoginForm.get("password"));

        if(login != null){
            session().clear();
            session("numId", login.getIdNum());
            session("role", login.checkRole());

        }else{
            flash("error", "Invalid Username or Password");
            return redirect(routes.HomeController.index());
        }

        if(session("role").equals("Admin")){
            return redirect(routes.AdminController.adminHomePage());
        }
        if(session("role").equals("Consultant")){
            return redirect(routes.ConsultantController.consultantHomePage());
        }
        return redirect(controllers.routes.HomeController.homepage());
    }

    public Result logout(){
        session().clear();
        flash("success", "You have been logged out");
        return redirect(routes.HomeController.index());
    }
}
