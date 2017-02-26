package controllers;

import controllers.*;
import play.mvc.*;

import sun.rmi.runtime.Log;
import views.html.*;
import views.html.loginPage.*;
import views.html.mainTemplate.*;
import play.data.*;
import java.util.*;

import javax.inject.Inject;
import models.users.*;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    private FormFactory formFactory;

    @Inject
    public HomeController(FormFactory f){
        this.formFactory = f;
    }

    public Result index() {
        Form<Login> loginForm = formFactory.form(Login.class);
        return ok(index.render(loginForm));
    }

    public Result homepage(){
        User u = getUserFromSession();
        return ok(homepage.render(u));
    }

    public Result createUser(){
        Form<User> adduserForm = formFactory.form(User.class);
        return ok(createUser.render(adduserForm, null));
    }

    public Result addUserSubmit(){
        DynamicForm newUserForm = formFactory.form().bindFromRequest();
        Form errorForm = formFactory.form().bindFromRequest();
        //Checking if Form has errors.
        if(newUserForm.hasErrors()){
            return badRequest(createUser.render(errorForm, "Error in form."));
        }
        //Checking that Email and Name are not blank.
        if(newUserForm.get("email").equals("") || newUserForm.get("name").equals("")){
            return badRequest(createUser.render(errorForm, "Please enter an email and name."));
        }
        if(newUserForm.get("role").equals("select")){
            return badRequest(createUser.render(errorForm, "Please enter a role."));
        }
        //Checking if password == confirmed password
        if(!newUserForm.get("password").equals(newUserForm.get("passwordConfirm"))){
            return badRequest(createUser.render(errorForm, "Passwords do not match."));
        }
        //Checking if password is longer than 6 characters
        if(newUserForm.get("password").length() < 6){
            return badRequest(createUser.render(errorForm, "Password must be at least six characters."));
        }
        //Checking if email exists already in database (Duplicate primary key)
        List<User> allusers = User.findAll();
        for(User a : allusers) {
            if (a.getEmail().equals(newUserForm.get("email"))) {
                return badRequest(createUser.render(errorForm, "Email already exists in system."));
            }
        }

        //Adding user to database
        User.create(newUserForm.get("email"), newUserForm.get("role"), newUserForm.get("name"), newUserForm.get("password"));
        String s = newUserForm.get("role") + ": " + newUserForm.get("name") + " added successfully.";
        //Flashing String s to memory to be used in index screen.
        flash("success", s);
        return redirect(controllers.routes.HomeController.index());
    }

    public User getUserFromSession(){
        return User.getUserById(session().get("email"));
    }
}
